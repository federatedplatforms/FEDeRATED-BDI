package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.states.*
import nl.tno.federated.states.EventType.*
import java.util.*

@InitiatingFlow
@StartableByRPC
class NewEventFlow(
    val type : EventType,
    val digitalTwins: List<UniqueIdentifier>,
    val location: Location,
    val previousEventID: List<UUID>?
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
        // Retrieving counterparties from location
        val counterParties = serviceHub.networkMapCache.allNodes.flatMap {it.legalIdentities}
            .filter { location.country == it.name.country } // to be changed later, meant for the poc only
        // Above we are matching the name of the country provided with the one of the known nodes,
        // and we are assuming every counterparty related to a country should receive the tx.

        val allParties = counterParties + serviceHub.myInfo.legalIdentities.first()

        // The input states are the DTs whose ID is passed as argument (i.e. those related to the event)
        val criteriaDT = QueryCriteria.LinearStateQueryCriteria(uuid = digitalTwins.map { it.id })
        val digitalTwinReferenceStates = serviceHub.vaultService.queryBy<DigitalTwinState>(criteriaDT).states

        // Generate an unsigned transaction.
        val newEventState = EventState(type, digitalTwins, Date(), location, allParties)

        val command : EventContract.Commands = when (type) {
            DEPART -> {
                EventContract.Commands.Departure()
            }
            DISCHARGE -> {
                EventContract.Commands.Discharge()
            }
            ARRIVE -> {
                EventContract.Commands.Arrive()
            }
            LOAD -> {
                EventContract.Commands.Load()
            }
            else -> {
                EventContract.Commands.Other()
            }
        }
        val txCommand = Command(command, newEventState.participants.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(newEventState, EventContract.ID)
            .addCommand(txCommand)

        // Adding Input and Output states for DT
        digitalTwinReferenceStates.forEach{txBuilder.addReferenceState(it.referenced())}

        // Adding input state if necessary
        if(type == DISCHARGE) {
            val criteriaDischarge = QueryCriteria.LinearStateQueryCriteria(uuid = previousEventID)
            val previousEventStates = serviceHub.vaultService.queryBy<EventState>(criteriaDischarge).states
            previousEventStates.forEach{txBuilder.addInputState(it)}
        }

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
                val eventOutput = stx.tx.outputs.filter { it.data is EventState }.map { it.data }
                "There must be one event output." using (eventOutput.size == 1)
                val iou = eventOutput.single() as EventState
                "I must be party to this event." using (iou.participants.contains(serviceHub.myInfo.legalIdentities.first()))
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}