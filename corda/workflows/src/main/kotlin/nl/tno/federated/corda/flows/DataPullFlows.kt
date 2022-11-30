package nl.tno.federated.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.transactions.WireTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.DataPullContract
import nl.tno.federated.states.DataPullState
import org.slf4j.LoggerFactory

@InitiatingFlow
@StartableByRPC
class DataPullQueryFlow(
    val destination: CordaX500Name,
    val sPARQLquery: String
) : FlowLogic<SignedTransaction>() {

    private val log = LoggerFactory.getLogger(DataPullQueryFlow::class.java)

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object RETRIEVING_COUNTERPARTY_INFO : Step("Generating transaction.")
        object GENERATING_TRANSACTION : Step("Generating transaction.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            RETRIEVING_COUNTERPARTY_INFO,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    override fun call(): SignedTransaction {
        log.debug("DataPullFlow initiated.")
        val notary = serviceHub.networkMapCache.notaryIdentities.firstOrNull()
        require(notary != null) { "No notary found!" }.also { log.info("No notary found!") }

        val me = serviceHub.myInfo.legalIdentities.firstOrNull()
        require(me != null) { "Me is null!" }.also { log.info("Me is null!") }

        log.debug("DataPullFlow me: $me, notary: $notary")

        /////////////
        progressTracker.currentStep = RETRIEVING_COUNTERPARTY_INFO
        val counterParty = findParty()

        val queryState = DataPullState(sPARQLquery, emptyList(), participants = listOf(counterParty, me!!))

        /////////////
        progressTracker.currentStep = GENERATING_TRANSACTION
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(queryState, DataPullContract.ID)
            .addCommand(Command(DataPullContract.Commands.Query(), queryState.participants.map { it.owningKey }))

        /////////////
        progressTracker.currentStep = VERIFYING_TRANSACTION
        try {
            txBuilder.verify(serviceHub)
        } catch (e: Exception) {
            log.debug("Verification of transaction failed because: {}", e.message, e)
            throw e
        }

        /////////////
        progressTracker.currentStep = SIGNING_TRANSACTION
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        /////////////
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySession = listOf(initiateFlow(counterParty))
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySession, GATHERING_SIGS.childProgressTracker()))

        /////////////
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        return subFlow(FinalityFlow(fullySignedTx, otherPartySession, FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun findParty(): Party {
        val counterParty = try {
            serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
                .single { it.name.organisation.equals(destination.organisation, ignoreCase = true) && it.name.locality.equals(destination.locality, ignoreCase = true) && it.name.country.equals(destination.country, ignoreCase = true) }
        } catch (e: IllegalArgumentException) {
            log.debug("Too many parties found matching organisation: $destination.organisation and locality: $destination.Locality and country $destination.Country")
            throw IllegalArgumentException("Too many parties found matching organisation: $destination.organisation and locality: $destination.Locality and country $destination.Country")
        } catch (e: NoSuchElementException) {
            log.debug("No parties found matching organisation: $destination.organisation and locality: $destination.Locality and country $destination.Country")
            throw IllegalArgumentException("No parties found matching organisation: $destination.organisation and locality: $destination.Locality and country $destination.Country")
        } catch (e: Exception) {
            log.info("Finding the correct party failed because $e.message")
            throw e
        }
        return counterParty
    }
}

@InitiatingFlow
@InitiatedBy(DataPullQueryFlow::class)
class DataPullQueryResponderFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    companion object {
        object SIGNING_QUERY_TRANSACTION :
            Step("Responding to CollectSignaturesFlow in the context of the query transaction.")

        object FINALISATION_QUERY_TRANSACTION : Step("Finalising the query transaction.")

        fun tracker() = ProgressTracker(
            SIGNING_QUERY_TRANSACTION,
            FINALISATION_QUERY_TRANSACTION
        )
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO Something to be checked in the tx?
            }
        }

        /////////////
        progressTracker.currentStep = SIGNING_QUERY_TRANSACTION
        val tx = subFlow(signTransactionFlow).tx

        /////////////
        progressTracker.currentStep = FINALISATION_QUERY_TRANSACTION
        subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = tx.id))

        return subFlow(RespondToQueryFlow(tx))
    }

}

@InitiatingFlow
class RespondToQueryFlow(val previousTx: WireTransaction) : FlowLogic<SignedTransaction>() {

    private val log = LoggerFactory.getLogger(RespondToQueryFlow::class.java)

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object PHASE_2_START : Step("Initialization of new session and gathering data for new transaction.")
        object RUN_SPARQL_QUERY : Step("Run SPARQL query retrieved from previous context and get results.")
        object GENERATING_TRANSACTION : Step("Generating result transaction.")
        object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_RESULT_TRANSACTION :
            Step("Responding to CollectSignaturesFlow in the context of the query transaction.")

        object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISATION_RESULT_TRANSACTION : Step("Finalising the query transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(
            PHASE_2_START,
            RUN_SPARQL_QUERY,
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_RESULT_TRANSACTION,
            GATHERING_SIGS,
            FINALISATION_RESULT_TRANSACTION
        )
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val notary = previousTx.notary
        /////////////
        progressTracker.currentStep = PHASE_2_START

        val inputStateWithQuery = previousTx.outputs.single().data as DataPullState
        val inputStateWithQueryAndRef =
            serviceHub.vaultService.queryBy<DataPullState>(/* is the same output state of previous approved transaction */).states.single {
                it.state.data.linearId == inputStateWithQuery.linearId
            }
        val (meList, otherPartyList) = inputStateWithQuery.participants.partition {
            it.owningKey in serviceHub.myInfo.legalIdentities
                .map { participants -> participants.owningKey }
        }
        assert(meList.size == 1 && otherPartyList.size == 1) { "Too many or too few parties found to send a response to a data pull query." }
        val otherParty = otherPartyList.single()
        /////////////
        progressTracker.currentStep = RUN_SPARQL_QUERY
        val result = graphdb().generalSPARQLquery(inputStateWithQuery.sparqlQuery, privateRepo = true)

        /////////////
        // progressTracker.currentStep = GENERATING_TRANSACTION
        val outputStateWithResult = inputStateWithQuery.copy(result = listOf(result))

        val txBuilder = TransactionBuilder(notary)
            .addInputState(inputStateWithQueryAndRef)
            .addOutputState(outputStateWithResult, DataPullContract.ID)
            .addCommand(
                Command(
                    DataPullContract.Commands.Response(),
                    outputStateWithResult.participants.map { it.owningKey })
            )

        /////////////
        progressTracker.currentStep = VERIFYING_TRANSACTION
        try {
            txBuilder.verify(serviceHub)
        } catch (e: Exception) {
            log.debug("Verification of transaction failed because: {}", e.message, e)
            throw e
        }

        /////////////
        progressTracker.currentStep = SIGNING_RESULT_TRANSACTION
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        /////////////
        progressTracker.currentStep = GATHERING_SIGS
        val otherPartySession = initiateFlow(otherParty)
        val fullySignedTx =
            subFlow(CollectSignaturesFlow(partSignedTx, listOf(otherPartySession), GATHERING_SIGS.childProgressTracker()))

        /////////////
        progressTracker.currentStep = FINALISATION_RESULT_TRANSACTION
        return subFlow(
            FinalityFlow(
                fullySignedTx,
                listOf(otherPartySession),
                FINALISATION_RESULT_TRANSACTION.childProgressTracker()
            )
        )
    }
}

@InitiatedBy(RespondToQueryFlow::class)
class DataPullResultResponderFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    companion object {
        object SIGNING : Step("Responding to CollectSignaturesFlow.")
        object FINALISATION : Step("Finalising a transaction.")

        fun tracker() = ProgressTracker(
            SIGNING,
            FINALISATION
        )
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO Something to be checked in the tx?
            }
        }

        /////////////
        progressTracker.currentStep = SIGNING
        val tx = subFlow(signTransactionFlow).tx

        /////////////
        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = tx.id))
    }
}
