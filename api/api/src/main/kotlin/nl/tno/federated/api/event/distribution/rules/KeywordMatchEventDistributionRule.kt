package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination

class KeywordMatchEventDistributionRule(
    private val keyword: String,
    private val destinations: Set<CordaEventDestination>
) : EventDistributionRule<CordaEventDestination> {

    override fun getDestinations() = destinations

    override fun appliesTo(ttl: String): Boolean = ttl.contains(keyword)
}