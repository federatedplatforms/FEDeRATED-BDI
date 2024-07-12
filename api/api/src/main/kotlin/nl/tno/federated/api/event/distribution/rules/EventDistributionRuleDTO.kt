package nl.tno.federated.api.event.distribution.rules

class EventDistributionRuleDTO (
    var id: Long? = null,
    val ruleType: String,
    val destinations: String
)