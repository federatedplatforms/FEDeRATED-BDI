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
import kotlin.test.assertEquals


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
        val result = GraphDBService.queryEventById("3bc54ecf-c111-43b0-9437-09d6df211e37")
        assert(result.contains("3bc54ecf-c111-43b0-9437-09d6df211e37"))
    }

    @Test
    fun `Query with a custom sparql query`() {
        val result = GraphDBService.generalSPARQLquery("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\nPREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>\nPREFIX ex: <http://example.com/base#>\nSELECT ?subject ?object \nWHERE {\n?subject rdfs:label ?object .\nFILTER (?subject = ex:Event-3bc54ecf-c111-43b0-9437-09d6df211e37)\n}")
        assert(result.contains("3bc54ecf-c111-43b0-9437-09d6df211e37"))
    }

    @Test
    fun `Parse event - 1`() {
        val testRdfEvent = """
            :Event-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a a Event:Event, owl:NamedIndividual;
              rdfs:label "GateOut", "Planned gate out";
              Event:hasTimestamp "2020-01-25T18:00:00Z"^^xsd:dateTime;
              Event:hasDateTimeType Event:Planned;
              Event:involvesDigitalTwin :DigitalTwin-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a, :DigitalTwin-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesBusinessTransaction :businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesPhysicalInfrastructure :physicalInfrastructure-BEDEU01;
              Event:hasMilestone Event:End;
              Event:hasSubmissionTimestamp "2020-01-21T14:24:36"^^xsd:dateTime .
            
            :DigitalTwin-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a a DigitalTwin:TransportMeans,
                owl:NamedIndividual .
            
            :businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39 a businessService:Consignment,
                owl:NamedIndividual;
              businessService:consignmentCreationTime "2021-05-13T21:23:04"^^xsd:dateTime;
              businessService:involvedActor :LegalPerson-Maersk .
            
            :LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;
              businessService:actorName "Maersk" .
            
            :physicalInfrastructure-BEDEU01 a pi:Terminal, pi:LogisticalFunction, owl:NamedIndividual;
              rdfs:label "BEDEU01";
              pi:locatedAt :Location-BEDEG .
            
            :Location-BEDEG a pi:Location, owl:NamedIndividual;
              pi:cityName "Deurne, BE";
              pi:cityLoCode "BEDEG" .
            
            :DigitalTwin-6c7edb9c-cfee-4b0c-998d-435cca8eeb39 a DigitalTwin:Equipment, owl:NamedIndividual;
              DigitalTwin:containerID "XINU4010266" .

            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFtoEvent(testRdfEvent)

        assertEquals("6c7edb9c-cfee-4b0c-998d-435cca8eeb39", parsedEvent.goods.single().toString())
        assertEquals("5edc2423-d258-4002-8d6c-9fb3b1f6ff9a", parsedEvent.transportMean.single().toString())

        assertEquals("BEDEU01", parsedEvent.location.single().toString())

        assertEquals("PLANNED", parsedEvent.timestamps.keys.single().toString())

        assertEquals(1579975200000, parsedEvent.timestamps[EventType.PLANNED]!!.time)
        assertEquals(Milestone.STOP, parsedEvent.milestone)
        assertEquals("5edc2423-d258-4002-8d6c-9fb3b1f6ff9a", parsedEvent.id)
    }

    @Test
    fun `Parse event - 2`() {
        val testRdfEvent = """
            :Event-0c1e0ed5-636c-48b2-8f52-542e6f4d156a a Event:Event, owl:NamedIndividual;
              rdfs:label "GateIn", "Planned gate in";
              Event:hasTimestamp "2020-01-25T22:00:00Z"^^xsd:dateTime;
              Event:hasDateTimeType Event:Planned;
              Event:involvesDigitalTwin :DigitalTwin-0c1e0ed5-636c-48b2-8f52-542e6f4d156a, :DigitalTwin-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesBusinessTransaction :businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesPhysicalInfrastructure :physicalInfrastructure-BEANTMP;
              Event:hasMilestone Event:Start;
              Event:hasSubmissionTimestamp "2020-01-21T14:24:39Z"^^xsd:dateTime .
            
            :DigitalTwin-0c1e0ed5-636c-48b2-8f52-542e6f4d156a a DigitalTwin:TransportMeans,
                owl:NamedIndividual .
            
            :physicalInfrastructure-BEANTMP a pi:Terminal, pi:LogisticalFunction, owl:NamedIndividual;
              rdfs:label "BEANTMP";
              pi:locatedAt :Location-BEANR .
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFtoEvent(testRdfEvent)

        assertEquals("0c1e0ed5-636c-48b2-8f52-542e6f4d156a", parsedEvent.transportMean.single().toString())

        assertEquals("BEANTMP", parsedEvent.location.single().toString())

        assertEquals("PLANNED", parsedEvent.timestamps.keys.single().toString())

        assertEquals(1579989600000, parsedEvent.timestamps[EventType.PLANNED]!!.time)
        assertEquals(Milestone.START, parsedEvent.milestone)
        assertEquals("0c1e0ed5-636c-48b2-8f52-542e6f4d156a", parsedEvent.id)
    }

    @Test
    fun `Insert new event`() {
        val successfulInsertion = GraphDBService.insertEvent(validSampleTtl)
        assert(successfulInsertion)
    }

    @Ignore("Enable this when shacl validation is back working")
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
            location = listOf("random string"),
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
            location = listOf("random string"),
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
            location = listOf("random string"),
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
}