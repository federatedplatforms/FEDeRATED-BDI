package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.contracts.EventNewContract
import nl.tno.federated.states.*
import nl.tno.federated.states.EventType.*
import java.util.*

@InitiatingFlow
@StartableByRPC
class NewEventNewFlow(
    val digitalTwins: List<DigitalTwinPair>,
    val eCMRuri: String,
    val milestone: MilestoneNew
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

        val newEventState = EventNewState(goods, transportMean, location, otherDT, Date(), eCMRuri, milestone, allParties - notary)

        val txCommand = Command(EventNewContract.Commands.Other(), newEventState.participants.map { it.owningKey })

        val txBuilder = TransactionBuilder(notary)
            .addOutputState(newEventState, EventNewContract.ID)
            .addCommand(txCommand)

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

@InitiatedBy(NewEventNewFlow::class)
class NewEventNewResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                // TODO
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