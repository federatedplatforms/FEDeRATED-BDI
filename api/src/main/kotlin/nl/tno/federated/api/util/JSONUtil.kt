package nl.tno.federated.api.util

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jsonldjava.core.JsonLdOptions
import com.github.jsonldjava.core.JsonLdProcessor
import com.github.jsonldjava.utils.JsonUtils
import java.io.StringWriter

fun Any.toJsonString(objectMapper: ObjectMapper): String {
    val sw = StringWriter()
    objectMapper.writeValue(sw, this)
    return sw.toString()
}


fun String.toJsonNode(objectMapper: ObjectMapper): JsonNode {
   return objectMapper.readTree(this)
}

fun Any.toJsonNode(objectMapper: ObjectMapper): JsonNode {
    return objectMapper.valueToTree(this)
}

fun compactJsonLD(jsonLd: String): Map<String, Any> {
    val jsonObject: Any = JsonUtils.fromString(jsonLd)
    return JsonLdProcessor.compact(jsonObject, HashMap<Any, Any>(), JsonLdOptions())
}

fun flattenJsonLD(jsonLd: String): String {
    val jsonObject: Any = JsonUtils.fromString(jsonLd)
    val result: Any = JsonLdProcessor.flatten(jsonObject, HashMap<Any, Any>(), JsonLdOptions())
    return JsonUtils.toString(result)
}