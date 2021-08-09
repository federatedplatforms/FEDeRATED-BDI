package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.AccessPoliciesContract
import nl.tno.federated.states.AccessPolicyState
import nl.tno.federated.states.IdsAction
import nl.tno.federated.states.Target


//////  CARGO CREATION  /////

@InitiatingFlow
@StartableByRPC
class CreateAccessPolicyFlow(
    private val context : String,
    private val type : String,
    private val id : String,
    private val idsProvider : String,
    private val idsConsumer : String,
    private val idsPermission : List<IdsAction>,
    private val idsTarget : Target
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

    @Suspendable
    override fun call(): SignedTransaction {

        progressTracker.currentStep = GENERATING_TRANSACTION

        val notary = serviceHub.networkMapCache.notaryIdentities.single()
        val me = serviceHub.myInfo.legalIdentities.first()
        val allParties = serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities } - notary
        val counterParties = allParties - me

        // Creating AP state
        val accessPolicyState = AccessPolicyState(context, type, id, idsProvider, idsConsumer, idsPermission, idsTarget, participants = allParties)

        // Generating tx
        val txCommand = Command(AccessPoliciesContract.Commands.CreateAccessPolicy(), accessPolicyState.participants.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(accessPolicyState)
            .addCommand(txCommand)

        progressTracker.currentStep = VERIFYING_TRANSACTION
        txBuilder.verify(serviceHub)

        progressTracker.currentStep = SIGNING_TRANSACTION
        // Sign the transaction.
        val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

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


@InitiatedBy(CreateAccessPolicyFlow::class)
class AccessPolicyCreationResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an access policy state" using (output is AccessPolicyState)
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}

