package nl.tno.federated.api.event.distribution

import nl.tno.federated.api.event.mapper.EventType
import java.util.*

interface EventDistributionService<T : EventDestination<*>> {

    /**
     * Distribute event to the specified EventDestinations
     */
    fun distributeEvent(eventUUID: UUID, event: String, eventType: EventType, destinations: Set<T>?): UUID
}


