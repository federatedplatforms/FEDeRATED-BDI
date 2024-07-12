package nl.tno.federated.api.event.distribution.rules

/**
 * Distributes the events to a set of static destinations.
 */
class StaticDestinationEventDistributionRule(
    private val destinations: Set<String>
) : EventDistributionRule {

    override fun getDestinations() = destinations

    override fun appliesTo(ttl: String): Boolean = true

    override fun toString(): String {
        return "StaticDestinationEventDistributionRule(destinations='${getDestinations().joinToString(",")}')"
    }
}