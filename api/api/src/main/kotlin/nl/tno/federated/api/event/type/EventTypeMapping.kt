package nl.tno.federated.api.event.type

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component



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
        return getEventTypes().find { it.eventType == eventType }
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

    fun updateSchemaDefinition(eventType: String, schema: String) {
        val current = eventTypeService.getEventType(eventType)
            ?: throw EventTypeMappingException("EventType not found: ${eventType}")
        eventTypeService.updateEventType(current.copy(schemaDefinition = schema))
    }

    fun updateRml(eventType: String, rml: String) {
        val current = eventTypeService.getEventType(eventType)
            ?: throw EventTypeMappingException("EventType not found: ${eventType}")
        eventTypeService.updateEventType(current.copy(rml = rml))
    }
}
