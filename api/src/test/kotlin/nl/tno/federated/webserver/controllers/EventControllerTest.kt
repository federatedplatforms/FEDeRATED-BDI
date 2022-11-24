package nl.tno.federated.webserver.controllers

import io.mockk.every
import io.mockk.mockk
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.NodeRPCConnection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class EventControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var rpc: NodeRPCConnection

    @Test
    fun newEventWithDestination() {
        val client = mockk<CordaRPCOps>()
        val flowHandle = mockk<FlowHandle<SignedTransaction>>()
        val uuid = UUID.randomUUID()

        whenever(rpc.client()).thenReturn(client) // return mockk mock which is more flexible than mockito

        every {
            (flowHandle.returnValue.get().coreTransaction.getOutput(0) as EventState).linearId.id
        }.returns(uuid)

        every { client.startFlowDynamic(NewEventFlow::class.java, *anyVararg()) }.returns(flowHandle)

        val body = """
            {
                "fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-b550739e-2ac2-4c21-9a56-e74791313375 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:START;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
                "countriesInvolved": []
            }
        """".trimIndent()

        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/TNO/Soesterberg/NL", HttpEntity(body, headers), String::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue("Response body should contain UUID returned from NewEvent flow", response.body!!.contains(uuid.toString()))
    }

    @Test
    fun newEventNoDestinations() {
        val client = mockk<CordaRPCOps>()
        val flowHandle = mockk<FlowHandle<SignedTransaction>>()
        val uuid = UUID.randomUUID()

        whenever(rpc.client()).thenReturn(client) // return mockk mock which is more flexible than mockito

        every {
            (flowHandle.returnValue.get().coreTransaction.getOutput(0) as EventState).linearId.id
        }.returns(uuid)

        every { client.startFlowDynamic(NewEventFlow::class.java, *anyVararg()) }.returns(flowHandle)

        val body = """
            {
                "fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-b550739e-2ac2-4c21-9a56-e74791313375 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:START;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
                "countriesInvolved": []
            }
        """".trimIndent()

        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/", HttpEntity(body, headers), String::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue("Response body should contain UUID returned from NewEvent flow", response.body!!.contains(uuid.toString()))
    }

    @Test
    fun newEventWrongDestinations() {
        val client = mockk<CordaRPCOps>()
        val flowHandle = mockk<FlowHandle<SignedTransaction>>()
        val uuid = UUID.randomUUID()

        whenever(rpc.client()).thenReturn(client) // return mockk mock which is more flexible than mockito

        every {
            (flowHandle.returnValue.get().coreTransaction.getOutput(0) as EventState).linearId.id
        }.returns(uuid)

        every { client.startFlowDynamic(NewEventFlow::class.java, *anyVararg()) }.returns(flowHandle)

        val body = """
            {
                "fullEvent": "@base <http:\/\/example.com\/base\/> . @prefix pi: <https:\/\/ontology.tno.nl\/logistics\/federated\/PhysicalInfrastructure#> . @prefix classifications: <https:\/\/ontology.tno.nl\/logistics\/federated\/Classifications#> . @prefix dcterms: <http:\/\/purl.org\/dc\/terms\/> . @prefix LogisticsRoles: <https:\/\/ontology.tno.nl\/logistics\/federated\/LogisticsRoles#> . @prefix rdfs: <http:\/\/www.w3.org\/2000\/01\/rdf-schema#> . @prefix owl: <http:\/\/www.w3.org\/2002\/07\/owl#> . @prefix Event: <https:\/\/ontology.tno.nl\/logistics\/federated\/Event#> . @prefix ReusableTags: <https:\/\/ontology.tno.nl\/logistics\/federated\/ReusableTags#> . @prefix businessService: <https:\/\/ontology.tno.nl\/logistics\/federated\/BusinessService#> . @prefix DigitalTwin: <https:\/\/ontology.tno.nl\/logistics\/federated\/DigitalTwin#> . @prefix skos: <http:\/\/www.w3.org\/2004\/02\/skos\/core#> . @prefix xsd: <http:\/\/www.w3.org\/2001\/XMLSchema#> . @prefix ex: <http:\/\/example.com\/base#> . @prefix time: <http:\/\/www.w3.org\/2006\/time#> . @prefix dc: <http:\/\/purl.org\/dc\/elements\/1.1\/> . @prefix era: <http:\/\/era.europa.eu\/ns#> .  ex:Event-b550739e-2ac2-4c21-9a56-e74791313375 a Event:Event, owl:NamedIndividual;   rdfs:label \"GateOut test\", \"Planned gate out\";   Event:hasTimestamp \"2019-09-22T06:00:00Z\"^^xsd:dateTime;   Event:hasDateTimeType Event:Planned;   Event:involvesDigitalTwin ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def, ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:involvesBusinessTransaction ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833;   Event:hasMilestone Event:START;   Event:hasSubmissionTimestamp \"2019-09-17T23:32:07Z\"^^xsd:dateTime .  ex:DigitalTwin-f7ed44a4-0ac1-42fc-820b-765bb2a70def a DigitalTwin:TransportMeans,     owl:NamedIndividual .  ex:businessTransaction-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a businessService:Consignment,     owl:NamedIndividual;   businessService:consignmentCreationTime \"2021-05-13T21:23:04Z\"^^xsd:dateTime;   businessService:involvedActor ex:LegalPerson-Maersk .  ex:LegalPerson-Maersk a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;   businessService:actorName \"Maersk\" .  ex:Equipment-a891b64d-d29f-4ef2-88ad-9ec4c88e0833 a DigitalTwin:Equipment, owl:NamedIndividual;   rdfs:label \"MNBU0494490\" .",
                "countriesInvolved": []
            }
        """".trimIndent()

        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/TNO/Soesterberg", HttpEntity(body, headers), String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue("The missing destination fields have not been identified", response.body!!.contains("Missing destination field"))
    }

    @Test
    fun generateRandomEventNoFlow() {

        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/random?start-flow=false&number-events=5", HttpEntity("", headers), String::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue("Response body should contain UUID returned from NewEvent flow", response.body!!.contains("Event:Event"))
    }

    @Test
    fun generateRandomEventWithFlow() {
        val client = mockk<CordaRPCOps>()
        val flowHandle = mockk<FlowHandle<SignedTransaction>>()
        val uuid = UUID.randomUUID()

        whenever(rpc.client()).thenReturn(client) // return mockk mock which is more flexible than mockito

        every {
            (flowHandle.returnValue.get().coreTransaction.getOutput(0) as EventState).linearId.id
        }.returns(uuid)

        every { client.startFlowDynamic(NewEventFlow::class.java, *anyVararg()) }.returns(flowHandle)


        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/random?start-flow=true&number-events=1", HttpEntity("", headers), String::class.java)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue("Response body should contain UUID returned from NewEvent flow", response.body!!.contains(uuid.toString()))
    }

    @Test
    fun newRandomEventWrongDestinationNoFlow() {
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer doitanyway")
        }

        val response = testRestTemplate.postForEntity("/events/random/TNO?start-flow=false&number-events=1", HttpEntity("", headers), String::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertTrue("The missing destination fields have not been identified", response.body!!.contains("Missing destination field"))
    }

    @Test
    fun newEventIncorrectAuthorizationHeader() {
        val headers = HttpHeaders().apply {
            set(HttpHeaders.AUTHORIZATION, "Bearer wontwork")
        }

        val response = testRestTemplate.postForEntity("/events/TNO/Soesterberg/NL", HttpEntity("bla", headers), String::class.java)

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @Test
    fun newUnprocessedEvent() {
        // In an integration test this:
        // - requires graphdb to be up and running
        // - requires semantic adapter to be up and running
        // - requires tradelens to be reachable
    }
}