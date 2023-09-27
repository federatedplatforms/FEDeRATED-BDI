package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.event.distribution.EventDestination

interface EventDistributionRule<out T : EventDestination<*>> {
    fun getDestinations(): Set<T>
    fun appliesTo(ttl: String): Boolean
    fun type(): String = this.javaClass.name
}