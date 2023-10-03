package nl.tno.federated.api.event.distribution.rules

import nl.tno.federated.api.event.distribution.corda.CordaEventDestination

class FailedEventDistributionRule(val errorMessage: String?) : EventDistributionRule<CordaEventDestination> {

    override fun getDestinations() = emptySet<CordaEventDestination>()

    override fun appliesTo(ttl: String): Boolean = true

    override fun toString(): String {

        return "FailedEventDistributionRule(errorMessage='${errorMessage}')"
    }
}