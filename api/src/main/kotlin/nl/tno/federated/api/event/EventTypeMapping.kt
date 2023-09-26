package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
@ConfigurationProperties(prefix = "bdi.federated.event")
class TypeMappingConfig(val types: List<Type>) {
    class Type {
        lateinit var contentType: String
        lateinit var name: String
        lateinit var rml: String
        lateinit var shacl: String

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

    // TODO read shapes
    fun getShaclShapes(): List<String> = arrayListOf()
}