package nl.tno.federated.api.event.distribution.corda

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.EventDistributionRuleConfiguration
import nl.tno.federated.api.event.distribution.EventDistributionService
import nl.tno.federated.api.event.mapper.EventType
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaEventDistributionService(
    private val cordaNodeService: CordaNodeService,
    private val ruleConfiguration: EventDistributionRuleConfiguration,
    private val environment: Environment
) : EventDistributionService<CordaEventDestination> {

    override fun distributeEvent(eventUUID: UUID, event: String, eventType: EventType, destinations: Set<CordaEventDestination>?): UUID {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return UUID.randomUUID()
        val destinationSet = destinations ?: runEventDistributionRules(event)
        return cordaNodeService.startNewEventFlow(eventUUID = eventUUID.toString(), event = event, eventType = eventType.name, cordaNames = destinationSet.map { it.destination }.toSet())
    }

    private fun runEventDistributionRules(eventRdf: String): Set<CordaEventDestination> = ruleConfiguration.getDistributionRules().first { it.appliesTo(eventRdf) }.getDestinations().toSet()
}