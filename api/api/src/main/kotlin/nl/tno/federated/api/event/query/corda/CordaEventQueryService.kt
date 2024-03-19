package nl.tno.federated.api.event.query.corda

import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.corda.SimpleEventState
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.event.query.EventQueryService
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class CordaEventQueryService(
    private val cordaNodeService: CordaNodeService,
    private val environment: Environment
) : EventQueryService {

    private val log = LoggerFactory.getLogger(CordaEventQueryService::class.java)

    override fun executeQuery(query: EventQuery): String? {
        val sender = getEventSender(query.eventUUID) ?: throw Exception("Cannot determine sender of Event with UUID: ${query.eventUUID}")
        return searchRemoteNode(query, sender)
    }

    override fun findById(id: String): String? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = cordaNodeService.startVaultQueryBy(criteria)
        return state.firstOrNull()?.eventData
    }

    override fun findAll(): List<String> = findAll(1, 100).map { it.eventData }

    fun findAll(page: Int, size: Int): List<SimpleEventState> {
        val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val pagingSpec = PageSpecification(pageNumber = page, pageSize = size)
        return cordaNodeService.startVaultQueryBy(criteria, pagingSpec)
    }

    private fun searchRemoteNode(query: EventQuery, remote: CordaX500Name): String? {
        log.info("Sending EventQuery to remote node: {}", remote)
        val stateId = cordaNodeService.startDataPullFlow(query.sparql, remote)
        // TODO need to check if the flow has been completed, only then we can fetch the result.
        // https://docs.r3.com/en/platform/corda/4.9/enterprise/node/operating/querying-flow-data.html#view-summary-information-for-a-suspended-flow
        return cordaNodeService.getDataPullResults(stateId)
    }

    private fun getEventSender(eventId: String): CordaX500Name? {
        return cordaNodeService.extractSender(eventId)?.name
    }
}