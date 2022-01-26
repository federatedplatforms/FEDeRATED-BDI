package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.services.GraphDBService.generalSPARQLquery
import nl.tno.federated.services.GraphDBService.insertEvent
import nl.tno.federated.services.GraphDBService.isDataValid
import nl.tno.federated.services.GraphDBService.queryEventById
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import nl.tno.federated.states.PhysicalObject
import java.util.*

@InitiatingFlow
@StartableByRPC
class NewEventFlow(
    val fullEvent: String,
    val countriesInvolved: Set<String>
    ) : FlowLogic<SignedTransaction>() {
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
        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val me = serviceHub.myInfo.legalIdentities.first()
        val counterParties : MutableList<Party?> = mutableListOf()
        countriesInvolved.forEach { involvedCountry ->
            counterParties.add(serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
                .firstOrNull { it.name.country == involvedCountry })
        }
        require(!counterParties.contains(null)) { "One of the requested counterparties was not found"}

        val allParties = counterParties.map { it!! } + mutableListOf(notary, me)

        val newEvent = GraphDBService.parseRDFToEvents(fullEvent).first()

        val previousEvents = serviceHub.vaultService.queryBy<EventState>(/*isTheSame*/).states
                .filter{ it.state.data.milestone == Milestone.START &&
                        it.state.data.goods == newEvent.goods &&
                        it.state.data.transportMean == newEvent.transportMean &&
                        it.state.data.location == newEvent.location
                }

        if (newEvent.milestone == Milestone.START) {
            require(previousEvents.isEmpty()) { "There cannot be a previous equal start event" }
        }
        else if (newEvent.milestone == Milestone.STOP) {
            require (previousEvents.size <= 1) { "There must be one previous event only" }
        }

        val newEventState = EventState(
            goods = newEvent.goods,
            transportMean = newEvent.transportMean,
            location = newEvent.location,
            otherDigitalTwins = emptySet(),
            timestamps = newEvent.timestamps,
            ecmruri = newEvent.ecmruri,
            milestone = newEvent.milestone,
            fullEvent = fullEvent,
            participants = allParties - notary,
            linearId = UniqueIdentifier(newEvent.id)
        )
        require(isDataValid(newEventState)) { "RDF data is not valid"}

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(newEventState, EventContract.ID)
                .addCommand(EventContract.Commands.Create(), newEventState.participants.map { it.owningKey })

        if(previousEvents.isNotEmpty())
            txBuilder.addReferenceState(previousEvents.single().referenced())

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySessions = counterParties.map { initiateFlow(it!!) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.

        require(insertEvent(newEventState.fullEvent)) { "Unable to insert event data into the triple store."}
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(NewEventFlow::class)
class NewEventResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

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
//                require(insertEvent(outputState.fullEvent)) { "Unable to insert event data into the triple store."}
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

@InitiatingFlow
@StartableByRPC
class UpdateEstimatedTimeFlow(
        val eventUUID: UUID,
        val time: Date
) : FlowLogic<SignedTransaction>() {
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
        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val allParties = serviceHub.networkMapCache.allNodes.flatMap {it.legalIdentities}
        val me = serviceHub.myInfo.legalIdentities.first()
        val counterParties = allParties - notary - me

        val newEventState : EventState

        val retrievedEvent = serviceHub.vaultService.queryBy<EventState>().states
                .filter{ it.state.data.linearId.id == eventUUID }

        require(retrievedEvent.isNotEmpty()){ "There must be a corresponding event" }

        val retrievedEventData = retrievedEvent.single().state.data

        val newTimestamp = retrievedEventData.timestamps
        newTimestamp[EventType.ESTIMATED] = time
        newEventState = retrievedEventData.copy(
                timestamps = newTimestamp
        )

        require(isDataValid(newEventState)) { "RDF data is not valid or does not match event"}

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(newEventState, EventContract.ID)
                .addCommand(Command(EventContract.Commands.UpdateEstimatedTime(), newEventState.participants.map { it.owningKey }))

        if(retrievedEvent.isNotEmpty())
            txBuilder.addInputState(retrievedEvent.single())

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySessions = counterParties.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        require(insertEvent(newEventState.fullEvent)) { "Unable to insert event data into the triple store."}
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(UpdateEstimatedTimeFlow::class)
class UpdateEstimatedTimeResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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
                require(insertEvent(outputState.fullEvent)) { "Unable to insert event data into the triple store."}
                // TODO what to check in the counterparty flow? (update estimate)
            }
        }
        progressTracker.currentStep = SIGNING
        val txId = subFlow(signTransactionFlow).id

        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

@InitiatingFlow
@StartableByRPC
class ExecuteEventFlow(
        val eventUUID: UUID,
        val time: Date
) : FlowLogic<SignedTransaction>() {
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
        val notary = serviceHub.networkMapCache.notaryIdentities.single() // METHOD 1

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val allParties = serviceHub.networkMapCache.allNodes.flatMap {it.legalIdentities}
        val me = serviceHub.myInfo.legalIdentities.first()
        val counterParties = allParties - notary - me

        val newEventState : EventState

        val retrievedEvents = serviceHub.vaultService.queryBy<EventState>().states
                .filter{ it.state.data.linearId.id == eventUUID }

        requireThat{
            "There must be one corresponding event" using (retrievedEvents.size == 1)
        }

        val correspondingEvent = retrievedEvents.single().state.data
        require(isDataValid(correspondingEvent)) { "RDF data is not valid or does not match event"}

        val txBuilder = TransactionBuilder(notary)
                .addCommand(Command(EventContract.Commands.ExecuteEvent(), correspondingEvent.participants.map { it.owningKey }))

        if(retrievedEvents.isNotEmpty())
            txBuilder.addInputState(retrievedEvents.single())

        val newTimestamps = correspondingEvent.timestamps
        newTimestamps[EventType.ACTUAL] = time
        newEventState = correspondingEvent.copy(
            timestamps = newTimestamps
        )
        if (correspondingEvent.milestone == Milestone.STOP) {
            val previousStartEvents = serviceHub.vaultService.queryBy<EventState>(/*isTheSame*/).states
                .filter{ it.state.data.hasSameDigitalTwins(correspondingEvent) && it.state.data.milestone == Milestone.START }

            if (previousStartEvents.isNotEmpty()) {
                txBuilder.addInputState(previousStartEvents.single())
            }
        }

        txBuilder.addOutputState(newEventState, EventContract.ID)

        // Stage 2.
        progressTracker.currentStep = VERIFYING_TRANSACTION
        // Verify that the transaction is valid.
        txBuilder.verify(serviceHub)

        // Stage 3.
        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

        // Stage 4.
        progressTracker.currentStep = GATHERING_SIGS
        // Send the state to the counterparty, and receive it back with their signature.
        val otherPartySessions = counterParties.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        require(insertEvent(newEventState.fullEvent)) { "Unable to insert event data into the triple store."}
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(ExecuteEventFlow::class)
class ExecuteEventResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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
                require(insertEvent(outputState.fullEvent)) { "Unable to insert event data into the triple store."}
                // TODO what to check in the counterparty flow (execute)?
            }
        }
        progressTracker.currentStep = SIGNING
        val txId = subFlow(signTransactionFlow).id

        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

@InitiatingFlow
@StartableByRPC
class QueryGraphDBbyIdFlow(
        val id: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return queryEventById(id)
    }
}

@InitiatingFlow
@StartableByRPC
class GeneralSPARQLqueryFlow(
        val query: String
) : FlowLogic<String>() {

    @Suspendable
    override fun call(): String {
        return generalSPARQLquery(query)
    }
}

@CordaSerializable
data class DigitalTwinPair(
        val content : String,
        val type: PhysicalObject
)