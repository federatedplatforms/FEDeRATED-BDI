package nl.tno.federated.api.event.distribution

import nl.tno.federated.api.event.EnrichedEvent
import java.util.*

interface EventDistributionService<T : EventDestination<*>> {

    /**
     * Distribute event to the specified EventDestinations
     */
    fun distributeEvent(enrichedEvent: EnrichedEvent, destinations: Set<T>?): UUID
}


