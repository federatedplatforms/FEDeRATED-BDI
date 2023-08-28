package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination

/**
 * Distributes the events to a set of static destinations.
 */
class StaticDestinationEventDistributionRule(
    private val destinations: Set<CordaEventDestination>
) : EventDistributionRule<CordaEventDestination> {

    override fun getDestinations() = destinations

    override fun appliesTo(ttl: String): Boolean = true

    override fun toString(): String {
        return "StaticDestinationEventDistributionRule(destinations='${getDestinations().joinToString(",") { "[organisation=${it.destination.organisation},locality=${it.destination.locality},country=${it.destination.country}]" }}')"
    }
}