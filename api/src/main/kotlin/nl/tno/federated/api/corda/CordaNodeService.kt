package nl.tno.federated.api.corda

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.DEFAULT_PAGE_NUM
import net.corda.core.node.services.vault.DEFAULT_PAGE_SIZE
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.Sort
import net.corda.core.node.services.vault.SortAttribute
import nl.tno.federated.corda.flows.DataPullQueryFlow
import nl.tno.federated.corda.flows.NewEventFlow
import nl.tno.federated.states.DataPullState
import nl.tno.federated.states.EventState
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaNodeService(private val rpc: NodeRPCConnection) {

    fun startNewEventFlow(event: String, eventType: String, cordaNames: Set<CordaX500Name>): UUID {
        if (cordaNames.isEmpty()) throw NoEventDestinationsAvailableException("No event destinations found to send the event to.")
        val newEventTx = rpc.client().startFlowDynamic(
            NewEventFlow::class.java,
            cordaNames,
            event,
            eventType
        ).returnValue.get()

        return (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
    }

    fun startVaultQueryBy(criteria: QueryCriteria, pagingSpec: PageSpecification = PageSpecification(DEFAULT_PAGE_NUM, DEFAULT_PAGE_SIZE)): List<SimpleEventState> {
        return rpc.client().vaultQueryPagedAndSortedByRecordedTime<EventState>(pagingSpec).map { it.toSimpleEventState() }
    }

    fun startDataPullFlow(query: String, cordaName: CordaX500Name?): UUID {
        val dataPull = rpc.client().startFlowDynamic(
            DataPullQueryFlow::class.java,
            cordaName,
            query
        ).returnValue.get()

        return (dataPull.coreTransaction.getOutput(0) as DataPullState).linearId.id
    }

    fun getDataPullResults(uuid: UUID): String? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(uuid))
        return rpc.client().vaultQueryBy<DataPullState>(criteria).states.firstOrNull()?.state?.data?.results
    }

    /**
     * Given UUID of event state, returns the certificate of the node that
     * initiated the transaction that created that state.
     *
     * Assumption:  the initiator of the transaction who created the event is always
     *              the first element of the list `participants` in the state.
     */
    fun extractSender(eventId: String): Party? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(eventId)))
        val eventStateParties = rpc.client().vaultQueryBy<EventState>(criteria).states.single().state.data.participants

        val me = rpc.client().nodeInfo().legalIdentities.first()
        val eventStateCounterParty = (eventStateParties - me).singleOrNull() ?: return null

        return rpc.client().partyFromKey(eventStateCounterParty.owningKey)
    }

    fun getNetworkMapSnapshot() = rpc.client().networkMapSnapshot()
}


inline fun <reified T : ContractState> CordaRPCOps.vaultQueryPagedAndSortedByRecordedTime(pageSpec: PageSpecification): List<T> {
    val sortAttribute = SortAttribute.Standard(Sort.VaultStateAttribute.RECORDED_TIME)
    val sorter = Sort(setOf(Sort.SortColumn(sortAttribute, Sort.Direction.DESC)))
    val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
    return this.vaultQueryBy<T>(criteria, pageSpec, sorter).states.map { it.state.data }
}