package nl.tno.federated

import net.corda.core.contracts.UniqueIdentifier
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.util.*


class GraphDBServiceTests {

    private val invalidSampleTTL = File("src/test/resources/SHACL_FAIL - TradelensEvents_ArrivalDeparture.ttl").readText()
    private val validSampleTtl = File("src/test/resources/TradelensEvents_SingleConsignment.ttl").readText()

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Query everything`() {
        val result = GraphDBService.queryEventIds()
        assert(result.contains("12345"))
    }

    @Test
    fun `Query event by ID`() {
        val result = GraphDBService.queryEventById("a2a19d3a-48b2-4d77-b4b2-0da12ba9ef89")
        assert(result.contains("a2a19d3a-48b2-4d77-b4b2-0da12ba9ef89"))
    }

    @Test
    fun `Query with a custom sparql query`() {
        val result = GraphDBService.generalSPARQLquery("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>\nPREFIX ex: <http://example.com/base#>\nSELECT ?subject ?object \nWHERE {\n?subject rdfs:label ?object .\nFILTER (?subject = ex:Event-a2a19d3a-48b2-4d77-b4b2-0da12ba9ef89)\n}")
        assert(result.contains("a2a19d3a-48b2-4d77-b4b2-0da12ba9ef89"))
    }

    @Test
    fun `Insert new event`() {
        val successfulInsertion = GraphDBService.insertEvent(validSampleTtl)
        assert(successfulInsertion)
    }

    @Test
    fun `Insert invalid event`() {
        val successfulInsertion = GraphDBService.insertEvent(invalidSampleTTL)
        assert(!successfulInsertion)
    }

    @Test
    fun `Verify repository is available`() {
        val resultTrue = GraphDBService.isRepositoryAvailable()
        assert(resultTrue)
    }

    @Ignore("Enable this when we find a shacl endpoint")
    @Test
    fun `Validate invalid event - nonsense RDF`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = invalidSampleTTL,
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(!GraphDBService.isDataValid(eventState))
    }

    @Ignore("Enable this when we find a shacl endpoint")
    @Test
    fun `Validate invalid event - valid RDF`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = validSampleTtl,
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(!GraphDBService.isDataValid(eventState))
    }

    @Ignore("Enable this when we can parse the whole event string")
    @Test
    fun `Validate valid event`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = "valid RDF matching this state", // TODO
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(GraphDBService.isDataValid(eventState))
    }

    @Test
    fun `Parse id from queried event`() {
        val result = GraphDBService.queryEventIds()
    }
}