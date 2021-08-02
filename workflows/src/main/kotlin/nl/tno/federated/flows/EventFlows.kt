package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.states.*
import java.util.*

@InitiatingFlow
@StartableByRPC
class NewEventFlow(
    val digitalTwins: List<DigitalTwinPair>,
    val time: Date,
    val eCMRuri: String,
    val milestone: Milestone
    ) : FlowLogic<SignedTransaction>() {
    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
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

        val goods = emptyList<UUID>().toMutableList()
        val transportMean = emptyList<UUID>().toMutableList()
        val location = emptyList<UUID>().toMutableList()
        val otherDT = emptyList<UUID>().toMutableList()

        digitalTwins.forEach{
            when(it.type) {
                PhysicalObject.GOOD -> {
                    goods.add(it.uuid)
                }
                PhysicalObject.TRANSPORTMEAN -> {
                    transportMean.add(it.uuid)
                }
                PhysicalObject.LOCATION -> {
                    location.add(it.uuid)
                }
                PhysicalObject.OTHER -> {
                    otherDT.add(it.uuid)
                }
            }
        }

        val newEventState : EventState

        // TODO This criteria raises some nullpointerexception when running tests
        /*val isTheSame = QueryCriteria.VaultCustomQueryCriteria(EventNewSchemaV1.PersistentEvent::goods.equal(goods))
                .and(QueryCriteria.VaultCustomQueryCriteria( EventNewSchemaV1.PersistentEvent::transportMean.equal(transportMean)))
                .and(QueryCriteria.VaultCustomQueryCriteria(EventNewSchemaV1.PersistentEvent::location.equal(location)))
                .and(QueryCriteria.VaultCustomQueryCriteria(EventNewSchemaV1.PersistentEvent::otherDigitalTwins.equal(otherDT)))*/

        val previousEvents = serviceHub.vaultService.queryBy<EventState>(/*isTheSame*/).states
                .filter{ it.state.data.milestone == Milestone.START &&
                        it.state.data.goods == goods &&
                        it.state.data.transportMean == transportMean &&
                        it.state.data.location == location &&
                        it.state.data.otherDigitalTwins == otherDT
                }

        when(milestone) {
            Milestone.START -> {

                requireThat {
                    "There cannot be a previous equal start event" using (previousEvents.isEmpty())
                }

                newEventState = EventState(goods, transportMean, location, otherDT, Date(), listOf(TimeAndType(time, TimeType.PLANNED)), emptyList(), eCMRuri, milestone, allParties - notary)
            }
            Milestone.STOP -> {

                requireThat {
                    "There must be one previous event only" using ( previousEvents.size <= 1 )
                }

                newEventState = EventState(goods, transportMean, location, otherDT, Date(), listOf(TimeAndType(time, TimeType.PLANNED)), emptyList(), eCMRuri, milestone, allParties - notary)
            }
            else -> {
                // Make the contract and the tx fail (by setting all dt fields to empty)
                newEventState = EventState(emptyList(), emptyList(), emptyList(), emptyList(), Date(), listOf(TimeAndType(time, TimeType.PLANNED)), emptyList(), eCMRuri, milestone, allParties - notary)
            }
        }

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
        val otherPartySessions = counterParties.map { initiateFlow(it) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySessions, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(NewEventFlow::class)
class NewEventResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO what to check in the counterparty flow?
                // especially: if I'm not passing all previous states in the tx (see "requires" in the flow)
                // then I want the counterparties to check by themselves that everything's legit
            }
        }
        val txId = subFlow(signTransactionFlow).id

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
        object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
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

        requireThat{
            "There must be a corresponding event" using (retrievedEvent.isNotEmpty())
        }

        newEventState = retrievedEvent.single().state.data.copy(
                timestamps = retrievedEvent.single().state.data.timestamps + TimeAndType(time, TimeType.ESTIMATED)
        )

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
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(UpdateEstimatedTimeFlow::class)
class UpdateEstimatedTimeResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO what to check in the counterparty flow (update estimate)?
            }
        }
        val txId = subFlow(signTransactionFlow).id

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
        object GENERATING_TRANSACTION : Step("Generating transaction based on new IOU.")
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

        val txBuilder = TransactionBuilder(notary)
                .addCommand(Command(EventContract.Commands.ExecuteEvent(), correspondingEvent.participants.map { it.owningKey }))

        if(retrievedEvents.isNotEmpty())
            txBuilder.addInputState(retrievedEvents.single())

        when(correspondingEvent.milestone) {
            Milestone.STOP -> {
                val previousStartEvents = serviceHub.vaultService.queryBy<EventState>(/*isTheSame*/).states
                        .filter{ it.state.data.hasSameDigitalTwins(correspondingEvent) && it.state.data.milestone == Milestone.START }

                if(previousStartEvents.isNotEmpty()) {
                    txBuilder.addInputState(previousStartEvents.single())
                    newEventState = correspondingEvent.copy(
                            timestamps = correspondingEvent.timestamps + TimeAndType(time, TimeType.ACTUAL),
                            startTimestamps = previousStartEvents.single().state.data.timestamps
                    )
                } else {
                    newEventState = correspondingEvent.copy(
                            timestamps = correspondingEvent.timestamps + TimeAndType(time, TimeType.ACTUAL)
                    )
                }
            }
            else -> {
                newEventState = correspondingEvent.copy(
                        timestamps = correspondingEvent.timestamps + TimeAndType(time, TimeType.ACTUAL)
                )
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
        return subFlow(FinalityFlow(fullySignedTx, otherPartySessions, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(ExecuteEventFlow::class)
class ExecuteEventResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO what to check in the counterparty flow (update estimate)?
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

@CordaSerializable
data class DigitalTwinPair(
        val uuid : UUID,
        val type: PhysicalObject
)