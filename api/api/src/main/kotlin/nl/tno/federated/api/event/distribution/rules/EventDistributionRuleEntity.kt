package nl.tno.federated.api.event.distribution.rules

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("EVENT_DISTRIBUTION_RULE")
data class EventDistributionRuleEntity(
    @Id
    var id: Long? = null,
    @Column
    val ruleType: EventDistributionRuleType,
    val destinations: String
)