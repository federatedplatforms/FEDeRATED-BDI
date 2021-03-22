package nl.tno.federated

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.*
import nl.tno.federated.states.*
import nl.tno.federated.states.Target
import org.junit.After
import org.junit.Before
import org.junit.Test


class AccessPoliciesFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    private val assetRefinement = AssetRefinement(
        "", "left", "=", "right"
    )

    private val target = Target("1234", "type", assetRefinement)

    private val idsAction = IdsAction(listOf(""))

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
        startedNodes.forEach { it.registerInitiatedFlow(AccessPolicyCreationResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple access policy flow creation`() {

        val flow = CreateAccessPolicyFlow(
            "", "", "", "", "",
            listOf(idsAction), target
        )

        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifyRequiredSignatures()
    }
}