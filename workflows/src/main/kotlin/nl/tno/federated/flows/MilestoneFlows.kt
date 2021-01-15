package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import nl.tno.federated.contracts.MilestoneContract
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.Location
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import java.util.*

@InitiatingFlow
@StartableByRPC
class ArrivalFlow(val digitalTwins: List<UniqueIdentifier>, val location: Location) : FlowLogic<SignedTransaction>() {
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
        // val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB")) // METHOD 2

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION
        // Retrieving counterparties from location
        val counterParties = serviceHub.networkMapCache.allNodes.flatMap {it.legalIdentities}
            .filter { location.country == it.name.country } // to be changed later, meant for the poc only
            // Above we are matching the name of the country provided with the one of the known nodes,
            // and we are assuming every counterparty related to a country should receive the tx.

        val allParties = counterParties + serviceHub.myInfo.legalIdentities.first()

        // The input states are the DTs whose ID is passed as argument (i.e. those related to the milestone)
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = digitalTwins.map { it.id })
        val digitalTwinInputStates = serviceHub.vaultService.queryBy<DigitalTwinState>(criteria).states

        // Generate an unsigned transaction.
        val milestoneState = MilestoneState(MilestoneType.ARRIVE, digitalTwins, Date(), location, allParties)
        val digitalTwinsOutput = digitalTwinInputStates.map{ it.state.data }.map{ it.copy( lastMilestone = milestoneState.linearId)}

        val txCommand = Command(MilestoneContract.Commands.Arrive(), milestoneState.participants.map { it.owningKey })
        val txBuilder = TransactionBuilder(notary)
            .addOutputState(milestoneState, MilestoneContract.ID)
            .addCommand(txCommand)

        digitalTwinInputStates.forEach{txBuilder.addInputState( it )}
        digitalTwinsOutput.forEach{txBuilder.addOutputState( it )}

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


@InitiatedBy(ArrivalFlow::class)
class ArrivalResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        val signTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val milestoneOutput = stx.tx.outputs.filter { it.data is MilestoneState }.map { it.data }
                "There must be one milestone output." using (milestoneOutput.size == 1)
                val iou = milestoneOutput.single() as MilestoneState
                "I must be party to this milestone." using (iou.participants.contains(serviceHub.myInfo.legalIdentities.first()))
            }
        }
        val txId = subFlow(signTransactionFlow).id

        return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
    }
}
