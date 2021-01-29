package nl.tno.federated

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.CreateFlow
import nl.tno.federated.flows.CreationResponder
import nl.tno.federated.states.DigitalTwinType
import org.junit.After
import org.junit.Before
import org.junit.Test


class DigitalTwinFlowTests {

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
        startedNodes.forEach { it.registerInitiatedFlow(CreationResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the acceptor`() {
        val type = DigitalTwinType.TRUCK
        val plate = "N1C3PL4T3"
        val owner = "Best Business"

        val flow = CreateFlow(type, plate, owner)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifyRequiredSignatures()
    }

}