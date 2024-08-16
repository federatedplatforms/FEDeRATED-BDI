package nl.tno.federated.api.controllers

import nl.tno.federated.api.corda.CordaNodeService
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class EventControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var cordaNodeService: CordaNodeService

    /**
     * When creating a LoadEvent we expect a 201 created response code with the location header pointing to the correct resource URI.
     */
    @Test
    fun testCreateMinimalEvent() {
        val eventContentType = "federated.events.minimal.v1"

        val headers = HttpHeaders().apply {
            set(ACCEPT, APPLICATION_JSON_VALUE);
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
        }

        val jsonString = String(ClassPathResource("test-data/MinimalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/api/events", HttpEntity(jsonString, headers), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location!!.toString().startsWith("/api/events/"))
    }

    @Test
    fun testCreateIncorrectMinimalEvent() {
        val eventContentType = "federated.events.minimal.v1"

        val headers = HttpHeaders().apply {
            set(ACCEPT, APPLICATION_JSON_VALUE);
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
        }
        val jsonString = String(ClassPathResource("test-data/IncorrectMinimalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/api/events", HttpEntity(jsonString, headers), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location!!.toString().startsWith("/api/events/"))
    }

    /**
     * When creating a LoadEvent we expect a 201 created response code with the location header pointing to the correct resource URI.
     */
    @Test
    fun testEventEventWithProvidedDestinations() {
        val eventContentType = "federated.events.minimal.v1"
        val eventDestinations = "O=DCA,L=Apeldoorn,C=NL;O=DCA,L=Utrecht,C=NL"

        val headers = HttpHeaders().apply {
            set(ACCEPT, APPLICATION_JSON_VALUE);
            set(CONTENT_TYPE, APPLICATION_JSON_VALUE);
            set(EVENT_TYPE_HEADER, eventContentType)
            set(EVENT_DESTINATION_HEADER, eventDestinations)
        }

        val jsonString = String(ClassPathResource("test-data/MinimalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/api/events", HttpEntity(jsonString, headers), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.headers.location!!.toString().startsWith("/api/events/"))
    }
}