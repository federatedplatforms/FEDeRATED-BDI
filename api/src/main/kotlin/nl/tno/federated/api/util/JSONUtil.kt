package nl.tno.federated.api.util

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

fun <T : Any> String.fromJson(objectMapper: ObjectMapper, type: Class<T>): T {
    return objectMapper.readValue(this, type)
}

fun compactJsonLD(jsonLd: String): String {
    val jsonObject: Any = JsonUtils.fromString(jsonLd)
    val result: Any = JsonLdProcessor.compact(jsonObject, HashMap<Any, Any>(), JsonLdOptions())
    return JsonUtils.toString(result)
}

fun flattenJsonLD(jsonLd: String): String {
    val jsonObject: Any = JsonUtils.fromString(jsonLd)
    val result: Any = JsonLdProcessor.flatten(jsonObject, HashMap<Any, Any>(), JsonLdOptions())
    return JsonUtils.toString(result)
}