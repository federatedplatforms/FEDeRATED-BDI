package nl.tno.federated.api.event.distribution.corda

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.EnrichedEvent
import nl.tno.federated.api.event.distribution.EventDistributionService
import nl.tno.federated.api.event.distribution.rules.BroadcastEventDistributionRule
import nl.tno.federated.api.event.distribution.rules.EventDistributionRule
import nl.tno.federated.api.event.distribution.rules.EventDistributionRuleEntity
import nl.tno.federated.api.event.distribution.rules.EventDistributionRuleService
import nl.tno.federated.api.event.distribution.rules.EventDistributionRuleType
import nl.tno.federated.api.event.distribution.rules.StaticDestinationEventDistributionRule
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaEventDistributionService(
    private val cordaNodeService: CordaNodeService,
    private val rules: EventDistributionRuleService
) : EventDistributionService<CordaEventDestination> {

    override fun distributeEvent(enrichedEvent: EnrichedEvent, destinations: Set<CordaEventDestination>?): UUID {
        val destinationSet = destinations ?: runEventDistributionRules(enrichedEvent.eventRDF)
        log.info("Sending eventType: ${enrichedEvent.eventType.eventType} with eventUUID: ${enrichedEvent.eventUUID} to destination(s): ${destinationSet.map { it.destination }}")
        return cordaNodeService.startNewEventFlow(eventUUID = enrichedEvent.eventUUID.toString(), event = enrichedEvent.eventRDF, eventType = enrichedEvent.eventType.eventType, cordaNames = destinationSet.map { it.destination }.toSet())
    }

    private fun runEventDistributionRules(eventRdf: String): Set<CordaEventDestination> {
        val rule = getDistributionRules().first { it.appliesTo(eventRdf) }
        log.info("Using first matching rule for event that was found: {}", rule)
        return rule
            .getDestinations()
            .map { CordaEventDestination.parse(it) }
            .toSet()
    }

    private fun EventDistributionRuleEntity.toEventDistributionRule(): EventDistributionRule {
        val parsed = this.destinations.split(";")

        return when (this.ruleType) {
            EventDistributionRuleType.STATIC -> StaticDestinationEventDistributionRule(parsed.toSet())
            EventDistributionRuleType.SPARQL -> TODO("Not implemented yet")
            EventDistributionRuleType.BROADCAST -> BroadcastEventDistributionRule(cordaNodeService)
        }
    }

    fun getDistributionRules(): List<EventDistributionRule> {
        val rules = rules.getDistributionRules()

        return if (rules.none()) {
            log.info("No rules configured, returning 'broadcast' event distribution mode as default option.")
            listOf(BroadcastEventDistributionRule(cordaNodeService))
        } else {
            rules.map { it.toEventDistributionRule() }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CordaEventDistributionService::class.java)
    }
}