package nl.tno.federated.api.event.distribution.rules

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventDistributionRuleRepository : CrudRepository<EventDistributionRuleEntity, Long> {
}