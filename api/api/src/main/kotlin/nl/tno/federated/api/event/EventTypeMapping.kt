package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.nio.charset.Charset
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
        if(existing != null) {
            throw EventTypeMappingException("Existing EventType found with same name: ${eventType.eventType}")
        }
        eventTypeService.addEventType(eventType)
    }

    fun deleteEventType(eventType: String) {
        eventTypeService.delete(eventType)
    }

    fun getEventType(contentType: String): EventType? {
        return config.types.find { it.eventType == contentType }?.toEventType()
    }

    fun getEventTypes(): List<EventType> {
        val configured = config.types.map { it.toEventType() }
        val findAll = eventTypeService.getAllEventTypes()
        return configured + findAll
    }

    fun readShacl(eventType: EventType) = if (eventType.shacl != null) {
        eventType.shacl
    } else null

    fun readRml(eventType: EventType) = eventType.rml

    fun readShaclShapes(): List<String> {
        return eventTypeService.getAllEventTypes().mapNotNull { it.shacl }
    }
}
