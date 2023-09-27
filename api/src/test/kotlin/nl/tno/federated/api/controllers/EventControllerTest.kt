package nl.tno.federated.api.controllers

import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class EventControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var eventDistributionService: CordaEventDistributionService

    /**
     * When creating a LoadEvent we expect a 201 created response code with the location header pointing to the correct resource URI.
     */
    @Test
    fun testCreateLoadEvent() {
        val eventContentType = "federated.events.load-event.v1"

        val headers = HttpHeaders().apply {
            set(ACCEPT, APPLICATION_JSON_VALUE);
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
        }

        val jsonString = String(ClassPathResource("test-data/LoadEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/events", HttpEntity(jsonString, headers), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location!!.toString().startsWith("/events/"))
    }

    /**
     * When adding a LoadEvent we expect a created response with the location header to be set.
     */
    @Test
    fun testCreateArrivalEvent() {
        val eventContentType = "federated.events.arrival-event.v1"

        val headers = HttpHeaders().apply {
            set(ACCEPT, APPLICATION_JSON_VALUE);
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
        }

        val jsonString = String(ClassPathResource("test-data/ArrivalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/events", HttpEntity(jsonString, headers), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location!!.toString().startsWith("/events/"))
    }


    /**
     * When adding a LoadEvent we expect a created response with the location header to be set.
     */
    @Test
    fun testValidateArrivalEvent() {
        val eventContentType = "federated.events.arrival-event.v1"

        val headers = HttpHeaders().apply { ;
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
        }

        val jsonString = String(ClassPathResource("test-data/ArrivalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/events/validate", HttpEntity(jsonString, headers), String::class.java)

        println(response.body)

        assertNotNull(response.body)
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}