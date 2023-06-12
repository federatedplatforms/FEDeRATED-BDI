package nl.tno.federated.api.event

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import nl.tno.federated.api.event.mapper.EventMapper
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
    fun handleNewEvent(event: EventWithDestinations): UUID {
        if( !isValidRDF(event.event, RDFFormat.TURTLE)) throw InvalidEventDataException("Expected RDF turtle event data.")
        val destinations = event.eventDestinations.map { CordaEventDestination.parse(it) }.toSet()
        return eventDistributionService.distributeEvent(event = event.event, eventType = event.eventType, destinations = destinations)
    }

    fun <T : Any> handleNewEvent(event: T, destinations: Set<CordaEventDestination>? = null): UUID {
        val rdf = eventMapper.toRDFTurtle(event)
        return eventDistributionService.distributeEvent(rdf, event.javaClass.simpleName, destinations)
    }

    fun findEventById(id: String): String? {
        val rdf = eventQueryService.findById(id) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }

    fun findAll(): List<String> {
        val result = eventQueryService.findAll()
        return result.map { eventMapper.toCompactedJSONLD(it) }
    }

    fun query(eventQuery: EventQuery): String? {
        val rdf = eventQueryService.executeQuery(eventQuery) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }
}