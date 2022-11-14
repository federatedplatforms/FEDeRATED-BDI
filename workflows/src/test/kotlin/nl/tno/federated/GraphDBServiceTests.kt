package nl.tno.federated

import com.google.common.collect.testing.Helpers.assertContainsAllOf
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.junit.BeforeClass
import org.junit.Assert.assertFalse
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals


/**
 * This test only works with a running GraphDB instance (see: database.properties)
 */
class GraphDBServiceTests : GraphDBTestContainersSupport() {

    private val invalidSampleTTL = File("src/test/resources/SHACL_FAIL - TradelensEvents_ArrivalDeparture.ttl").readText()

    companion object {

        private val validSampleTtl = File("src/test/resources/correct-event.ttl").readText()

        @JvmStatic
        @BeforeClass
        fun setup() {
            // Override database.properties with docker properties
            System.setProperty("triplestore.host",  graphDB.host)
            System.setProperty("triplestore.port",  graphDB.firstMappedPort?.toString() ?: "7200")

            // 1. Create repositories
            GraphDBService.createRemoteRepositoryFromConfig("bdi-repository-config.ttl")
            GraphDBService.createRemoteRepositoryFromConfig("private-repository-config.ttl")
            // 2. Insert data
            GraphDBService.insertEvent(validSampleTtl, false)
        }
    }

    @Test
    fun `Query everything`() {
        val weKnowEvent = GraphDBService.queryEventById("b0efeca7-7b33-4d4e-8a5e-1d33b75a3e19")
        assertFalse(GraphDBService.isQueryResultEmpty(weKnowEvent))
    }

    @Test
    fun `Query event by ID`() {
        val result = GraphDBService.queryEventById("b0efeca7-7b33-4d4e-8a5e-1d33b75a3e19")
        assertFalse(GraphDBService.isQueryResultEmpty(result))
    }

    @Test
    fun `Parse event - 1`() {
        val testRdfEvent = """
            @base <http://example.com/base/> . 
            @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
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
        val parsedEvents = GraphDBService.parseRDFToEvents(testRdfEvent)
        assertEquals(1, parsedEvents.size)
        val parsedEvent = parsedEvents.single()

        assertEquals("6c7edb9c-cfee-4b0c-998d-435cca8eeb39", parsedEvent.goods.single().toString())
        assertEquals("5edc2423-d258-4002-8d6c-9fb3b1f6ff9a", parsedEvent.transportMean.single().toString())

        assertEquals("BEDEU01", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 2`() {
        val testRdfEvent = """
                        @base <http://example.com/base/> . 
                        @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
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

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).first()

        assertEquals("0c1e0ed5-636c-48b2-8f52-542e6f4d156a", parsedEvent.transportMean.single().toString())

        assertEquals("BEANTMP", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 3`() {
        val testRdfEvent = """
                        @base <http://example.com/base/> . 
                        @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
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
            :Event-5b856159-4788-11ec-a78e-5c879c8043a4 a Event:Event, Event:ArrivalEvent;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Actual;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin :DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a;
                Event:involvesPhysicalInfrastructure :PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.
            
            :DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).first()

        assertEquals("c5836199-8809-3930-9cf8-1d14a54d242a", parsedEvent.transportMean.single().toString())

        assertEquals("b4d51938-5ae5-330d-af2e-a198dd2c16ab", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 4`() {
        val testRdfEvent = """
                        @base <http://example.com/base/> . 
                        @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
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
            :Event-5b8699f1-4788-11ec-b5e4-5c879c8043a4 a Event:Event, Event:DischargeEvent;
                Event:hasMilestone Event:End;
                Event:hasDateTimeType Event:Planned;
                Event:hasTimestamp "2021-11-10T18:51:20Z"^^xsd:dateTime;
                Event:involvesDigitalTwin :DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, :DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1.
            
            :DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a DigitalTwin:TransportMeans.
            
            :DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a DigitalTwin:Goods.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("c5836199-8809-3930-9cf8-1d14a54d242a", parsedEvent.transportMean.single().toString())
        assertEquals("ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1", parsedEvent.goods.single().toString())
    }

    @Test
    fun `Parse event - 5`() {
        val testRdfEvent = """
            @base <http://example.com/base/> . 
            @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
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
            
            :Event-f223c17c-c3ab-4871-9b78-3536d121925c a Event:Event, Event:ArrivalEvent;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Actual;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin :DigitalTwin-43691f54-091c-4378-a176-b730a4966996;
                Event:involvesPhysicalInfrastructure :PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.
            
            :DigitalTwin-43691f54-091c-4378-a176-b730a4966996 a DigitalTwin:TransportMeans.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("43691f54-091c-4378-a176-b730a4966996", parsedEvent.transportMean.single().toString())
        assertEquals("b4d51938-5ae5-330d-af2e-a198dd2c16ab", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 6`() {
        val testRdfEvent = """
            @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
            @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
            @prefix owl: <http://www.w3.org/2002/07/owl#>.
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
            @prefix sd: <http://www.w3.org/ns/sparql-service-description#>.
            @prefix event: <https://ontology.tno.nl/logistics/federated/Event#>.
            @prefix dt: <https://ontology.tno.nl/logistics/federated/digitalTwin#>.
            @prefix bs: <https://ontology.tno.nl/logistics/federated/businessService#>.
            @prefix pi: <https://ontology.tno.nl/logistics/federated/physicalInfrastructure#>.
            @prefix cl: <https://ontology.tno.nl/logistics/federated/Classifications#>.
            
            :event-41068e69-4be0-11ec-a52a-5c879c8043a5 a event:Event, event:ArrivalEvent;
                event:hasMilestone event:Start;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                event:involvesDigitalTwin :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f;
                event:involvesPhysicalInfrastructure :PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("dce1774a-b2a1-338e-bd56-1902c57f836f", parsedEvent.otherDigitalTwins.single().toString())
        assertEquals("be989099-2e25-3259-975b-9f17c63b0281", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 7 - milliseconds`() {
        val testRdfEvent = """
            @prefix data: <https://ontology.tno.nl/logistics/federated/tradelens#> .
            @prefix dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .
            @prefix event: <https://ontology.tno.nl/logistics/federated/Event#> .
            @prefix owl: <http://www.w3.org/2002/07/owl#> .
            @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .
            @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
            
            data:DigitalTwin-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f a owl:NamedIndividual, dt:TransportMeans .
            
            data:PhysicalInfrastructure-INNSA a "http://www.w3.org/2002/07/owl#NamedIndivudal~iri",
                pi:Location .
            
            data:event-10d7fd0d-7a26-4b83-be1a-9c2606cebdc9 a "http://www.w3.org/2002/07/owl#NamedIndividual",
                event:Event, event:LoadEvent;
              event:hasDateTimeType event:Actual;
              event:hasMilestone event:Start;
              event:hasSubmissionTimestamp "2022-09-09T12:20:15.332Z"^^xsd:dateTime;
              event:hasTimestamp "2022-09-09T18:20:15.329Z"^^xsd:dateTime;
              event:involvesBusinessTransaction "https://ontology.tno.nl/logistics/federated/tradelens#BusinessTransaction-bc71cb37-f2a9-4844-8d8b-891c8bf75521";
              event:involvesDigitalTwin data:DigitalTwin-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f;
              event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-INNSA .

            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("bf93dc6a-1f04-4dec-ba0d-3ba987b2723f", parsedEvent.transportMean.single().toString())
        assertEquals("INNSA", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - TL`() {
        val testRdfEvent = """
                @prefix data: <https://ontology.tno.nl/logistics/federated/tradelens#> .
                @prefix dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .
                @prefix event: <https://ontology.tno.nl/logistics/federated/Event#> .
                @prefix owl: <http://www.w3.org/2002/07/owl#> .
                @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .
                @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
                @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
                
                data:DigitalTwin-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f a owl:NamedIndividual, dt:TransportMeans .
                
                data:PhysicalInfrastructure-INNSA a "http://www.w3.org/2002/07/owl#NamedIndivudal~iri",
                    pi:Location .
                
                data:event-10d7fd0d-7a26-4b83-be1a-9c2606cebdc9 a "http://www.w3.org/2002/07/owl#NamedIndividual",
                    event:Event, event:LoadEvent;
                  event:hasDateTimeType event:Actual;
                  event:hasMilestone event:Start;
                  event:hasSubmissionTimestamp "2022-09-09T12:20:15Z"^^xsd:dateTime;
                  event:hasTimestamp "2022-09-09T18:20:15Z"^^xsd:dateTime;
                  event:involvesBusinessTransaction "https://ontology.tno.nl/logistics/federated/tradelens#BusinessTransaction-bc71cb37-f2a9-4844-8d8b-891c8bf75521";
                  event:involvesDigitalTwin data:DigitalTwin-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f;
                  event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-INNSA .
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("bf93dc6a-1f04-4dec-ba0d-3ba987b2723f", parsedEvent.transportMean.single().toString())
        assertEquals("INNSA", parsedEvent.location.single().toString())
    }

    @Test
    fun `Insert new event`() {
        val successfulInsertion = GraphDBService.insertEvent(validSampleTtl, false)
        assert(successfulInsertion)
    }

    @Ignore("Enable this when shacl validation is back working")
    @Test
    fun `Insert invalid event`() {
        val successfulInsertion = GraphDBService.insertEvent(invalidSampleTTL, false)
        assert(!successfulInsertion)
    }

    @Test
    fun `parse labels`() {
        val testRdfEvent = """
            @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
            @prefix owl: <http://www.w3.org/2002/07/owl#>.
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
            @prefix sd: <http://www.w3.org/ns/sparql-service-description#>.
            @prefix event: <https://ontology.tno.nl/logistics/federated/Event#>.
            @prefix dt: <https://ontology.tno.nl/logistics/federated/digitalTwin#>.
            @prefix bs: <https://ontology.tno.nl/logistics/federated/businessService#>.
            @prefix pi: <https://ontology.tno.nl/logistics/federated/physicalInfrastructure#>.
            @prefix cl: <https://ontology.tno.nl/logistics/federated/Classifications#>.
            
            :event-41068e69-4be0-11ec-a52a-5c879c8043a5 a event:Event;
                rdfs:label "GateOut test"^^xsd:string, "insuranceEvent"^^xsd:string;
                event:hasMilestone event:Start;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                event:involvesDigitalTwin :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f;
                event:involvesPhysicalInfrastructure :PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals(2, parsedEvent.labels.size)
        assertContainsAllOf(parsedEvent.labels, "GateOut test", "insuranceEvent")
    }

    @Test
    fun `parse other dt mean`() {
        val testRdfEvent = """
            @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
            @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
            @prefix owl: <http://www.w3.org/2002/07/owl#>.
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#>.
            @prefix sd: <http://www.w3.org/ns/sparql-service-description#>.
            @prefix event: <https://ontology.tno.nl/logistics/federated/Event#>.
            @prefix dt: <https://ontology.tno.nl/logistics/federated/digitalTwin#>.
            @prefix bs: <https://ontology.tno.nl/logistics/federated/businessService#>.
            @prefix pi: <https://ontology.tno.nl/logistics/federated/physicalInfrastructure#>.
            @prefix cl: <https://ontology.tno.nl/logistics/federated/Classifications#>.
            
            :event-41068e69-4be0-11ec-a52a-5c879c8043a5 a event:Event;
                rdfs:label "GateOut test"^^xsd:string, "insuranceEvent"^^xsd:string;
                event:hasMilestone event:Start;
                event:hasDateTimeType event:Planned;
                event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                event:involvesDigitalTwin :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f;
                event:involvesPhysicalInfrastructure :PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            :DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = GraphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals(1, parsedEvent.otherDigitalTwins.size)
        assertEquals(UUID.fromString("dce1774a-b2a1-338e-bd56-1902c57f836f"), parsedEvent.otherDigitalTwins.single())
    }
}