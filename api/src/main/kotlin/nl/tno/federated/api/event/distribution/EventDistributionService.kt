package nl.tno.federated.api.event.distribution

import java.util.*

interface EventDistributionService<T : EventDestination<*>> {

    /**
     * Distribute event to the specified EventDestinations
     */
    fun distributeEvent(event: String, destinations: Set<T>?): UUID
}


