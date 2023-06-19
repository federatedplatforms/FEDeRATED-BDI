package nl.tno.federated.api.event.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import nl.tno.federated.api.model.ArrivalEvent
import nl.tno.federated.api.model.LoadEvent
import nl.tno.federated.api.util.RDFUtils.convert
import nl.tno.federated.api.util.compactJsonLD
import nl.tno.federated.api.util.toJsonString
import nl.tno.federated.semantic.adapter.core.TripleService
import org.eclipse.rdf4j.rio.RDFFormat
import org.springframework.stereotype.Service


open class EventMapperException(msg: String, throwable: Throwable? = null) : Exception(msg, throwable)
class UnsupportedEventTypeException(msg: String) : EventMapperException(msg)

@Service
class EventMapper(
    private val objectMapper: ObjectMapper
) {
    private val tripleService = TripleService()

    fun <T : Any> toRDFTurtle(event: T): String {
        val json = event.toJsonString(objectMapper)
        val rmlFile = getRMLFileLocation(event)
        return tripleService.createTriples(json, rmlFile) ?: throw EventMapperException("Unable to map event to RDF, no output from mapping.")
    }

    fun toCompactedJSONLD(rdf: String): Map<String, Any> {
        val jsonLd = convert(rdf, RDFFormat.TURTLE, RDFFormat.JSONLD)
        return compactJsonLD(jsonLd)
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
