package nl.tno.federated.api.event.mapper

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import nl.tno.federated.api.model.ArrivalEvent
import nl.tno.federated.api.model.LoadEvent
import nl.tno.federated.api.rml.RMLMapper
import nl.tno.federated.api.util.RDFUtils.convert
import nl.tno.federated.api.util.toJsonNode
import nl.tno.federated.api.util.toJsonString
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.helpers.JSONLDMode
import org.springframework.stereotype.Service

open class EventMapperException(msg: String, throwable: Throwable? = null) : Exception(msg, throwable)
class UnsupportedEventTypeException(msg: String) : EventMapperException(msg)

@Service
class EventMapper(
    private val objectMapper: ObjectMapper
) {
    private val tripleService = RMLMapper()

    fun <T : Any> toJsonNode(event: T): JsonNode {
        return event.toJsonNode(objectMapper)
    }

    fun toRDFTurtle(jsonNode: JsonNode, eventType: EventType): String {
        val json = objectMapper.writeValueAsString(jsonNode)
        val rmlFile = eventType.rmlFile
        return tripleService.createTriples(json, rmlFile) ?: throw EventMapperException("Unable to map event to RDF, no output from mapping.")
    }

    fun toCompactedJSONLD(rdf: String): String {
        return convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD, JSONLDMode.COMPACT)
    }

    fun toFlattenedJSONLD(rdf: String): String {
        return convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD, JSONLDMode.FLATTEN)
    }

    private fun <T : Any> getRMLFileLocation(event: T): String = when (event) {
        is ArrivalEvent -> EventType.ArrivalEvent.rmlFile
        is LoadEvent -> EventType.LoadEvent.rmlFile
        else -> throw UnsupportedEventTypeException(event.javaClass.simpleName)
    }

    fun <T> isEventTypeSupported(event: T): Boolean {
        return when (event) {
            is ArrivalEvent -> true
            is LoadEvent -> true
            else -> false
        }
    }
}
