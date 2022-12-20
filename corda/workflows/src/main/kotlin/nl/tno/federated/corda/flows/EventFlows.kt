package nl.tno.federated.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import nl.tno.federated.corda.services.ishare.ISHARECordaService
import nl.tno.federated.states.EventState
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.NoSuchElementException

@InitiatingFlow
@StartableByRPC
class NewEventFlow(
    private val destinations: Collection<CordaX500Name>,
    private val event: String
) : FlowLogic<SignedTransaction>() {

    private val log = LoggerFactory.getLogger(NewEventFlow::class.java)

    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
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
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val me = serviceHub.myInfo.legalIdentities.first()
        val counterParties = findParties()

        // Retrieving event ID from RDF event
        val eventID = GraphDBEventConverter.parseRDFToEventIDs(event).single()

        // Set criteria to match UUID of state with the supplied event UUID
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(eventID)))
        require(serviceHub.vaultService.queryBy<EventState>(criteria).states.isEmpty()) {
            "An event with the same UUID already exists"
        }

        val newEventState = EventState(
            fullEvent = event,
            participants = listOf(me) + counterParties,
            linearId = UniqueIdentifier(null, UUID.fromString(eventID))
        )

        val txBuilder = TransactionBuilder(notary)
            .addOutputState(newEventState, EventContract.ID)
            .addCommand(EventContract.Commands.Create(), newEventState.participants.map { it.owningKey })

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        try {
            txBuilder.verify(serviceHub)
        } catch (e: Exception) {
            log.warn("Verification of transaction failed because: {}", e.message, e)
            throw e
        }

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION

        /*
          When an Event is sent, the same event is sent to all parties
          This needs to change, because if ISHARE is used, every party hands out his own accesstoken,
          which is stored in the event.
        */
        log.info("Event flow using iSHARE: {}", serviceHub.cordaService(ISHARECordaService::class.java).ishareEnabled())
        if (serviceHub.cordaService(ISHARECordaService::class.java).ishareEnabled()) {
            // create an eventstate for every party , since they all have different accesstokens
            log.info("Gathering Access Tokens for the Event counterparties")
            counterParties.forEach {
                //  create new session to get the tokens ?
                log.info("Retrieving iSHARE access token for: {}", it.name)
                val tokenResponse = subFlow(ISHARETokenFlow(it))
                newEventState.accessTokens[it] = tokenResponse.access_token
            }
        }

        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        log.info("Sending new Event to counterparties")

        val otherPartySessions = counterParties.map { initiateFlow(it) }
        val fullySignedTx =
            subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.

        val await = await(GraphDBInsert(graphdb(), newEventState.fullEvent, false))
        require(await) {
            "Unable to insert event data into the triple store at ${me.name}."
        }.also {
            log.info("Inserted event data into the triple store: {} at: {}", await, me.name)
        }
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }

    private fun findParties(): List<Party> = destinations.map { destination ->
        try {
            serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
                .single { it.name.organisation.equals(destination.organisation, ignoreCase = true) && it.name.locality.equals(destination.locality, ignoreCase = true) && it.name.country.equals(destination.country, ignoreCase = true) }
        } catch (e: IllegalArgumentException) {
            log.warn("Too many parties found matching organisation: $destination.name and locality: $destination.locality and country $destination.country")
            throw IllegalArgumentException("Too many parties found matching organisation: $destination.name and locality: $destination.locality and country $destination.country")
        } catch (e: NoSuchElementException) {
            log.warn("No parties found matching organisation: $destination.name and locality: $destination.locality and country $destination.country")
            throw IllegalArgumentException("No parties found matching organisation: $destination.name and locality: $destination.locality and country $destination.country")
        } catch (e: Exception) {
            log.warn("Finding the correct party failed because $e.message")
            throw e
        }
    }
}

@InitiatedBy(NewEventFlow::class)
class NewEventResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

    private val log = LoggerFactory.getLogger(NewEventResponder::class.java)

    companion object {
        object VERIFYING_STRING_INTEGRITY : Step("Verifying that accompanying full event is acceptable.")
        object SIGNING : Step("Responding to CollectSignaturesFlow.")
        object FINALISATION : Step("Finalising a transaction.")

        fun tracker() = ProgressTracker(
            VERIFYING_STRING_INTEGRITY,
            SIGNING,
            FINALISATION
        )
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction {
        progressTracker.currentStep = VERIFYING_STRING_INTEGRITY

        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            @Suspendable
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val outputState = stx.tx.outputStates.single() as EventState
                val ishareService = serviceHub.cordaService(ISHARECordaService::class.java)
                log.info("Responder flow using iSHARE: {}", ishareService.ishareEnabled())
                // Check accesstoken if required and available in the eventState
                if (ishareService.ishareEnabled()) {
                    require(outputState.accessTokens.contains(serviceHub.myInfo.legalIdentities.first())) {
                        "ISHARE accessToken not found while it is required"
                    }
                    outputState.accessTokens[serviceHub.myInfo.legalIdentities.first()]?.let {
                        require(ishareService.checkAccessToken(it).first) {
                            "ISHARE AccessToken is invalid."
                        }
                        // TODO optional check if insert of event is allowed by the iSHARE AR
                    }
                    log.info("iSHARE AccessToken provided and valid, accepting new event!")
                }

                val await = await(GraphDBInsert(graphdb(), outputState.fullEvent, false))

                require(await) {
                    "Unable to insert event data into the triple store at " + serviceHub.myInfo.legalIdentities.first().name
                }.also {
                    log.info("Inserted event data into the triple store: {} at: {}", await, serviceHub.myInfo.legalIdentities.first().name)
                }
                // TODO what to check in the counterparty flow?
                // especially: if I'm not passing all previous states in the tx (see "requires" in the flow)
                // then I want the counterparties to check by themselves that everything's legit
            }
        }
        progressTracker.currentStep = SIGNING
        val txId = subFlow(signTransactionFlow).id

        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

