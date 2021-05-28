package nl.tno.federated

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.*
import nl.tno.federated.states.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


class EventNewFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    val eCMRuriExample = "This is a URI example for an eCMR"

    val digitalTwins = listOf(
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.TRANSPORTMEAN),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.LOCATION),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER)
    )

    @Before
    fun setup() {
        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","Brussels","BE"))),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Rotterdam","NL")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        val startedNodes = arrayListOf(a, b, c)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(NewEventNewResponder::class.java) }
        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple flow transaction`() {

        val flow = NewEventNewFlow(digitalTwins, eCMRuriExample, MilestoneNew.START)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }
}