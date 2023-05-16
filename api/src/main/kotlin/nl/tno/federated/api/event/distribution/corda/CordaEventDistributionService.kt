package nl.tno.federated.api.event.distribution.corda

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.EventDistributionService
import nl.tno.federated.api.event.distribution.rules.EventDistributionRule
import nl.tno.federated.api.event.distribution.rules.SparqlEventDistributionRule
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaEventDistributionService(
    private val cordaNodeService: CordaNodeService,
    private val rules: List<EventDistributionRule<CordaEventDestination>>,
    private val environment: Environment
) : EventDistributionService<CordaEventDestination> {

    override fun distributeEvent(event: String, destinations: Set<CordaEventDestination>?): UUID {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return UUID.randomUUID()
        val destinationSet = destinations ?: runEventDistributionRules(event)
        return cordaNodeService.startNewEventFlow(event = event, cordaNames = destinationSet.map { it.destination }.toSet())
    }

    private fun runEventDistributionRules(eventRdf: String): List<CordaEventDestination> = rules.first { it.appliesTo(eventRdf) }.getDestinations()
}