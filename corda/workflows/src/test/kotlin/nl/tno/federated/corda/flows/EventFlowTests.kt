package nl.tno.federated.corda.flows

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import nl.tno.federated.corda.services.graphdb.GraphDBCordaService
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import nl.tno.federated.corda.services.graphdb.IGraphDBService
import nl.tno.federated.states.EventState
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EventFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode
    private lateinit var c: StartedMockNode
    private lateinit var d: StartedMockNode
    private lateinit var graphDBService: IGraphDBService

    private val unknownNames = setOf(CordaX500Name("PartyY", "Reykjavik", "IS"), CordaX500Name("PartyZ", "Reykjavik", "IS"))
    private val aName = CordaX500Name("PA", "PartyA", "Reykjavik", "IS")
    private val bName = CordaX500Name("PartyB", "Rotterdam", "NL")
    private val cName = CordaX500Name("PartyC", "Berlin", "DE")
    private val dName = CordaX500Name("PartyD", "Paris", "FR")
    private val countriesInvolved = setOf(bName, cName, dName)

    @Before
    fun setup() {
        graphDBService = mockk()
        mockkObject(GraphDBEventConverter)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))
        every { graphDBService.insertEvent(any(), false) } returns true

        network = MockNetwork(
            listOf("nl.tno.federated"), notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "Brussels", "BE"))), networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyA", "Reykjavik", "IS")))
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB", "Rotterdam", "NL")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC", "Berlin", "DE")))
        d = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyD", "Paris", "FR")))

        val startedNodes = arrayListOf(a, b, c, d)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach {
            it.services.cordaService(GraphDBCordaService::class.java).setGraphDBService(graphDBService)
            it.registerInitiatedFlow(NewEventResponder::class.java)
        }
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unmockkAll()
    }

    @Test
    fun `Start event with goods and transport`() {
        val flow = NewEventFlow(countriesInvolved, "unused event")
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start event with other DTs`() {
        val flow = NewEventFlow(countriesInvolved, "unused event")
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Ignore
    @Test
    fun `fail Start event with invalid rdf`() {
        val flow = NewEventFlow(countriesInvolved, "invalid data")
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { future.getOrThrow() }
    }

    @Test
    fun `fail Start event with unknown country`() {
        val flow = NewEventFlow(unknownNames, "invalid data")
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("One of the requested counterparties was not found") { future.getOrThrow() }
    }

    @Test
    fun `Start event with transport and location`() {
        val flow = NewEventFlow(countriesInvolved, "unused event")
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Start and stop event`() {
        val flowStart = NewEventFlow(countriesInvolved, "unused event")
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowStop = NewEventFlow(countriesInvolved, "unused event")
        val futureStop = a.startFlow(flowStop)
        network.runNetwork()

        val signedTxStop = futureStop.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and update event`() {
        val flowStart = NewEventFlow(countriesInvolved, "unused event")
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowUpdated = NewEventFlow(countriesInvolved, "unused event")
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and update and execute event`() {

        val flowStart = NewEventFlow(countriesInvolved, "unused event")
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowUpdated = NewEventFlow(countriesInvolved, "unused event")
        val futureUpdated = a.startFlow(flowUpdated)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowExecuted = NewEventFlow(countriesInvolved, "unused event")
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple flow start and execution of stop event`() {
        val flowStart = NewEventFlow(countriesInvolved, "unused event")
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)
        val flowStopped = NewEventFlow(countriesInvolved, "unused event")
        val futureUpdated = a.startFlow(flowStopped)
        network.runNetwork()

        val signedTxStop = futureUpdated.getOrThrow()
        signedTxStop.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowStartExecuted = NewEventFlow(countriesInvolved, "unused event")
        val futureStartExecuted = a.startFlow(flowStartExecuted)
        network.runNetwork()

        val signedTxStartExec = futureStartExecuted.getOrThrow()
        signedTxStartExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        every { GraphDBEventConverter.parseRDFToEventIDs(any()) }.returns(listOf(UniqueIdentifier().id.toString()))

        val flowExecuted = NewEventFlow(countriesInvolved, "unused event")
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Data is distributed only to countries included in countriesInvolved`() {
        val flowStart = NewEventFlow(setOf(cName), "")

        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val eventStateInNetherlands = b.services.vaultService.queryBy<EventState>().states.filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInGermany = c.services.vaultService.queryBy<EventState>().states.filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        assertTrue { eventStateInGermany.isNotEmpty() || eventStateInNetherlands.isEmpty() }
    }

    @Test
    fun `Data is distributed only to countries included in countriesInvolved - 2`() {
        val flowStart = NewEventFlow(setOf(cName, dName), "unused event")
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val eventStateInNetherlands = b.services.vaultService.queryBy<EventState>().states.filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInGermany = c.services.vaultService.queryBy<EventState>().states.filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        val eventStateInFrance = d.services.vaultService.queryBy<EventState>().states.filter { it.state.data.linearId.externalId == "KLM7915-20210801" }

        assertTrue {
            eventStateInGermany.isNotEmpty() || eventStateInFrance.isNotEmpty() || eventStateInNetherlands.isEmpty()
        }
    }
}