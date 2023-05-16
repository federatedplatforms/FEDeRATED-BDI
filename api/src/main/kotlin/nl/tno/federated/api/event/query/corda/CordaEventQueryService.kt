package nl.tno.federated.api.event.query.corda

import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.DUMMY_DATA_LOAD_EVENT
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.event.query.EventQueryService
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class CordaEventQueryService(
    private val cordaNodeService: CordaNodeService,
    private val environment: Environment
) : EventQueryService {

    override fun executeQuery(query: EventQuery): String? {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        val sender = if (query.eventId != null) getEventSender(query.eventId) else null
        val result = when (sender) {
            null -> searchLocalIndex(query)
            else -> searchRemoteNode(query, sender)
        }
        return result
    }

    override fun findById(id: String): String? {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = cordaNodeService.startVaultQueryBy(criteria)
        return state.firstOrNull()?.fullEvent
    }

    private fun searchLocalIndex(query: EventQuery): String {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        return cordaNodeService.startNewGeneralSPARQLqueryFlow(query.queryString)
    }

    private fun searchRemoteNode(query: EventQuery, remote: CordaX500Name): String? {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        val stateId = cordaNodeService.startDataPullFlow(query.queryString, remote).toString()

        val result = cordaNodeService.getDataPullResults(stateId)
        return result.firstOrNull()
    }

    private fun getEventSender(eventId: String): CordaX500Name? {
        return cordaNodeService.extractSender(eventId)?.name
    }

    private val dummy = DUMMY_DATA_LOAD_EVENT

}