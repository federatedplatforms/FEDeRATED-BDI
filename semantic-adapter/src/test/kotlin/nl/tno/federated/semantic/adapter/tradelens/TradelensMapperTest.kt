package nl.tno.federated.semantic.adapter.tradelens

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset

class TradelensMapperTest {

    private val objectMapper = ObjectMapper()
        .findAndRegisterModules()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    private val mapper: TradelensMapper = TradelensMapper(objectMapper)

    private val events = ClassPathResource("tl-events-input.json")
    private val eventsWithWrapperNode = ClassPathResource("tl-events-wrapper-node-input.json")
    private val transportEquipmentList = ClassPathResource("tl-container-details.json")

    /**
     * This test converts a JSON array containing events (1).
     *
     * [ { "eventType" : "etc", ...  } ]
     */
    @Test
    fun createPreMappingEventsTest() {
        val events = mapper.createPreMappingEvents(events.readAsString()).second
        assertEquals(1, events.size, "expected 1 event")
        // TODO more assertion
    }

    /**
     * This test converts a JSON object with an 'events' node containing an array of events (3)
     *
     * { "events" : [ { "eventType" : "etc", ...  } ] }
     *
     */
    @Test
    fun createPreMappingEventsWithWrapperNodeTest() {
        val events = mapper.createPreMappingEvents(eventsWithWrapperNode.readAsString()).second
        assertEquals(3, events.size, "expected 3 event")
        // TODO more assertion
    }

    /**
     * This test converts a JSON object with a 'transportEquipmentList' node containing an array with container details (1)
     *
     * { ..., "transportEquipmentList" : [ "transportEquipmentSummary" : { "transportEquipmentId" : "etc", ... }, ... ]
     */
    @Test
    fun createPreMappingContainersTest() {
        val events = mapper.createPreMappingContainers(transportEquipmentList.readAsString()).second
        assertEquals(1, events.size, "expected 1 container")
        // TODO more assertion
    }
}

private fun ClassPathResource.readAsString(): String = String(inputStream.readBytes(), Charset.defaultCharset())