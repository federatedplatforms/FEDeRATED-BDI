package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination

class BroadcastToAllEventDistributionRule(val cordaNodeService: CordaNodeService?) : EventDistributionRule<CordaEventDestination> {

    override fun getDestinations() = cordaNodeService?.getNetworkMapSnapshot()?.map { CordaEventDestination(it.legalIdentities.first().name) } ?: listOf()

    override fun appliesTo(ttl: String): Boolean = true
}