package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.DigitalTwinContract
import nl.tno.federated.states.Cargo
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.PhysicalObject
import nl.tno.federated.states.Truck


//////  CARGO CREATION  /////

@InitiatingFlow
@StartableByRPC
class CreateCargoFlow(
    private val cargo : Cargo
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


        /// Generate an unsigned transaction ///

        // Creating DT state
        val digitalTwinState = DigitalTwinState(PhysicalObject.CARGO, cargo = cargo, participants = listOf(me))

        // Generating tx
        val txCommand = Command(DigitalTwinContract.Commands.CreateCargo(), digitalTwinState.participants.map { it.owningKey })
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


@InitiatedBy(CreateCargoFlow::class)
class CargoCreationResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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

//////  TRUCK CREATION  /////


@InitiatingFlow
@StartableByRPC
class CreateTruckFlow(
    private val truck: Truck
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


        /// Generate an unsigned transaction ///

        // Creating DT state
        val digitalTwinState = DigitalTwinState(PhysicalObject.TRANSPORTMEAN, truck = truck, participants = listOf(me),
            linearId = UniqueIdentifier(externalId = truck.licensePlate)
        )

        // Generating tx
        val txCommand = Command(DigitalTwinContract.Commands.CreateTruck(), digitalTwinState.participants.map { it.owningKey })
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


@InitiatedBy(CreateTruckFlow::class)
class TruckCreationResponder(val counterpartySession: FlowSession) : FlowLogic<SignedTransaction>() {
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

