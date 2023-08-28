package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination

class BroadcastEventDistributionRule(val cordaNodeService: CordaNodeService) : EventDistributionRule<CordaEventDestination> {

    override fun getDestinations() = cordaNodeService.getPeersExcludingSelfAndNotary().map { CordaEventDestination(it.name) }.toSet()

    override fun appliesTo(ttl: String): Boolean = true

    override fun toString(): String {
        val cordaEventDestinations = try {
            getDestinations()
        } catch (e: Exception) {
            emptySet<CordaEventDestination>()
        }
        return "BroadcastToAllEventDistributionRule(destinations='${cordaEventDestinations.joinToString(",") { "[organisation=${it.destination.organisation},locality=${it.destination.locality},country=${it.destination.country}]" }}')"
    }
}