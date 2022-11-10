package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.*
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.services.GraphDBService.insertEvent
import nl.tno.federated.states.EventState
import nl.tno.federated.states.Milestone
import org.slf4j.LoggerFactory

@InitiatingFlow
@StartableByRPC
class InsuranceFlow(
    val fullEvent: String,
    val countriesInvolved: Set<String>
) : FlowLogic<SignedTransaction>() {

    private val log = LoggerFactory.getLogger(InsuranceFlow::class.java)

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

        val me = serviceHub.myInfo.legalIdentities.first()
        val counterPartiesAndMe: MutableList<Party?> = mutableListOf()
        countriesInvolved.forEach { involvedCountry ->
            counterPartiesAndMe.add(serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
                .firstOrNull { it.name.country == involvedCountry })
        }
        require(!counterPartiesAndMe.contains(null)) { "One of the requested counterparties was not found" }.also { log.info("One of the requested counterparties was not found") }
        val counterParties = counterPartiesAndMe.filter { it!!.owningKey != me.owningKey }

        val allParties = counterParties.map { it!! } + mutableListOf(notary, me)
        val insuranceEvent = GraphDBService.parseRDFToEvents(fullEvent).first()
        require(insuranceEvent.labels.contains("InsuranceEvent")) { "This flow must be called with an insurance event (label == InsuranceEvent)" }.also {
            log.info("This flow" +
                "must be called with an insurance event (label == InsuranceEvent)")
        }

        val previousEvents = serviceHub.vaultService.queryBy<EventState>(/* has the same digital twins*/).states
            .filter {
                it.state.data.transportMean == insuranceEvent.transportMean && it.state.data.goods.isNotEmpty()
            }
        // this implementation assumes stopped good-transportmean connections are never re-started
        val goodsAndTheirMilestone = previousEvents.flatMap { it.state.data.goods zip listOf(it.state.data.milestone) }
        val stoppedGoods = goodsAndTheirMilestone.filter { it.second == Milestone.END }.map { it.first }
        val goodsStillInTransportMean = goodsAndTheirMilestone.filter { it.first !in stoppedGoods }.map { it.first }
        val previousEventsWithoutStop = previousEvents.filter { state ->
            state.state.data.goods.any {
                it in goodsStillInTransportMean
            }
        }

        require(previousEventsWithoutStop.isNotEmpty()) { "There must be events to share" }.also { log.info("There must events to share") }

        val insuranceEventState = EventState(goods = insuranceEvent.goods,
            transportMean = insuranceEvent.transportMean,
            location = insuranceEvent.location,
            otherDigitalTwins = insuranceEvent.otherDigitalTwins,
            timestamps = insuranceEvent.timestamps,
            ecmruri = insuranceEvent.ecmruri,
            milestone = insuranceEvent.milestone,
            fullEvent = fullEvent,
            labels = insuranceEvent.labels,
            participants = allParties - notary)

        val txBuilder = TransactionBuilder(notary)
            .addOutputState(insuranceEventState, EventContract.ID)
            .addCommand(
                Command(
                    EventContract.Commands.AccidentEvent(),
                    insuranceEventState.participants.map { it.owningKey })
            )

        previousEventsWithoutStop.forEach { txBuilder.addReferenceState(ReferencedStateAndRef(it)) }

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        try {
            txBuilder.verify(serviceHub)
        } catch (e: Exception) {
            log.debug("Verification of transaction failed because: {}", e.message, e)
            throw e
        }

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySessions = counterParties.map { initiateFlow(it!!) }
        val fullySignedTx =
            subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        require(insertEvent(insuranceEventState.fullEvent, false)) { "Unable to insert event data into the triple store." }.also {
            log.info("Unable to insert" +
                "event data into the triple store")
        }
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(InsuranceFlow::class)
class InsuranceResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

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
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val outputState = stx.tx.outputStates.single() as EventState
                // does the insurer need to check anything here? I am assuming not, as they are happy to receive anything
            }
        }
        progressTracker.currentStep = SIGNING
        val txId = subFlow(signTransactionFlow).id

        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}