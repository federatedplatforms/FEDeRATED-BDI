package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.ExecuteEventFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.NewEventResponder
import nl.tno.federated.flows.UpdateEstimatedTimeFlow
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.EventState
import nl.tno.federated.states.Milestone
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

    private val digitalTwinsGoodsAndTransportStopEvent = """
            data:event-5b8699f1-4788-12ec-b5e4-5c879c8043a4 a event:Event, event:DischargeEvent;
                event:hasMilestone event:End;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-12-10T18:51:20Z"^^xsd:dateTime;
                event:involvesDigitalTwin data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1.
            
            data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.
            
            data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a DigitalTwin:Goods.
            """
    private val digitalTwinsTooManyGoodsAndTransportStartEvent = """
            data:event-5b8699f1-4788-11ec-b5e4-5c879c8043a4 a event:Event, event:DischargeEvent;
                event:hasMilestone event:Start;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-11-10T18:51:20Z"^^xsd:dateTime;
                event:involvesDigitalTwin data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d2, data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1.
            
            data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.
            
            data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a DigitalTwin:Goods.
            
            data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d2 a DigitalTwin:Goods.
            """

    private val digitalTwinsGoodsAndTransportAndLocationStartEvent = """
        data:event-5b856159-4788-11ec-a78e-5c879c8043a4 a event:Event, event:ArrivalEvent;
            event:hasMilestone event:Start;
            event:hasDateTimeType event:Actual;
            event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
            event:involvesDigitalTwin data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1;
            event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.   
            
            data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.     
            
            data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a DigitalTwin:Goods.
        """

    private val digitalTwinsTransportAndLocationStartEvent = """
        data:event-5b856159-4788-11ec-a78e-5c879c8043a4 a event:Event, event:ArrivalEvent;
            event:hasMilestone event:Start;
            event:hasDateTimeType event:Actual;
            event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
            event:involvesDigitalTwin data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a;
            event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.   
            
            data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.     
        """

    private val digitalTwinsGoodsAndTransportStartEvent = """
            data:event-5b8699f1-4788-11ec-b5e4-5c879c8043a4 a event:Event, event:DischargeEvent;
                event:hasMilestone event:Start;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-11-10T18:51:20Z"^^xsd:dateTime;
                event:involvesDigitalTwin data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1.
            
            data:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.
            
            data:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a DigitalTwin:Goods.
            """

    private val countriesInvolved = setOf("NL", "DE", "FR")

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
    }

    @Test
    fun `Start event with goods and transport`() {
        val flow = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail Start event with invalid rdf`() {
        every { GraphDBService.isDataValid(any()) } returns false
        val flow = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { future.getOrThrow() }
    }

    @Test
    fun `Start event with transport and location`() {
        val flow = NewEventFlow(digitalTwinsTransportAndLocationStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start and stop event`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransportStopEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Stop event fails without start event`() {
        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransportStopEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Stop event fails without relevant start event`() {
        val flowStart = NewEventFlow(digitalTwinsTransportAndLocationStartEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransportStopEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Duplicate start events fail`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val secondStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val futureStart2 = a.startFlow(secondStart)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("There cannot be a previous equal start event") { futureStart2.getOrThrow() }
    }

    @Test
    fun `Simple flow transaction 2`() {

        val flow = NewEventFlow(digitalTwinsTransportAndLocationStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail flow transaction because too many goods`() {
        val flow = NewEventFlow(digitalTwinsTooManyGoodsAndTransportStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `fail flow transaction because goods are linked to locations`() {

        val flow = NewEventFlow(digitalTwinsGoodsAndTransportAndLocationStartEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Ignore("Ignore until what to do with update and execution mechanics is decided")
    @Test
    fun `Simple flow start and update event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
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

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
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
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransportStopEvent, countriesInvolved)
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

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransportStopEvent, countriesInvolved)
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
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, setOf("DE"))

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
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransportStartEvent, setOf("DE", "FR"))
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