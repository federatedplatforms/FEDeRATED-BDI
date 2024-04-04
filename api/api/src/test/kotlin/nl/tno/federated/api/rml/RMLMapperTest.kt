package nl.tno.federated.api.rml

import org.junit.Test
import org.springframework.core.io.ClassPathResource
import java.nio.charset.Charset
import kotlin.test.assertNotNull

class RMLMapperTest {

    private val rmlMapper = RMLMapper()
    private val loadEventJson = ClassPathResource("test-data/MinimalEvent.json").getContentAsString(Charset.defaultCharset())
    private val rml = ClassPathResource("rml/MinimalEvent.ttl").getContentAsString(Charset.defaultCharset())

    @Test
    fun testMinimalEvent() {
        val result = rmlMapper.createTriples(loadEventJson, rml)
        println(result)
        assertNotNull(result)
        // TODO more assertions here
    }

}