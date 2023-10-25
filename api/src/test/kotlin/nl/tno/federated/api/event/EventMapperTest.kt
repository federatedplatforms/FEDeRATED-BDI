package nl.tno.federated.api.event

import com.fasterxml.jackson.databind.ObjectMapper
import nl.tno.federated.api.event.mapper.EventMapper
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertNotNull

class EventMapperTest {

    private var eventMapper: EventMapper = EventMapper(ObjectMapper())
    private val event = ClassPathResource("test-data/LoadEvent.ttl")

    @Test
    fun rdfToJsonLDCompacted() {
        val json = eventMapper.toCompactedJSONLD(String(event.inputStream.readBytes()))
        assertNotNull(json)
    }

    @Test
    fun rdfToJsonLDCompactedMap() {
        val json = eventMapper.toCompactedJSONLDMap(String(event.inputStream.readBytes()))
        assertNotNull(json)
    }

    @Test
    fun rdfToJsonLDFlattened() {
        val json = eventMapper.toFlattenedJSONLD(String(event.inputStream.readBytes()))
        assertNotNull(json)
    }
}