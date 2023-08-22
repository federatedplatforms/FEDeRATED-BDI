package nl.tno.federated.api.event

import com.fasterxml.jackson.databind.node.ObjectNode
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import nl.tno.federated.api.event.mapper.EventMapper
import nl.tno.federated.api.event.mapper.EventType
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.model.LoadEvent
import nl.tno.federated.api.util.toJsonNode
import org.eclipse.rdf4j.model.Model
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventService(
    private val eventMapper: EventMapper,
    private val eventDistributionService: CordaEventDistributionService,
    private val eventQueryService: CordaEventQueryService
) {

    /**
     * Convert the given event to RDF.
     *
     * @throws UnsupportedEventTypeException is an unsupported Event type is encountered.
     */
    fun <T : Any> newJsonEvent(event: T): UUID {
        if(!eventMapper.isEventTypeSupported(event)) throw UnsupportedEventTypeException("Event type not supported: ${event.javaClass.simpleName}")

        val uuid = UUID.randomUUID()
        val eventType = event.javaClass.simpleName.toString()

        val node = eventMapper.toJsonNode(event) as ObjectNode
        node.put("UUID", uuid.toString())
        node.put("eventType", eventType)

        val rdf = eventMapper.toRDFTurtle(jsonNode = node, eventType = EventType.valueOf(eventType))
        publishRDFEvent(eventUUID = uuid, event = rdf, eventType = eventType)
        return uuid
    }

    fun publishRDFEvent(event: NewEvent): UUID {
        val uuid = UUID.randomUUID()
        return publishRDFEvent(eventUUID = uuid, event = event.event, eventType = event.eventType, destinations = event.eventDestinations)
    }

    fun publishRDFEvent(eventUUID: UUID, event: String, eventType: String, destinations: Set<String>? = null): UUID {
        val dest = destinations?.map { CordaEventDestination.parse(it) }?.toSet()
        return eventDistributionService.distributeEvent(event = event, eventType = eventType, destinations = dest)
    }

    private fun addUniqueIdentifier(model: Model): String {
        val uuid = UUID.randomUUID().toString()
        // TODO add unique ID
        // val uuidIri = Values.iri("https://ontology.tno.nl/logistics/federated/Event#", uuid);
        return uuid
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