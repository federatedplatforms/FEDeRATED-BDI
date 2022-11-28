package nl.tno.federated.semantic.adapter.tradelens

import nl.tno.federated.semantic.adapter.core.TripleService
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service

@Service
class TradelensTripleService(private val tradelensMapper: TradelensMapper) : TripleService() {

    private val eventRules = ClassPathResource("tradelens/event_rules.ttl")
    private val containersRules = ClassPathResource("tradelens/container_rules.ttl")

    fun createTriplesForEvents(jsonData: String, baseUri: String? = null): String? {
        val preProcessedData = tradelensMapper.createPreMappingEvents(jsonData).first
        return createTriples(preProcessedData, eventRules, baseUri)
    }

    fun createTriplesForContainers(jsonData: String, baseUri: String? = null): String? {
        val preProcessedData = tradelensMapper.createPreMappingContainers(jsonData).first
        return createTriples(preProcessedData, containersRules, baseUri)
    }
}