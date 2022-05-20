package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.*
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class EventFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode
    lateinit var d: StartedMockNode

    private val eCMRuriExample = "This is a URI example for an eCMR"

    private val digitalTwinsWrong = listOf(
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.GOOD),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.GOOD),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.TRANSPORTMEAN),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER)
    )

    private val digitalTwinsWrong2 = listOf(
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.GOOD),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.TRANSPORTMEAN),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.LOCATION),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER)
    )

    private val digitalTwinsTransportAndLocation = listOf(
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.TRANSPORTMEAN),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.LOCATION),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER)
    )

    private val digitalTwinsGoodsAndTransport = listOf(
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.GOOD),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.TRANSPORTMEAN),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER),
        DigitalTwinPair(UniqueIdentifier().id.toString(), PhysicalObject.OTHER)
    )

    private val sampleEvent = ""


    private val countriesInvolved = setOf("NL", "DE", "FR")
    private val unknownCountries = setOf("NL", "ZZ")

    @Before
    fun setup() {
        mockkObject(GraphDBService)
        every { GraphDBService.isDataValid(any()) } returns true
        every { GraphDBService.insertEvent(any()) } returns true

        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","Brussels","BE"))),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyA","Reykjavik","IS")))
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Rotterdam","NL")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        d = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyD","Paris","FR")))
        val startedNodes = arrayListOf(a, b, c, d)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(NewEventResponder::class.java) }
        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
        unmockkAll()
    }

    @Test
    fun `Start event with goods and transport`() {
        val event = Event(setOf(UniqueIdentifier().id), setOf(UniqueIdentifier().id), emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("unused event", countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail Start event with invalid rdf`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        every { GraphDBService.isDataValid(any()) } returns false
        val flow = NewEventFlow("invalid data", countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { future.getOrThrow() }
    }

    @Test
    fun `fail Start event with unknown country`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("invalid data", unknownCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("One of the requested counterparties was not found") { future.getOrThrow() }
    }

    @Test
    fun `Start event with transport and location`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("unused event", countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start and stop event`() {
        val goods = setOf(UniqueIdentifier().id)
        val transportMean = setOf(UniqueIdentifier().id)
        val startEvent = Event(goods,
            transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(startEvent)

        val flowStart = NewEventFlow("unused event", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val stopEvent = Event(goods, transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.STOP, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(stopEvent)
        val flowStop = NewEventFlow("unused event", countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Duplicate start events fail`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val secondStart = NewEventFlow("", countriesInvolved)
        val futureStart2 = a.startFlow(secondStart)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("There cannot be a previous equal start event") { futureStart2.getOrThrow() }
    }

    @Test
    fun `Simple flow transaction 2`() { // TODO what does this test? seems redundant
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("", countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and update event`() {
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        val event = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)

        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // update of the event
        val updatedEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ESTIMATED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(updatedEvent)


        val flowUpdated = NewEventFlow("", countriesInvolved)
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and update and execute event`() {
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        val event = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)

        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // update of the event
        val updatedEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ESTIMATED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(updatedEvent)


        val flowUpdated = NewEventFlow("", countriesInvolved)
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // execute event
        val executedEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(executedEvent)

        val flowExecuted = NewEventFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and execution of stop event`() {

        // Create first START event
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        val event = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)

        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Create corresponding STOP event
        val stopEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.STOP, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(stopEvent)

        val flowStopped = NewEventFlow("", countriesInvolved)
        val futureUpdated = a.startFlow(flowStopped)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Execute START event
        val executedStartEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(executedStartEvent)

        val flowStartExecuted = NewEventFlow("", countriesInvolved)
        val futureStartExecuted = a.startFlow(flowStartExecuted)
        network.runNetwork()

        val signedTxStartExec = futureStartExecuted.getOrThrow()
        signedTxStartExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Execute STOP event
        val executedEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), eCMRuriExample, Milestone.STOP, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(executedEvent)

        val flowExecuted = NewEventFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Data is distributed only to countries included in countriesInvolved`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flowStart = NewEventFlow("", setOf("DE"))

        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val eventStateInNetherlands = b.services.vaultService.queryBy<EventState>().states
                .filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInGermany = c.services.vaultService.queryBy<EventState>().states
                .filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        assertTrue { eventStateInGermany.isNotEmpty() || eventStateInNetherlands.isEmpty() }
    }

    @Test
    fun `Data is distributed only to countries included in countriesInvolved - 2`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)), eCMRuriExample, Milestone.START, sampleEvent)
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flowStart = NewEventFlow("unused event", setOf("DE", "FR"))
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val eventStateInNetherlands = b.services.vaultService.queryBy<EventState>().states
                .filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInGermany = c.services.vaultService.queryBy<EventState>().states
                .filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInFrance = d.services.vaultService.queryBy<EventState>().states
                .filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        assertTrue {
                    eventStateInGermany.isNotEmpty() ||
                    eventStateInFrance.isNotEmpty() ||
                    eventStateInNetherlands.isEmpty()
        }
    }
}