package nl.tno.federated.api.rml

import org.junit.Test
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset
import kotlin.test.assertNotNull

class RMLMapperTest {

    private val rmlMapper = RMLMapper()
    private val loadEventJson = ClassPathResource("test-data/LoadEvent.json").getContentAsString(Charset.defaultCharset())
    private val arrivalEventJson = ClassPathResource("test-data/ArrivalEvent.json").getContentAsString(Charset.defaultCharset())
    private val rml = ClassPathResource("rml/EventMapping.ttl").getContentAsString(Charset.defaultCharset())

    @Test
    fun test() {
        val result = rmlMapper.createTriples(loadEventJson, rml)
        assertNotNull(result)
        // TODO more assertions here
    }

    @Test
    fun testArrival() {
        val result = rmlMapper.createTriples(arrivalEventJson, rml)
        assertNotNull(result)
        // TODO more assertions here
    }
}