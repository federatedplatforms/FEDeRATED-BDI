package nl.tno.federated.api.event.distribution.rules

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventDistributionRuleService(
    private val eventDistributionRuleRepository: EventDistributionRuleRepository
) {

    private val log = LoggerFactory.getLogger(EventDistributionRuleService::class.java)

    fun addDistributionRule(dto: EventDistributionRuleEntity): EventDistributionRuleEntity {
        return eventDistributionRuleRepository.save(dto)
    }

    fun getDistributionRules(): Set<EventDistributionRuleEntity> {
        return eventDistributionRuleRepository.findAll().toSet()
    }

    fun delete(id: Long) {
        val findById = eventDistributionRuleRepository.findById(id)
        eventDistributionRuleRepository.delete(findById.orElseThrow())
    }
}