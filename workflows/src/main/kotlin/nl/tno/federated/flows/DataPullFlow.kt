package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.DataPullContract
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.DataPullState
import nl.tno.federated.states.EventState

@InitiatingFlow
@StartableByRPC
class DataPullFlow(
    val nodeIdentity: String,
    val SPARQLquery: String
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
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val me = serviceHub.myInfo.legalIdentities.first()

        val counterParty = listOf(serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }.single { it.name.organisation == nodeIdentity })
        // TODO Use a more unique param to find the node

        require(counterParty.isNotEmpty())

        val allParties = counterParty + me

        val queryState = DataPullState(SPARQLquery, participants = counterParty + me)

        val txBuilder = TransactionBuilder(notary)
                .addOutputState(queryState, DataPullContract.ID)
                .addCommand(Command(DataPullContract.Commands.Query(), queryState.participants.map { it.owningKey }))

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
        val otherPartySession = counterParty.map { initiateFlow(it!!) }
        val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, otherPartySession, GATHERING_SIGS.childProgressTracker()))

        // Stage 5.
        progressTracker.currentStep = FINALISING_TRANSACTION
        // Notarise and record the transaction in both parties' vaults.
        return subFlow(FinalityFlow(fullySignedTx, otherPartySession, FINALISING_TRANSACTION.childProgressTracker()))
    }
}

@InitiatedBy(DataPullFlow::class)
class DataPullResponderFlow(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {

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
                // TODO Implement responder flow
            }
        }
        progressTracker.currentStep = SIGNING
        val txId = subFlow(signTransactionFlow).id

        progressTracker.currentStep = FINALISATION
        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}
