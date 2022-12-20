package nl.tno.federated.api.corda

import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.corda.flows.DataPullQueryFlow
import nl.tno.federated.corda.flows.GeneralSPARQLqueryFlow
import nl.tno.federated.corda.flows.NewEventFlow
import nl.tno.federated.corda.flows.QueryGraphDBbyIdFlow
import nl.tno.federated.states.DataPullState
import nl.tno.federated.states.EventState
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaNodeService(private val rpc: NodeRPCConnection) {

    fun startNewEventFlow(event: String, cordaName: CordaX500Name?): UUID {
        return startNewEventFlow(event = event, cordaNames = if (cordaName == null) emptySet() else setOf(cordaName))
    }

    fun startNewEventFlow(event: String, cordaNames: Set<CordaX500Name>): UUID {
        val newEventTx = rpc.client().startFlowDynamic(
            NewEventFlow::class.java,
            cordaNames,
            event
        ).returnValue.get()

        return (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
    }

    fun startNewQueryGraphDBbyIdFlow(id: String): String {
        return rpc.client().startFlowDynamic(
            QueryGraphDBbyIdFlow::class.java,
            id
        ).returnValue.get()
    }

    fun startNewGeneralSPARQLqueryFlow(query: String): String {
        return rpc.client().startFlowDynamic(
            GeneralSPARQLqueryFlow::class.java,
            query
        ).returnValue.get()
    }

    fun startVaultQuery(): List<EventState> {
        return rpc.client().vaultQuery(EventState::class.java).states.map { it.state.data }
    }

    fun startVaultQueryBy(criteria: QueryCriteria.LinearStateQueryCriteria): List<EventState> {
        return rpc.client().vaultQueryBy<EventState>(criteria).states.map { it.state.data }
    }

    fun startDataPullFlow(query: String, cordaName: CordaX500Name?): UUID {
        val dataPull = rpc.client().startFlowDynamic(
            DataPullQueryFlow::class.java,
            cordaName,
            query
        ).returnValue.get()
        return (dataPull.coreTransaction.getOutput(0) as DataPullState).linearId.id
    }

    fun getDataPullResults(uuid: String): List<String> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(uuid)))
        return rpc.client().vaultQueryBy<DataPullState>(criteria).states
            .flatMap { it.state.data.result }

    }

    /**
     * Given UUID of event state, returns the certificate of the node that
     * initiated the transaction that created that state.
     *
     * Assumption:  the initiator of the transaction who created the event is always
     *              the first element of the list `participants` in the state.
     */
    fun extractSender(eventuuid: String): Party {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(eventuuid)))
        val eventStateParties = rpc.client().vaultQueryBy<DataPullState>(criteria).states.single().state.data.participants

        val me = eventStateParties.first()
        val eventStateCounterParty = (eventStateParties - me).single()

        return rpc.client().partyFromKey(eventStateCounterParty.owningKey)!!
    }
}