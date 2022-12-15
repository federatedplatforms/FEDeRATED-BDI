package nl.tno.federated.api.distribution

import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import org.springframework.stereotype.Service

@Service
class EventDistributionService(private val rpc: NodeRPCConnection) {

    /**
     * This method determines based on the incoming message which node to send this message to.
     * It returns the EventDestination, containing the node where to send this event to.
     * In this prototype it uses Corda network map snapshot to figure out which country belongs to the given event.
     */
    fun extractDestinationFromEvent(event: String): EventDestination? {
        return rpc.client().networkMapSnapshot().flatMap { it.legalIdentities }.singleOrNull {
            it.name.country == GraphDBEventConverter.parseRDFtoCountries(event).single()
        }?.name?.let { EventDestination(it) }
    }
}