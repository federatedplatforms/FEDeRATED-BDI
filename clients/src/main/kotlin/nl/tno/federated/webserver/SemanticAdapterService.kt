package nl.tno.federated.webserver

import nl.tno.federated.services.GraphDBService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

class SemanticAdapterException(message: String) : Exception(message)

@Service
class SemanticAdapterService(
    @Autowired @Qualifier("semanticAdapterRestTemplate") private val semanticAdapterRestTemplate: RestTemplate,
    private val tradelensService: TradelensService
) {

    private val log = LoggerFactory.getLogger(SemanticAdapterService::class.java)

    fun convertEventData(dataFromApi: String): String? {
        log.debug("Calling /tradelens/events for: {}", dataFromApi)
        return semanticAdapterRestTemplate.exchange("/tradelens-events", HttpMethod.POST, HttpEntity(dataFromApi), String::class.java).body
    }

    fun convertDigitalTwinData(dataFromApi: String): String? {
        log.debug("Calling /tradelens/containers for: {}", dataFromApi)
        return semanticAdapterRestTemplate.exchange("/tradelens-containers", HttpMethod.POST, HttpEntity(dataFromApi), String::class.java).body
    }

    /**
     * Convert Tradelens event data to triples using the semantic adapter
     * Enrich the event with additional tradelens data and store in graphdb
     * Return the event triple from the semantic adapter
     */
    fun processTradelensEvent(event: String): String {
        log.debug("Processing incoming Tradelens event")
        val convertedEvent = convertEventData(event)
        log.debug("Event converted: {}", convertedEvent)

        if(convertedEvent.isNullOrEmpty()) {
            log.warn("Unable to convert event to triple, empty response from semantic adapter for event: {}", event)
            throw SemanticAdapterException("Unable to convert event to triple, empty response from semantic adapter for incoming event.")
        }

        retrieveAndStoreExtraData(convertedEvent!!)
        return convertedEvent
    }

    /**
     * Enrich the event with more tradelens data and store in graphdb
     */
    private fun retrieveAndStoreExtraData(event: String): Boolean {
        log.debug("Retrieving extra data for event.")
        val digitalTwinIdsAndConsignmentIds = parseDTIdsAndBusinessTransactionIds(event)

        digitalTwinIdsAndConsignmentIds.forEach {
            val consignmentId = it.value
            it.key.forEach { twinId ->
                log.debug("Getting data for consignmentId: {} and twinId: {}", consignmentId, twinId)
                val dataFromApi = tradelensService.getTransportEquipmentData(consignmentId, twinId)

                if(dataFromApi.isNullOrEmpty()) {
                    log.warn("Unable to get transportEquipment data from Tradelens, empty response for consignmentId: {} and twinId: {}", consignmentId, twinId)
                    throw SemanticAdapterException("Unable to get transportEquipment data from Tradelens, empty response for consignmentId: $consignmentId and twinId: $twinId")
                }

                val convertedData = convertDigitalTwinData(dataFromApi!!)

                if(convertedData.isNullOrEmpty()) {
                    log.warn("Unable to convert Tradelens transportEquipment to triple, empty response from semantic adapter for Tradelens data: {}", dataFromApi)
                    throw SemanticAdapterException("Unable to convert Tradelens transportEquipment to triple, empty response from semantic adapter for Tradelens data.")
                }

                log.debug("Inserting extra data  {into graphdb: {}", convertedData)
                insertDataIntoPrivateGraphDB(convertedData!!)
            }
        }
        return true
    }

    /**
     * This is a temporary solution, we are storing tradelens data for the data pull that happens at a later moment in time.
     */
    private fun insertDataIntoPrivateGraphDB(dataFromApi: String): Boolean {
        return GraphDBService.insertEvent(dataFromApi, true)
    }

    private fun parseDTIdsAndBusinessTransactionIds(event: String): Map<List<UUID>, String> {
        val parsedEvent = GraphDBService.parseRDFToEvents(event)
        return parsedEvent.associate { it.allEvents().flatten() to it.businessTransaction }
    }

}