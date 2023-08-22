package nl.tno.federated.api.controllers

import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class EventControllerTest {

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockBean
    lateinit var eventDistributionService: CordaEventDistributionService

    /**
     * When adding a LoadEvent we expect a created response with the location header to be set.
     */
    @Test
    fun testLoadEvent() {
        val uuid = UUID.randomUUID()
        whenever(eventDistributionService.distributeEvent(any(), any(), anyOrNull())).thenReturn(uuid)

        val jsonString = String(ClassPathResource("test-data/LoadEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/events/LoadEvent", HttpEntity(jsonString, HttpHeaders().apply { set(ACCEPT, APPLICATION_JSON_VALUE); set(CONTENT_TYPE, APPLICATION_JSON_VALUE) }), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("/events/LoadEvent/${uuid}", response.headers.location!!.toString())
    }

    /**
     * When adding a LoadEvent we expect a created response with the location header to be set.
     */
    @Test
    @Ignore("TODO: Need a sample ArrivalEvent")
    fun testArrivalEvent() {
        val uuid = UUID.randomUUID()
        whenever(eventDistributionService.distributeEvent(any(), any(), anyOrNull())).thenReturn(uuid)

        val jsonString = String(ClassPathResource("test-data/ArrivalEvent.json").inputStream.readBytes())
        val response = testRestTemplate.postForEntity("/events/ArrivalEvent", HttpEntity(jsonString, HttpHeaders().apply { set(ACCEPT, APPLICATION_JSON_VALUE); set(CONTENT_TYPE, APPLICATION_JSON_VALUE) }), String::class.java)

        assertNotNull(response)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals("/events/ArrivalEvent/${uuid}", response.headers.location!!.toString())
    }
}