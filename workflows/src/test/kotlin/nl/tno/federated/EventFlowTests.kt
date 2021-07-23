package nl.tno.federated

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
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
import kotlin.test.assertFailsWith


class EventFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    val eCMRuriExample = "This is a URI example for an eCMR"

    val digitalTwinsWrong = listOf(
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.TRANSPORTMEAN),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER)
    )

    val digitalTwinsWrong2 = listOf(
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.TRANSPORTMEAN),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.LOCATION),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER)
    )

    val digitalTwinsTransportAndLocation = listOf(
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.TRANSPORTMEAN),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.LOCATION),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.OTHER)
    )

    val digitalTwinsGoodsAndTransport = listOf(
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.GOOD),
            DigitalTwinPair(UniqueIdentifier().id, PhysicalObject.TRANSPORTMEAN),
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
        startedNodes.forEach { it.registerInitiatedFlow(NewEventResponder::class.java) }
        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple flow transaction`() {

        val flow = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.START)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and stop event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.START)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.STOP)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start and stop event failed because no previous start event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.START)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsTransportAndLocation, eCMRuriExample, Milestone.STOP)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Two identical start events`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.START)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransport, eCMRuriExample, Milestone.START)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("There cannot be a previous equal start event") { futureStop.getOrThrow() }
    }

    @Test
    fun `Simple flow transaction 2`() {

        val flow = NewEventFlow(digitalTwinsTransportAndLocation, eCMRuriExample, Milestone.START)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail flow transaction because too many goods`() {

        val flow = NewEventFlow(digitalTwinsWrong, eCMRuriExample, Milestone.START)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `fail flow transaction because goods are linked to locations`() {

        val flow = NewEventFlow(digitalTwinsWrong2, eCMRuriExample, Milestone.START)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }
}