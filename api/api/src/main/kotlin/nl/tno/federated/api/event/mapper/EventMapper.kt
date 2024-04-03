package nl.tno.federated.api.event.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import nl.tno.federated.api.rml.RMLMapper
import nl.tno.federated.api.util.RDFUtils.convert
import nl.tno.federated.api.util.toJsonNode
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.helpers.JSONLDMode
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service

open class EventMapperException(msg: String, throwable: Throwable? = null) : Exception(msg, throwable)
class UnsupportedEventTypeException(msg: String) : EventMapperException(msg)

@Service
class EventMapper(
    private val objectMapper: ObjectMapper
) {
    private val tripleService = RMLMapper()

    fun toJsonNode(event: String): JsonNode {
        return event.toJsonNode(objectMapper)
    }

    fun toRDFTurtle(jsonNode: JsonNode, eventType: EventType): String {
        val json = objectMapper.writeValueAsString(jsonNode)
        return tripleService.createTriples(json, eventType.rml) ?: throw EventMapperException("Unable to map event to RDF, no output from mapping.")
    }

    fun toCompactedJSONLD(rdf: String): String {
        return convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD, JSONLDMode.COMPACT)
    }

    fun toCompactedJSONLDMap(rdf: String): JsonNode {
        val bla = convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD, JSONLDMode.COMPACT)
        return objectMapper.readTree(bla)
    }

    fun toFlattenedJSONLD(rdf: String): String {
        return convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD, JSONLDMode.FLATTEN)
    }
}
