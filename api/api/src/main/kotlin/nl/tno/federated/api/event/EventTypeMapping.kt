package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets.UTF_8

@ConfigurationProperties(prefix = "federated.node.event")
class EventTypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var eventType: String
        lateinit var rml: String
        var shacl: String? = null
        private val resourceLoader = DefaultResourceLoader()

        fun toEventType() = EventType(eventType, rml.loadResourceAsString(), shacl?.loadResourceAsString())

        fun String.loadResourceAsString() = resourceLoader.getResource(this).getContentAsString(UTF_8)
    }
}

class EventTypeMappingException(message: String) : Exception(message)

@Component
class EventTypeMapping(
    private val config: EventTypeMappingConfig,
    private val eventTypeService: EventTypeService
) {

    private val log = LoggerFactory.getLogger(EventTypeMapping::class.java)

    fun addEventType(eventType: EventType) {
        val existing = getEventTypes().firstOrNull { it.eventType.equals(eventType.eventType, true) }
        if (existing != null) {
            throw EventTypeMappingException("Existing EventType found with same name: ${eventType.eventType}")
        }
        eventTypeService.addEventType(eventType)
    }

    fun deleteEventType(eventType: String) {
        eventTypeService.deleteEventType(eventType)
    }

    fun getEventType(eventType: String): EventType? {
        return config.types.find { it.eventType == eventType }?.toEventType()
    }

    fun getEventTypes(): List<EventType> {
        val configured = config.types.map { it.toEventType() }
        val findAll = eventTypeService.getAllEventTypes()
        return configured + findAll
    }

    fun readShaclShapes(): List<String> {
        return eventTypeService.getAllEventTypes().mapNotNull { it.shacl }
    }

    fun updateShacl(eventType: String, shacl: String) {
        val current = eventTypeService.getEventType(eventType)
            ?: throw EventTypeMappingException("EventType not found: ${eventType}")
        eventTypeService.updateEventType(current.copy(shacl = shacl))
    }

    fun updateRml(eventType: String, rml: String) {
        val current = eventTypeService.getEventType(eventType)
            ?: throw EventTypeMappingException("EventType not found: ${eventType}")
        eventTypeService.updateEventType(current.copy(rml = rml))
    }
}
