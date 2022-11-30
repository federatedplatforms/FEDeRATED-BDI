package nl.tno.federated.corda.services.graphdb

import com.google.common.collect.testing.Helpers.assertContainsAllOf
import net.corda.core.internal.randomOrNull
import nl.tno.federated.services.PrefixHandlerTTLGenerator
import nl.tno.federated.corda.services.TTLRandomGenerator
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals


/**
 * This test only works with a running GraphDB instance (see: database.properties)
 */
class GraphDBServiceTests : GraphDBTestContainersSupport() {

    companion object {
        private val invalidSampleTTL = File("src/test/resources/SHACL_FAIL - TradelensEvents_ArrivalDeparture.ttl").readText()
        private val generatedTTL = TTLRandomGenerator().generateRandomEvents()
        private val validSampleTtl = generatedTTL.constructedTTL
        private val eventPool = generatedTTL.eventIdentifiers
    }

    @Before
    fun before() {
        graphDBService.insertEvent(validSampleTtl, false)
    }

    @Test
    fun `Query everything`() {
        val everythingQueryResult = graphDBService.queryEventIds()
        assertFalse(graphDBService.isQueryResultEmpty(everythingQueryResult))
    }

    @Test
    fun `Query event by ID`() {
        val randomEvent = eventPool.randomOrNull()!!
        val result = graphDBService.queryEventById(randomEvent)
        assertFalse(graphDBService.isQueryResultEmpty(result))
    }


    @Test
    fun `Parse event - 1`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            ex:Event-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a a Event:Event, owl:NamedIndividual;
              rdfs:label "GateOut", "Planned gate out";
              Event:hasTimestamp "2020-01-25T18:00:00Z"^^xsd:dateTime;
              Event:hasDateTimeType Event:Planned;
              Event:involvesDigitalTwin ex:dt-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a, ex:dt-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesBusinessTransaction Event:businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesPhysicalInfrastructure Event:physicalInfrastructure-BEDEU01;
              Event:hasMilestone Event:End;
              Event:hasSubmissionTimestamp "2020-01-21T14:24:36"^^xsd:dateTime .
            
            ex:dt-5edc2423-d258-4002-8d6c-9fb3b1f6ff9a a dt:TransportMeans,
                owl:NamedIndividual .
            
            ex:businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39 a businessService:Consignment,
                owl:NamedIndividual;
              businessService:consignmentCreationTime "2021-05-13T21:23:04"^^xsd:dateTime;
              businessService:involvedActor ex:LegalPerson-Maersk .
            
            ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;
              businessService:actorName "Maersk" .
            
            ex:physicalInfrastructure-BEDEU01 a pi:Terminal, pi:LogisticalFunction, owl:NamedIndividual;
              rdfs:label "BEDEU01";
              pi:locatedAt pi:Location-BEDEG .
            
            ex:Location-BEDEG a pi:Location, owl:NamedIndividual;
              pi:cityName "Deurne, BE";
              pi:cityLoCode "BEDEG" .
            
            ex:dt-6c7edb9c-cfee-4b0c-998d-435cca8eeb39 a dt:Equipment, owl:NamedIndividual;
              dt:containerID "XINU4010266" .

            """.trimIndent()
        val parsedEvents = graphDBService.parseRDFToEvents(testRdfEvent)
        assertEquals(1, parsedEvents.size)
        val parsedEvent = parsedEvents.single()

        assertEquals("6c7edb9c-cfee-4b0c-998d-435cca8eeb39", parsedEvent.goods.single().toString())
        assertEquals("5edc2423-d258-4002-8d6c-9fb3b1f6ff9a", parsedEvent.transportMean.single().toString())

        assertEquals("BEDEU01", parsedEvent.location.single().toString())

        assertEquals(EventType.PLANNED, parsedEvent.timestamps.single().type)

        assertEquals(1579975200000, parsedEvent.timestamps.single().time.time)
        assertEquals(Milestone.END, parsedEvent.milestone)
        assertEquals("5edc2423-d258-4002-8d6c-9fb3b1f6ff9a", parsedEvent.timestamps.single().id)
    }

    @Test
    fun `Parse event - 2`() {
        val testRdfEvent = """
        ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            ex:Event-0c1e0ed5-636c-48b2-8f52-542e6f4d156a a Event:Event, owl:NamedIndividual;
              rdfs:label "GateIn", "Planned gate in";
              Event:hasTimestamp "2020-01-25T22:00:00Z"^^xsd:dateTime;
              Event:hasDateTimeType Event:Planned;
              Event:involvesDigitalTwin ex:dt-0c1e0ed5-636c-48b2-8f52-542e6f4d156a, ex:dt-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesBusinessTransaction ex:businessTransaction-6c7edb9c-cfee-4b0c-998d-435cca8eeb39;
              Event:involvesPhysicalInfrastructure ex:physicalInfrastructure-BEANTMP;
              Event:hasMilestone Event:Start;
              Event:hasSubmissionTimestamp "2020-01-21T14:24:39Z"^^xsd:dateTime .
            
            ex:dt-0c1e0ed5-636c-48b2-8f52-542e6f4d156a a dt:TransportMeans,
                owl:NamedIndividual .
            
            ex:physicalInfrastructure-BEANTMP a pi:Terminal, pi:LogisticalFunction, owl:NamedIndividual;
              rdfs:label "BEANTMP";
              pi:locatedAt pi:Location-BEANR .
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).first()

        assertEquals("0c1e0ed5-636c-48b2-8f52-542e6f4d156a", parsedEvent.transportMean.single().toString())

        assertEquals("BEANTMP", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 3`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()} 
            ex:Event-5b856159-4788-11ec-a78e-5c879c8043a4 a Event:Event, Event:ArrivalEvent;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Actual;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a;
                Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.
            
            ex:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a dt:TransportMeans.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).first()

        assertEquals("c5836199-8809-3930-9cf8-1d14a54d242a", parsedEvent.transportMean.single().toString())

        assertEquals("b4d51938-5ae5-330d-af2e-a198dd2c16ab", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 4`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}  
            ex:Event-5b8699f1-4788-11ec-b5e4-5c879c8043a4 a Event:Event, Event:DischargeEvent;
                Event:hasMilestone Event:End;
                Event:hasDateTimeType Event:Planned;
                Event:hasTimestamp "2021-11-10T18:51:20Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a, ex:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1.
            
            ex:DigitalTwin-c5836199-8809-3930-9cf8-1d14a54d242a a dt:TransportMeans.
            
            ex:DigitalTwin-ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1 a dt:Goods.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("c5836199-8809-3930-9cf8-1d14a54d242a", parsedEvent.transportMean.single().toString())
        assertEquals("ce1c5fa7-707d-385b-bdcd-d1d4025eb3d1", parsedEvent.goods.single().toString())
    }

    @Test
    fun `Parse event - 5`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}  
            
            ex:Event-f223c17c-c3ab-4871-9b78-3536d121925c a Event:Event, Event:ArrivalEvent;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Actual;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:dt-43691f54-091c-4378-a176-b730a4966996;
                Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-b4d51938-5ae5-330d-af2e-a198dd2c16ab.
            
            ex:dt-43691f54-091c-4378-a176-b730a4966996 a dt:TransportMeans.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("43691f54-091c-4378-a176-b730a4966996", parsedEvent.transportMean.single().toString())
        assertEquals("b4d51938-5ae5-330d-af2e-a198dd2c16ab", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 6`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            
            ex:Event-41068e69-4be0-11ec-a52a-5c879c8043a5 a Event:Event, Event:ArrivalEvent;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Planned;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f;
                Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            ex:dt-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("dce1774a-b2a1-338e-bd56-1902c57f836f", parsedEvent.otherDigitalTwins.single().toString())
        assertEquals("be989099-2e25-3259-975b-9f17c63b0281", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - 7 - milliseconds`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            
            data:dt-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f a owl:NamedIndividual, dt:TransportMeans .
            
            data:PhysicalInfrastructure-INNSA a "http://www.w3.org/2002/07/owl#NamedIndivudal~iri",
                pi:Location .
            
            data:Event-10d7fd0d-7a26-4b83-be1a-9c2606cebdc9 a "http://www.w3.org/2002/07/owl#NamedIndividual",
                Event:Event, Event:LoadEvent;
              Event:hasDateTimeType Event:Actual;
              Event:hasMilestone Event:Start;
              Event:hasSubmissionTimestamp "2022-09-09T12:20:15.332Z"^^xsd:dateTime;
              Event:hasTimestamp "2022-09-09T18:20:15.329Z"^^xsd:dateTime;
              Event:involvesBusinessTransaction "https://ontology.tno.nl/logistics/federated/tradelens#BusinessTransaction-bc71cb37-f2a9-4844-8d8b-891c8bf75521";
              Event:involvesDigitalTwin data:dt-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f;
              Event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-INNSA .

            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("bf93dc6a-1f04-4dec-ba0d-3ba987b2723f", parsedEvent.transportMean.single().toString())
        assertEquals("INNSA", parsedEvent.location.single().toString())
    }

    @Test
    fun `Parse event - TL`() {
        val testRdfEvent = """
                ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
                
                data:dt-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f a owl:NamedIndividual, dt:TransportMeans .
                
                data:PhysicalInfrastructure-INNSA a "http://www.w3.org/2002/07/owl#NamedIndivudal~iri",
                    pi:Location .
                
                data:Event-10d7fd0d-7a26-4b83-be1a-9c2606cebdc9 a "http://www.w3.org/2002/07/owl#NamedIndividual",
                    Event:Event, Event:LoadEvent;
                  Event:hasDateTimeType Event:Actual;
                  Event:hasMilestone Event:Start;
                  Event:hasSubmissionTimestamp "2022-09-09T12:20:15Z"^^xsd:dateTime;
                  Event:hasTimestamp "2022-09-09T18:20:15Z"^^xsd:dateTime;
                  Event:involvesBusinessTransaction "https://ontology.tno.nl/logistics/federated/tradelens#BusinessTransaction-bc71cb37-f2a9-4844-8d8b-891c8bf75521";
                  Event:involvesDigitalTwin data:dt-bf93dc6a-1f04-4dec-ba0d-3ba987b2723f;
                  Event:involvesPhysicalInfrastructure data:PhysicalInfrastructure-INNSA .
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals("bf93dc6a-1f04-4dec-ba0d-3ba987b2723f", parsedEvent.transportMean.single().toString())
        assertEquals("INNSA", parsedEvent.location.single().toString())
    }

    @Test
    fun `Insert new event`() {
        val successfulInsertion = graphDBService.insertEvent(TTLRandomGenerator().generateRandomEvents(1).constructedTTL, false)
        assert(successfulInsertion)
    }

    @Ignore("Enable this when shacl validation is back working")
    @Test
    fun `Insert invalid event`() {
        val successfulInsertion = graphDBService.insertEvent(invalidSampleTTL, false)
        assert(!successfulInsertion)
    }

    @Test
    fun `parse labels`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            
            ex:Event-41068e69-4be0-11ec-a52a-5c879c8043a5 a Event:Event;
                rdfs:label "GateOut test"^^xsd:string, "insuranceEvent"^^xsd:string;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Planned;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:dt-dce1774a-b2a1-338e-bd56-1902c57f836f;
                Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            ex:dt-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals(2, parsedEvent.labels.size)
        assertContainsAllOf(parsedEvent.labels, "GateOut test", "insuranceEvent")
    }

    @Test
    fun `parse other dt mean`() {
        val testRdfEvent = """
            ${PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()}
            
            ex:Event-41068e69-4be0-11ec-a52a-5c879c8043a5 a Event:Event;
                rdfs:label "GateOut test"^^xsd:string, "insuranceEvent"^^xsd:string;
                Event:hasMilestone Event:Start;
                Event:hasDateTimeType Event:Planned;
                Event:hasTimestamp "2021-11-10T08:44:07Z"^^xsd:dateTime;
                Event:involvesDigitalTwin ex:DigitalTwin-dce1774a-b2a1-338e-bd56-1902c57f836f;
                Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-be989099-2e25-3259-975b-9f17c63b0281.
            
            ex:dt-dce1774a-b2a1-338e-bd56-1902c57f836f a dt:TransportMeans, owl:NamedIndividual.
            """.trimIndent()

        val parsedEvent = graphDBService.parseRDFToEvents(testRdfEvent).single()

        assertEquals(1, parsedEvent.otherDigitalTwins.size)
        assertEquals(UUID.fromString("dce1774a-b2a1-338e-bd56-1902c57f836f"), parsedEvent.otherDigitalTwins.single())
    }
}