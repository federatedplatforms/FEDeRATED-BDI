package nl.tno.federated.api.event

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.corda.CordaEventDistributionService
import nl.tno.federated.api.event.mapper.EventMapper
import nl.tno.federated.api.event.query.corda.CordaEventQueryService
import nl.tno.federated.api.event.query.EventQuery
import org.springframework.stereotype.Service
import java.util.*

@Service
class EventService(
    private val eventMapper: EventMapper,
    private val eventDistributionService: CordaEventDistributionService,
    private val eventQueryService: CordaEventQueryService
) {
    fun <T : Any> handleNewEvent(event: T, destinations: Set<CordaEventDestination>? = null): UUID {
        val rdf = eventMapper.toRDFTurtle(event)
        return eventDistributionService.distributeEvent(rdf, destinations)
    }

    fun findEventById(id: String): String? {
        val rdf = eventQueryService.findById(id) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }

    fun query(eventQuery: EventQuery): String? {
        val rdf = eventQueryService.executeQuery(eventQuery) ?: return null
        return eventMapper.toCompactedJSONLD(rdf)
    }
}