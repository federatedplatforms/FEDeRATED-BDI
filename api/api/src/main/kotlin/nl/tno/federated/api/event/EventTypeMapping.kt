package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets.UTF_8

@ConfigurationProperties(prefix = "bdi.federated.event")
class EventTypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var eventType: String
        lateinit var rml: String
        var shacl: String? = null

        fun toEventType() = EventType(eventType, rml, shacl)
    }
}

@Component
class EventTypeMapping(val config: EventTypeMappingConfig) {

    private val resourceLoader: ResourceLoader = DefaultResourceLoader()
    private val log = LoggerFactory.getLogger(EventTypeMapping::class.java)

    fun getEventType(contentType: String): EventType? {
        return config.types.find { it.eventType == contentType }?.toEventType()
    }

    fun getEventTypes(): Map<String, EventType> {
        return config.types.associate {
            it.eventType to it.toEventType()
        }
    }

    fun readShacl(eventType: EventType) = if (eventType.shacl != null) {
        resourceToString(eventType.shacl)
    } else null

    fun readRml(eventType: EventType) = resourceToString(eventType.rml)

    fun readShaclShapes(): List<String> {
        return config.types.filter {
            it.shacl != null
        }.map {
            if (!resourceLoader.getResource(it.shacl!!).exists()) {
                log.warn("SHACL file cannot be read: {}", it.shacl)
                throw Exception("SHACL file cannot be read: ${it.shacl}")
            }
            resourceToString(it.shacl!!)
        }.toList()
    }

    private fun resourceToString(resource: String): String {
        return InputStreamReader(resourceLoader.getResource(resource).inputStream, UTF_8).use {
            FileCopyUtils.copyToString(it)
        }
    }
}
