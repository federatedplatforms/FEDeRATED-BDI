package nl.tno.federated.api.corda

import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.corda.flows.GeneralSPARQLqueryFlow
import nl.tno.federated.corda.flows.NewEventFlow
import nl.tno.federated.corda.flows.QueryGraphDBbyIdFlow
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import nl.tno.federated.states.EventState
import org.springframework.stereotype.Service
import java.util.*

@Service
class CordaFlowService (private val rpc: NodeRPCConnection)
{
    fun extractDestinationFromEvent(event: String): CordaX500Name? {
        return rpc.client().networkMapSnapshot().flatMap { it.legalIdentities }.singleOrNull {
            it.name.locality.equals(
                GraphDBEventConverter.parseRDFToCity(event)
            )
        }?.name
    }

    fun startNewEventFlow(event: String, cordaName: CordaX500Name?): UUID {
        val newEventTx = rpc.client().startFlowDynamic(
            NewEventFlow::class.java,
            if (cordaName == null) emptySet() else setOf(cordaName),
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

}