package nl.tno.federated.api.event

import com.fasterxml.jackson.databind.node.JsonNodeType
import com.fasterxml.jackson.databind.node.ObjectNode
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import nl.tno.federated.api.event.mapper.EventMapper
import nl.tno.federated.api.event.mapper.EventType
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.event.validation.ShaclValidator
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventService(
    private val eventMapper: EventMapper,
    private val eventDistributionService: CordaEventDistributionService,
    private val eventQueryService: CordaEventQueryService,
    private val eventTypeMapping: EventTypeMapping
) {

    private val shaclValidator = ShaclValidator(eventTypeMapping.readShaclShapes())

    /**
     * Convert the given event to RDF.
     *
     * @throws UnsupportedEventTypeException is an unsupported Event type is encountered.
     */
    fun newJsonEvent(event: String, eventType: EventType): UUID {
        val node = eventMapper.toJsonNode(event)
        if(node.nodeType != JsonNodeType.OBJECT) throw UnsupportedEventTypeException("Unexpected Event content, not parsable as JSON!")

        node as ObjectNode
        val uuid = UUID.randomUUID()
        node.put("UUID", uuid.toString())
        node.put("eventType", eventType.name)

        val rdf = eventMapper.toRDFTurtle(jsonNode = node, eventType = eventType)
        validateWithShacl(rdf = rdf, eventType = eventType)
        publishRDFEvent(eventUUID = uuid, event = rdf, eventType = eventType)
        return uuid
    }

    fun validateNewJsonEvent(event: String, eventType: EventType): String {
        val node = eventMapper.toJsonNode(event)
        if(node.nodeType != JsonNodeType.OBJECT) throw UnsupportedEventTypeException("Unexpected Event content, not parsable as JSON!")

        node as ObjectNode
        val uuid = UUID.randomUUID()
        node.put("UUID", uuid.toString())
        node.put("eventType", eventType.name)

        val rdf = eventMapper.toRDFTurtle(jsonNode = node, eventType = eventType)
        validateWithShacl(rdf = rdf, eventType = eventType)
        return rdf
    }

    fun validateWithShacl(rdf: String, eventType: EventType) {
        if(eventType.shacl != null) shaclValidator.validate(rdf)
    }

    fun publishRDFEvent(eventUUID: UUID, event: String, eventType: EventType, destinations: Set<String>? = null): UUID {
        val dest = destinations?.map { CordaEventDestination.parse(it) }?.toSet()
        return eventDistributionService.distributeEvent(eventUUID = eventUUID, event = event, eventType = eventType, destinations = dest)
    }

    fun findEventById(id: String): String? {
        val rdf = eventQueryService.findById(id) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }

    fun findAll(page: Int, size: Int): List<String> {
        val result = eventQueryService.findAll(page, size)
        return result.map { eventMapper.toCompactedJSONLD(it) }
    }

    fun query(eventQuery: EventQuery): String? {
        val rdf = eventQueryService.executeQuery(eventQuery) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }
}