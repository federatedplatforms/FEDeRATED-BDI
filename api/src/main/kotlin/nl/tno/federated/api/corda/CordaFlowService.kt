package nl.tno.federated.api.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.corda.flows.NewEventFlow
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
                // Event
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

}