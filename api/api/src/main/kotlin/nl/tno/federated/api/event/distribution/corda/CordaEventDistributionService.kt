package nl.tno.federated.api.event.distribution.corda

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.EnrichedEvent
import nl.tno.federated.api.event.distribution.EventDistributionRuleConfiguration
import nl.tno.federated.api.event.distribution.EventDistributionService
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaEventDistributionService(
    private val cordaNodeService: CordaNodeService,
    private val ruleConfiguration: EventDistributionRuleConfiguration,
    private val environment: Environment
) : EventDistributionService<CordaEventDestination> {

    override fun distributeEvent(enrichedEvent: EnrichedEvent, destinations: Set<CordaEventDestination>?): UUID {
        val destinationSet = destinations ?: runEventDistributionRules(enrichedEvent.eventRDF)
        log.info("Sending eventType: ${enrichedEvent.eventType.eventType} with eventUUID: ${enrichedEvent.eventUUID} to destination(s): ${destinationSet.map { it.destination }}")
        return cordaNodeService.startNewEventFlow(eventUUID = enrichedEvent.eventUUID.toString(), event = enrichedEvent.eventRDF, eventType = enrichedEvent.eventType.eventType, cordaNames = destinationSet.map { it.destination }.toSet())
    }

    private fun runEventDistributionRules(eventRdf: String): Set<CordaEventDestination> = ruleConfiguration.getDistributionRules().first { it.appliesTo(eventRdf) }.getDestinations().toSet()

    companion object {
        private val log = LoggerFactory.getLogger(CordaEventDistributionService::class.java)
    }
}