package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.lang.Exception
import java.nio.charset.StandardCharsets.UTF_8

@Configuration
@ConfigurationProperties(prefix = "bdi.federated.event")
class EventTypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var eventType: String
        lateinit var name: String
        lateinit var rml: String
        var shacl: String? = null

        fun toEventType() = EventType(name, rml, shacl)
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

    fun readShaclShapes(): List<String> {
        return config.types.filter {
            it.shacl != null
        }.map {
            if(!resourceLoader.getResource(it.shacl!!).exists()) {
                log.warn("SHACL file cannot be read: {}", it.shacl)
                throw Exception("SHACL file cannot be read: ${it.shacl}")
            }
            InputStreamReader(resourceLoader.getResource(it.shacl!!).inputStream, UTF_8).use {
                FileCopyUtils.copyToString(it);
            }
        }.toList()
    }
}
