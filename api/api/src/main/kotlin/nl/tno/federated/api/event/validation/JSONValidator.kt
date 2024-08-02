package nl.tno.federated.api.event.validation

import net.pwall.json.schema.JSONSchema
import org.slf4j.LoggerFactory


class JSONValidationException(msg: String?) : Exception(msg)
class JSONValidator () {

    private val log = LoggerFactory.getLogger(JSONValidator::class.java)

    fun validateJSON(json: String,schema: String) {

        val JSONschema = JSONSchema.parse(schema)
        val output = JSONschema.validateBasic(json)
        if (!output.valid ) {
            val builder = StringBuilder()
            builder.append("The JSON event provided does not match the required definition: \n")
            output.errors?.forEach {
                builder.append("${it.error} - ${it.instanceLocation} \n")
            }
            throw JSONValidationException(builder.toString())
        }
    }



}