package nl.tno.federated.api.event.validation

import org.junit.Test
import org.springframework.core.io.ClassPathResource

class JSONValidatorTest
{

    @Test
    fun validate() {
        val schema = String(ClassPathResource("test-data/MinimalEventSchema.json").inputStream.readBytes())
        val json = String(ClassPathResource("test-data/MinimalEvent.json").inputStream.readBytes())

        val validator = JSONValidator()
        validator.validateJSON(json,schema)
    }

    @Test(expected = JSONValidationException::class)
    fun validateExceptionThrown() {
        val schema = String(ClassPathResource("test-data/MinimalEventSchema.json").inputStream.readBytes())
        val json = String(ClassPathResource("test-data/IncorrectMinimalEvent.json").inputStream.readBytes())

        val validator = JSONValidator()
        validator.validateJSON(json, schema)
    }
}