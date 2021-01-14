package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.identity.Party
import net.corda.core.transactions.TransactionBuilder
import nl.tno.federated.contracts.DigitalTwinContract
import nl.tno.federated.contracts.MilestoneContract
import nl.tno.federated.states.*
import java.util.*

@InitiatingFlow
@StartableByRPC
class CreateFlow(val type: DigitalTwinType, val plate: String, val owner: String) : FlowLogic<SignedTransaction>() {
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

        progressTracker.currentStep = GENERATING_TRANSACTION

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val me = serviceHub.myInfo.legalIdentities.first()

        // Generate an unsigned transaction.
        val digitalTwinState = DigitalTwinState(DigitalTwinType.TRUCK, plate, owner, null, null, listOf(me))
        val txCommand = Command(DigitalTwinContract.Commands.Create(), digitalTwinState.participants.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(digitalTwinState)
            .addCommand(txCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        val fullySignedTx = serviceHub.signInitialTransaction(txBuilder)

        progressTracker.currentStep = FINALISING_TRANSACTION
        return subFlow(FinalityFlow(fullySignedTx, emptyList(), FINALISING_TRANSACTION.childProgressTracker()))
    }
}


@InitiatedBy(CreateFlow::class)
class CreationResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be a digital twin." using (output is DigitalTwinState)
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}
