package nl.tno.federated.api.event.distribution.rules

interface EventDistributionRule {
    fun getDestinations(): Set<String>
    fun appliesTo(ttl: String): Boolean
    fun type(): String = this.javaClass.name
}