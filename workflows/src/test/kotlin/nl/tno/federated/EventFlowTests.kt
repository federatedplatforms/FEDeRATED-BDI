package nl.tno.federated

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.UniqueIdentifier
import nl.tno.federated.flows.LoadResponder
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.LoadFlow
import nl.tno.federated.flows.CreateFlow
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.DigitalTwinType
import nl.tno.federated.states.Location
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


class EventFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))
        )
        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Brussels","BE")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        val startedNodes = arrayListOf(a, b)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(LoadResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple flow transaction`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Transaction fails when a unique identifier for a DT that doesn't exist is passed`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        val location = Location("BE", "Brussels")
        val flow = LoadFlow(listOf(UniqueIdentifier()), location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the acceptor`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `flow records a transaction in both parties' transaction storages`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `flow doesn't record a transaction unrelated to a party`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
        assertNull(c.services.validatedTransactions.getTransaction(signedTx.id))
    }

    @Test
    fun `flow rejects tx with no counterparty`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val createDTflow = CreateFlow(type, plate, owner)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("IT", "Milan")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `flow rejects invalid events`() {
        val location = Location("BE", "Brussels")
        val flow = LoadFlow(emptyList(), location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }
}