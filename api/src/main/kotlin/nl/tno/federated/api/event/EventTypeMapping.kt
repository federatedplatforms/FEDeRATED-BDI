package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import org.springframework.util.FileCopyUtils
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets.UTF_8

@Configuration
@ConfigurationProperties(prefix = "bdi.federated.event")
class TypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var contentType: String
        lateinit var name: String
        lateinit var rml: String
        var shacl: String? = null

        fun toEventType() = EventType(name, rml, shacl)
    }
}

@Component
class EventTypeMapping(val config: TypeMappingConfig) {

    fun getEventType(contentType: String): EventType? {
        return config.types.find { it.contentType == contentType }?.toEventType()
    }

    fun getEventTypes(): Map<String, EventType> {
        return config.types.associate {
            it.contentType to it.toEventType()
        }
    }

    fun readShaclShapes(): List<String> {
        return config.types.filter {
            it.shacl != null
        }.map {
            InputStreamReader(ClassPathResource(it.shacl!!).inputStream, UTF_8).use {
                FileCopyUtils.copyToString(it);
            }
        }.toList()
    }
}
