package nl.tno.federated.api.distribution

import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import org.springframework.stereotype.Service

@Service
class CordaEventDistributionService(private val rpc: NodeRPCConnection) : EventDistributionService<String> {

    /**
     * We look inside the event data for a country field and map it to the Corda nodes in that country.
     * The nodes are retrieved using the corda network map snapshot
     */
    override fun extractDestinationsFromEvent(event: String): Set<EventDestination> {
        return rpc.client().networkMapSnapshot().flatMap { it.legalIdentities }.filter {
            it.name.country == GraphDBEventConverter.parseRDFtoCountries(event).single()
        }.map { EventDestination(it.name) }.toSet()
    }
}