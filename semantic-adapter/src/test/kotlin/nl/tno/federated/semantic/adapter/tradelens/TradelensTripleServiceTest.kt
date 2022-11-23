package nl.tno.federated.semantic.adapter.tradelens

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset

class TradelensTripleServiceTest {

    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    private val mapper: TradelensMapper = TradelensMapper(objectMapper)
    private val tripleService = TradelensTripleService(mapper)

    private val singleEvent = ClassPathResource("tl-events-input.json")
    private val eventWrapperNode = ClassPathResource("tl-events-wrapper-node-input.json")
    private val container = ClassPathResource("tl-container-details.json")

    @Test
    fun createTriplesForSingleEvent() {
        val result = tripleService.createTriplesForEvents(jsonData = singleEvent.readAsString())
        assertNotNull(result)
        // TODO more assertions
    }

    @Test
    fun createTriplesForEventsWithWrapperRootNode() {
        val result = tripleService.createTriplesForEvents(jsonData = eventWrapperNode.readAsString())
        assertNotNull(result)
        // TODO more assertions
    }

    @Test
    fun createTriplesForContainers() {
        val result = tripleService.createTriplesForContainers(jsonData = container.readAsString())
        assertNotNull(result)
        // TODO more assertions
    }
}

private fun ClassPathResource.readAsString(): String = String(inputStream.readBytes(), Charset.defaultCharset())