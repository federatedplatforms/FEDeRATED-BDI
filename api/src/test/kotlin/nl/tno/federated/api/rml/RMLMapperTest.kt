package nl.tno.federated.api.rml

import nl.tno.federated.api.event.mapper.EventType
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertNotNull

class RMLMapperTest {

    private val rmlMapper = RMLMapper()
    private val loadEventJson = String(ClassPathResource("test-data/LoadEvent.json").inputStream.readBytes())
    private val arrivalEventJson = String(ClassPathResource("test-data/ArrivalEvent.json").inputStream.readBytes())

    @Test
    fun test() {
        val result = rmlMapper.createTriples(loadEventJson, "rml/EventMapping.ttl")
        assertNotNull(result)
        // TODO more assertions here
    }

    @Test
    fun testArrival() {
        val result = rmlMapper.createTriples(arrivalEventJson, "rml/EventMapping.ttl")
        assertNotNull(result)
        // TODO more assertions here
    }
}