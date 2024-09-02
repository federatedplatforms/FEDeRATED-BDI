package nl.tno.federated.api.event.type

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.DefaultResourceLoader
import java.nio.charset.StandardCharsets

@ConfigurationProperties(prefix = "federated.node.event")
class EventTypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var eventType: String
        lateinit var rml: String
        var shacl: String? = null
        var schemaDefinition: String? = null
        private val resourceLoader = DefaultResourceLoader()

        fun toEventType() = EventType(eventType, rml.loadResourceAsString(), shacl?.loadResourceAsString(), schemaDefinition?.loadResourceAsString())

        fun String.loadResourceAsString() = resourceLoader.getResource(this).getContentAsString(StandardCharsets.UTF_8)
    }
}