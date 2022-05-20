package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.InsuranceFlow
import nl.tno.federated.flows.InsuranceResponder
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.NewEventResponder
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import nl.tno.federated.states.Timestamp
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertFailsWith


class InsuranceFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode

    private val countriesInvolved = setOf("NL", "IS")

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
        val startedNodes = arrayListOf(a, b)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(InsuranceResponder::class.java) }
        startedNodes.forEach { it.registerInitiatedFlow(NewEventResponder::class.java) }

        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
        unmockkAll()
    }

    @Test
    fun `Insurance flow`() {
        // insert one event
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        insertEvent(goodUUID, transportMeanUUID)

        // insert another
        val goodUUID2 = UniqueIdentifier().id
        insertEvent(goodUUID2, transportMeanUUID)

        // insert insurance event
        val insuranceEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), "", Milestone.START, "", setOf("InsuranceEvent"))
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(insuranceEvent)

        val flowExecuted = InsuranceFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        assert(signedTxExec.references.size == 2)
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Insurance flow shouldn't fetch irrelevant event`() {
        // insert one event
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        insertEvent(goodUUID, transportMeanUUID)

        // insert another
        val goodUUID2 = UniqueIdentifier().id
        val transportMeanUUID2 = UniqueIdentifier().id
        insertEvent(goodUUID2, transportMeanUUID2)

        // insert insurance event
        val insuranceEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), "", Milestone.START, "", setOf("InsuranceEvent"))
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(insuranceEvent)

        val flowExecuted = InsuranceFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        assert(signedTxExec.references.size == 1)
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Insurance flow shouldn't fetch stopped event`() {
        // insert one event
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        insertEvent(goodUUID, transportMeanUUID)

        // insert another
        val goodUUID2 = UniqueIdentifier().id
        val txHash = insertEvent(goodUUID2, transportMeanUUID)

        // stop the first
        insertEvent(goodUUID, transportMeanUUID, milestone = Milestone.STOP)

        // insert insurance event
        val insuranceEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), "", Milestone.START, "", setOf("InsuranceEvent"))
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(insuranceEvent)

        val flowExecuted = InsuranceFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        val signedTxExec = futureExecuted.getOrThrow()
        val referencedStates = signedTxExec.references
        assert(referencedStates.size == 1)
        assert(referencedStates.single().txhash == txHash)
        signedTxExec.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Insurance flow without insurance event`() {
        val goodUUID = UniqueIdentifier().id
        val transportMeanUUID = UniqueIdentifier().id
        // insert insurance event
        val insuranceEvent = Event(setOf(goodUUID), setOf(transportMeanUUID), emptySet(), emptySet(), setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.ACTUAL)), "", Milestone.START, "", setOf("InsuranceEvent"))
        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(insuranceEvent)

        val flowExecuted = NewEventFlow("", countriesInvolved)
        val futureExecuted = a.startFlow(flowExecuted)
        network.runNetwork()

        assertFailsWith<IllegalArgumentException>("Illegal rdf") { futureExecuted.getOrThrow() }
    }

    private fun insertEvent(goodUUID: UUID, transportMeanUUID: UUID, milestone: Milestone = Milestone.START): SecureHash {
        val event = Event(
            setOf(goodUUID),
            setOf(transportMeanUUID),
            emptySet(),
            emptySet(),
            setOf(Timestamp(UniqueIdentifier().id.toString(), Date(), EventType.PLANNED)),
            "",
            milestone,
            "",
            emptySet()
        )

        every { GraphDBService.parseRDFToEvents(any()) } returns listOf(event)

        val flowStart = NewEventFlow("", countriesInvolved)
        val futureStart = a.startFlow(flowStart)
        network.runNetwork()

        val signedTxStart = futureStart.getOrThrow()
        signedTxStart.verifySignaturesExcept(a.info.singleIdentity().owningKey)
        return signedTxStart.id
    }

}