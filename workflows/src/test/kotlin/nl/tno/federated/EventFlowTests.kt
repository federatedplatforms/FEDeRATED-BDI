package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
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
import nl.tno.federated.states.EventState
import nl.tno.federated.states.Milestone
import nl.tno.federated.states.PhysicalObject
import org.junit.After
import org.junit.Before
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

    @Before
    fun setup() {
        mockkObject(GraphDBService)
        every { GraphDBService.isDataValid(any()) } returns true

        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","Brussels","BE"))),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters())
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
        val flow = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail Start event with invalid rdf`() {
        every { GraphDBService.isDataValid(any()) } returns false
        val flow = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { future.getOrThrow() }
    }


    @Test
    fun `Start event with transport and location`() {
        val flow = NewEventFlow(digitalTwinsTransportAndLocation, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start and stop event`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.STOP, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Stop event fails without start event`() {
        val flowStop = NewEventFlow(digitalTwinsTransportAndLocation, Date(), eCMRuriExample, Milestone.STOP, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Stop event fails without relevant start event`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsTransportAndLocation, Date(), eCMRuriExample, Milestone.STOP, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { futureStop.getOrThrow() }
    }

    @Test
    fun `Duplicate start events fail`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val secondStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart2 = a.startFlow(secondStart)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("There cannot be a previous equal start event") { futureStart2.getOrThrow() }
    }

    @Test
    fun `Simple flow transaction 2`() {

        val flow = NewEventFlow(digitalTwinsTransportAndLocation, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `fail flow transaction because too many goods`() {
        val flow = NewEventFlow(digitalTwinsWrong, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `fail flow transaction because goods are linked to locations`() {

        val flow = NewEventFlow(digitalTwinsWrong2, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `Simple flow start and update event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
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

    @Test
    fun `Simple flow start and update and execute event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
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

    @Test
    fun `Simple flow start and execution of stop event`() {
        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.STOP, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
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

    @Test
    fun `failed stop event after execution with just planned start event`() {

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val flowStop = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.STOP, UniqueIdentifier("KLM7915-20210801"), sampleEvent, countriesInvolved)
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

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, setOf("DE"))
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

        val flowStart = NewEventFlow(digitalTwinsGoodsAndTransport, Date(), eCMRuriExample, Milestone.START, UniqueIdentifier("KLM7915-20210801"), sampleEvent, setOf("DE", "FR"))
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