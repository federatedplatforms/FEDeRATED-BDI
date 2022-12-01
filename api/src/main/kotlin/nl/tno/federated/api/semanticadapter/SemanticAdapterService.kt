package nl.tno.federated.api.semanticadapter

import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.api.tradelens.TradelensService
import nl.tno.federated.corda.flows.InsertRDFFlow
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import nl.tno.federated.semantic.adapter.tradelens.TradelensTripleService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

class SemanticAdapterException(message: String) : Exception(message)

@Service
class SemanticAdapterService(
    private val rpc: NodeRPCConnection,
    private val tradelensService: TradelensService,
    private val tradelensTripleService: TradelensTripleService
) {

    private val log = LoggerFactory.getLogger(SemanticAdapterService::class.java)

    fun convertEventData(dataFromApi: String): String? {
        log.info("convertEventData for incoming Tradelens event")
        return tradelensTripleService.createTriplesForEvents(dataFromApi)
    }

    fun convertDigitalTwinData(dataFromApi: String): String? {
        log.info("convertDigitalTwinData for incoming Tradelens data")
        return tradelensTripleService.createTriplesForContainers(dataFromApi)
    }

    /**
     * Convert Tradelens event data to triples using the semantic adapter
     * Enrich the event with additional tradelens data and store in graphdb
     * Return the event triple from the semantic adapter
     */
    fun processTradelensEvent(event: String): String {
        log.info("Processing incoming Tradelens event")
        val convertedEvent = convertEventData(event)
        log.debug("Tradelens event converted: {}", convertedEvent)

        if (convertedEvent.isNullOrEmpty()) {
            log.warn("Unable to convert event to triple, empty response from semantic adapter for event: {}", event)
            throw SemanticAdapterException("Unable to convert event to triple, empty response from semantic adapter for incoming event.")
        }

        retrieveAndStoreExtraData(convertedEvent)
        return convertedEvent
    }

    /**
     * Enrich the event with more tradelens data and store in graphdb
     */
    private fun retrieveAndStoreExtraData(event: String): Boolean {
        log.info("Retrieving extra data for event.")
        val digitalTwinIdsAndConsignmentIds = parseDTIdsAndBusinessTransactionIds(event)

        digitalTwinIdsAndConsignmentIds.forEach {
            val consignmentId = it.value
            it.key.forEach { twinId ->
                log.info("Getting data for consignmentId: {} and twinId: {}", consignmentId, twinId)
                val dataFromApi = tradelensService.getTransportEquipmentData(consignmentId, twinId)

                if (dataFromApi.isNullOrEmpty()) {
                    log.warn("Unable to get transportEquipment data from Tradelens, empty response for consignmentId: {} and twinId: {}", consignmentId, twinId)
                    throw SemanticAdapterException("Unable to get transportEquipment data from Tradelens, empty response for consignmentId: $consignmentId and twinId: $twinId")
                }

                val convertedData = convertDigitalTwinData(dataFromApi)
                log.debug("TransportEquipmentData converted to: {}", dataFromApi)

                if (convertedData.isNullOrEmpty()) {
                    log.warn("Unable to convert Tradelens transportEquipment to triple, empty response from semantic adapter for Tradelens data: {}", dataFromApi)
                    throw SemanticAdapterException("Unable to convert Tradelens transportEquipment to triple, empty response from semantic adapter for Tradelens data.")
                }

                log.debug("Inserting extra data into graphdb: {}", convertedData)
                insertDataIntoPrivateGraphDB(convertedData)
            }
        }
        return true
    }

    /**
     * This is a temporary solution, we are storing tradelens data for the data pull that happens at a later moment in time.
     */
    private fun insertDataIntoPrivateGraphDB(dataFromApi: String): Boolean {
        return rpc.client().startFlowDynamic(InsertRDFFlow::class.java, dataFromApi, true).returnValue.get()
    }

    private fun parseDTIdsAndBusinessTransactionIds(event: String): Map<List<UUID>, String> {
        val parsedEvent = GraphDBEventConverter.parseRDFToEvents(event)
        return parsedEvent.associate { it.allEvents().flatten() to it.businessTransaction }
    }
}