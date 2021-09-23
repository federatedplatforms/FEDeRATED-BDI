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
        assert(result.containsKey("12345"))
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
    fun `Parse event`() {
        val testRdfEvent = """
            @base <http://example.com/base/> .
            @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .
            @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> .
            @prefix dcterms: <http://purl.org/dc/terms/> .
            @prefix LogisticsRoles: <https://ontology.tno.nl/logistics/federated/LogisticsRoles#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
            @prefix owl: <http://www.w3.org/2002/07/owl#> .
            @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> .
            @prefix ReusableTags: <https://ontology.tno.nl/logistics/federated/ReusableTags#> .
            @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> .
            @prefix DigitalTwin: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .
            @prefix skos: <http://www.w3.org/2004/02/skos/core#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
            @prefix ex: <http://example.com/base#> .
            @prefix time: <http://www.w3.org/2006/time#> .
            @prefix dc: <http://purl.org/dc/elements/1.1/> .
            @prefix era: <http://era.europa.eu/ns#> .
            
            ex:Event-f99a5b51-039e-4f69-8238-2e11764f4835 a Event:Event, owl:NamedIndividual;
              rdfs:label "GateIn", "Actual gate in";
              Event:hasTimestamp "2019-10-18T10:22:00"^^xsd:dateTime;
              Event:hasDateTimeType Event:Actual;
              Event:involvesDigitalTwin ex:DigitalTwin-20ea72f7-90ed-42ff-ad9d-161593ba9fc5, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;
              Event:involvesBusinessTransaction ex:businessTransaction-ca6c007a-6ea9-4142-8234-6df28a2b1a82;
              Event:hasMilestone Event:Start;
              Event:hasSubmissionTimestamp "2019-10-18T11:18:25"^^xsd:dateTime .
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFtoEvent(testRdfEvent)

        println(parsedEvent.timestamps)

        assert(parsedEvent.goods.single().toString() == "a891b64d-d29f-4ef2-88ad-9ec4c88e0833")
        assert(parsedEvent.transportMean.single().toString() == "20ea72f7-90ed-42ff-ad9d-161593ba9fc5")
        // No check for location yet, as it is faked
        assert(parsedEvent.timestamps.keys.single().toString() == "ACTUAL")
        assert(parsedEvent.timestamps[EventType.ACTUAL].toString() == "Fri Jan 18 10:22:00 CET 2019")
        assert(parsedEvent.milestone == Milestone.START)
        assert(parsedEvent.id == "f99a5b51-039e-4f69-8238-2e11764f4835")
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