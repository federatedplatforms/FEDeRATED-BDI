package nl.tno.federated.api.event.query.corda

import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
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
        val sender = getEventSender(query.eventUUID) ?: throw Exception("Cannot determine sender of Event with UUID: ${query.eventUUID}")
        return searchRemoteNode(query, sender)
    }

    override fun findById(id: String): String? {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        // TODO when we add a UUID to the Event, this needs to change
        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = cordaNodeService.startVaultQueryBy(criteria)
        return state.firstOrNull()?.event
    }

    override fun findAll(): List<String> = findAll(1, 100)

    fun findAll(page: Int, size: Int): List<String> {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return listOf(dummy)
        val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val pagingSpec = PageSpecification(pageNumber = page, pageSize = size)
        val state = cordaNodeService.startVaultQueryBy(criteria, pagingSpec)
        return state.map { it.event }
    }

    private fun searchRemoteNode(query: EventQuery, remote: CordaX500Name): String? {
        if (environment.getProperty("demo.mode", Boolean::class.java) == true) return dummy
        val stateId = cordaNodeService.startDataPullFlow(query.sparql, remote)
        // TODO need to check if the flow has been completed, only then we can fetch the result.
        // https://docs.r3.com/en/platform/corda/4.9/enterprise/node/operating/querying-flow-data.html#view-summary-information-for-a-suspended-flow
        return cordaNodeService.getDataPullResults(stateId)
    }

    private fun getEventSender(eventId: String): CordaX500Name? {
        return cordaNodeService.extractSender(eventId)?.name
    }

    companion object {
        private val dummy = DUMMY_DATA_LOAD_EVENT
    }
}