package nl.tno.federated.api.event

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import nl.tno.federated.api.event.mapper.EventMapper
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.util.RDFUtils.isValidRDF
import org.eclipse.rdf4j.rio.RDFFormat
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
    fun <T : Any> convertEventToRDF(event: T): String {
        if(!eventMapper.isEventTypeSupported(event)) throw UnsupportedEventTypeException("Event type not supported: ${event.javaClass.simpleName}")
        return eventMapper.toRDFTurtle(event)
    }

    fun publishRDFEvent(event: EventWithDestinations): UUID {
        return publishRDFEvent(event.event, event.eventType, event.eventDestinations)
    }

    fun publishRDFEvent(event: String, eventType: String, destinations: Set<String>? = null): UUID {
        if (!isValidRDF(event, RDFFormat.TURTLE)) throw InvalidEventDataException("Expected RDF turtle event data.")
        val dest = destinations?.map { CordaEventDestination.parse(it) }?.toSet()
        return eventDistributionService.distributeEvent(event = event, eventType = eventType, destinations = dest)
    }

    fun findEventById(id: String): Map<String, Any>? {
        val rdf = eventQueryService.findById(id) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }

    fun findAll(page: Int, size: Int): List<Map<String, Any>> {
        val result = eventQueryService.findAll(page, size)
        return result.map { eventMapper.toCompactedJSONLD(it) }
    }

    fun query(eventQuery: EventQuery): Map<String, Any>? {
        val rdf = eventQueryService.executeQuery(eventQuery) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }
}