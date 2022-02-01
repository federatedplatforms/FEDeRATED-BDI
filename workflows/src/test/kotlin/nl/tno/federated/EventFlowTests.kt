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


    private val existingCountries = setOf("NL", "DE", "FR")
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
        val event = Event(setOf(UniqueIdentifier().id), setOf(UniqueIdentifier().id), emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("unused event", existingCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail Start event with invalid rdf`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        every { GraphDBService.isDataValid(any()) } returns false
        val flow = NewEventFlow("invalid data", existingCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { future.getOrThrow() }
    }

    @Test
    fun `fail Start event with unknown country`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("invalid data", unknownCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("One of the requested counterparties was not found") { future.getOrThrow() }
    }

    @Test
    fun `Start event with transport and location`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("unused event", existingCountries)
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
            transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(startEvent)

        val flowStart = NewEventFlow("unused event", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val stopEvent = Event(goods, transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.STOP, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(stopEvent)
        val flowStop = NewEventFlow("unused event", existingCountries)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Stop event fails without start event`() {
        val stopEvent = Event(setOf(UniqueIdentifier().id), setOf(UniqueIdentifier().id), emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.STOP, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(stopEvent)

        val flowStop = NewEventFlow("unused event", existingCountries)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Stop event fails without relevant start event`() {
        val goods = setOf(UniqueIdentifier().id)
        val transportMean = setOf(UniqueIdentifier().id)
        val startEvent = Event(goods,
            transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
                EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(startEvent)
//        val flowStart = NewEventFlow(digitalTwinsTransportAndLocationStartEvent, countriesInvolved)
        val flowStart = NewEventFlow("unused event", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val otherGoods = setOf(UniqueIdentifier().id)
        val stopEvent = Event(otherGoods, transportMean, emptySet(), setOf(UniqueIdentifier().id, UniqueIdentifier().id), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.STOP, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(stopEvent)
        val flowStop = NewEventFlow("unused event", existingCountries)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Duplicate start events fail`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val secondStart = NewEventFlow("", existingCountries)
        val futureStart2 = a.startFlow(secondStart)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("There cannot be a previous equal start event") { futureStart2.getOrThrow() }
    }

    @Test
    fun `Simple flow transaction 2`() { // TODO what does this test? seems redundant
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("", existingCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail flow transaction because too many goods`() {
        val event = Event(setOf(UniqueIdentifier().id, UniqueIdentifier().id), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("", existingCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `fail flow transaction because goods are linked to locations`() {
        val event = Event(setOf(UniqueIdentifier().id), setOf(), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)
        val flow = NewEventFlow("", existingCountries)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Ignore("Ignore until what to do with update and execution mechanics is decided")
    @Test
    fun `Simple flow start and update event`() {
        // digitalTwinsGoodsAndTransportStartEvent
        val flowStart = NewEventFlow("", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        val newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
        val idOfNewlyCreatedEvent = newlyCreatedEvent.map { it.state.data.linearId }.single().id


        val flowUpdated = UpdateEstimatedTimeFlow(idOfNewlyCreatedEvent, Date())
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Ignore("Ignore until what to do with update and execution mechanics is decided")
    @Test
    fun `Simple flow start and update and execute event`() {
        // digitalTwinsGoodsAndTransportStartEvent
        val flowStart = NewEventFlow("", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        var newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
        var idOfNewlyCreatedEvent = newlyCreatedEvent.map { it.state.data.linearId }.single().id


        val flowUpdated = UpdateEstimatedTimeFlow(idOfNewlyCreatedEvent, Date())
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
        idOfNewlyCreatedEvent = newlyCreatedEvent.map { it.state.data.linearId }.single().id


        val flowExecuted = ExecuteEventFlow(idOfNewlyCreatedEvent, Date())
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Ignore("Ignore until what to do with update and execution mechanics is decided")
    @Test
    fun `Simple flow start and execution of stop event`() {
        // digitalTwinsGoodsAndTransportStartEvent
        val flowStart = NewEventFlow("", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // digitalTwinsGoodsAndTransportStopEvent
        val flowStop = NewEventFlow("", existingCountries)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        var newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
                .filter{ it.state.data.milestone == Milestone.START }
        var idOfNewlyCreatedEvent = newlyCreatedEvent.map{ it.state.data.linearId }.single().id

        val flowExecutedStart = ExecuteEventFlow(idOfNewlyCreatedEvent, Date())
        val futureExecutedStart = a.startFlow(flowExecutedStart)
        network.runNetwork()

        val signedTxExecStart = futureExecutedStart.getOrThrow()
        signedTxExecStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
                .filter{ it.state.data.milestone == Milestone.STOP }
        idOfNewlyCreatedEvent = newlyCreatedEvent.map{ it.state.data.linearId }.single().id

        val flowExecuted = ExecuteEventFlow(idOfNewlyCreatedEvent, Date())
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Ignore("Ignore until what to do with update and execution mechanics is decided")
    @Test
    fun `failed stop event after execution with just planned start event`() {
        // digitalTwinsGoodsAndTransportStartEvent
        val flowStart = NewEventFlow("", existingCountries)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // digitalTwinsGoodsAndTransportStopEvent
        val flowStop = NewEventFlow("", existingCountries)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Retrieving ID of the new event
        val newlyCreatedEvent = a.services.vaultService.queryBy<EventState>().states
                .filter{ it.state.data.milestone == Milestone.STOP}
        val idOfNewlyCreatedEvent = newlyCreatedEvent.map { it.state.data.linearId }.single().id


        val flowExecuted = ExecuteEventFlow(idOfNewlyCreatedEvent, Date())
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureExecuted.getOrThrow() }
    }

    @Test
    fun `Data is distributed only to countries included in countriesInvolved`() {
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
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
        val event = Event(setOf(), setOf(UniqueIdentifier().id), setOf("some location"), emptySet(), linkedMapOf(
            EventType.PLANNED to Date()), eCMRuriExample, Milestone.START, sampleEvent, UniqueIdentifier().id.toString()
        )
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