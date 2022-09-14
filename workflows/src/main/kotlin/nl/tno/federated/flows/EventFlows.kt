package nl.tno.federated.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import nl.tno.federated.contracts.EventContract
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.services.GraphDBService.generalSPARQLquery
import nl.tno.federated.services.GraphDBService.insertEvent
import nl.tno.federated.services.GraphDBService.queryEventById
import nl.tno.federated.states.EventState
import nl.tno.federated.states.PhysicalObject

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
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Stage 1.
        progressTracker.currentStep = GENERATING_TRANSACTION

        // Retrieving counterparties (sending to all nodes, for now)
        val me = serviceHub.myInfo.legalIdentities.first()
        val counterPartiesAndMe : MutableList<Party?> = mutableListOf()
        countriesInvolved.forEach { involvedCountry ->
            counterPartiesAndMe.add(serviceHub.networkMapCache.allNodes.flatMap { it.legalIdentities }
                .firstOrNull { it.name.country == involvedCountry })
        }
        require(!counterPartiesAndMe.contains(null)) { "One of the requested counterparties was not found"}
        val counterParties = counterPartiesAndMe.filter { it!!.owningKey != me.owningKey }

        val allParties = counterParties.map { it!! } + mutableListOf(notary, me)

        val newEvent = GraphDBService.parseRDFToEvents(fullEvent).first()

                val newEventState = EventState(
                        goods = newEvent.goods,
                        transportMean = newEvent.transportMean,
                        location = newEvent.location,
                        otherDigitalTwins = newEvent.otherDigitalTwins,
                        timestamps = newEvent.timestamps,
                        ecmruri = newEvent.ecmruri,
                        milestone = newEvent.milestone,
                        fullEvent = fullEvent,
                        participants = allParties - notary
                )

                val txBuilder = TransactionBuilder(notary)
                        .addOutputState(newEventState, EventContract.ID)
                        .addCommand(EventContract.Commands.Create(), newEventState.participants.map { it.owningKey })

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

                require(insertEvent(newEventState.fullEvent, false)) { "Unable to insert event data into the triple store."}
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
                require(insertEvent(outputState.fullEvent, false)) { "Unable to insert event data into the triple store."}
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