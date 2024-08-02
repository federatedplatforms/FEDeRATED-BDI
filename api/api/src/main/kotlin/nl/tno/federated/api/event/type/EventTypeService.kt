package nl.tno.federated.api.event.type

import org.springframework.stereotype.Service

class EventTypeServiceException(msg: String) : Exception(msg)

@Service
class EventTypeService(private val eventTypeRepository: EventTypeRepository) {

    fun addEventType(e: EventType): EventTypeEntity {
        val current = eventTypeRepository.findByEventType(eventType = e.eventType)
        if( current != null) throw EventTypeServiceException("EventType already exists: ${e.eventType}")
        return eventTypeRepository.save(EventTypeEntity(eventType = e.eventType, rml = e.rml, shacl = e.shacl, schemaDefinition = e.schemaDefinition) )
    }

    fun getAllEventTypes(): List<EventType> {
        return eventTypeRepository.findAll().map { EventType(eventType = it.eventType, rml = it.rml, shacl = it.shacl, schemaDefinition = it.schemaDefinition) }
    }

    fun deleteEventType(eventType: String) {
        val current = eventTypeRepository.findByEventType(eventType = eventType)
            ?: throw EventTypeServiceException("No EventType found: ${eventType}")
        eventTypeRepository.delete(current)
    }

    fun updateEventType(update: EventType) {
        val current = eventTypeRepository.findByEventType(eventType = update.eventType)
            ?: throw EventTypeServiceException("No EventType found: ${update.eventType}")
        val copy = current.copy(rml = update.rml, shacl = update.shacl)
        eventTypeRepository.save(copy)
    }

    fun getEventType(eventType: String): EventType? {
        val entity = eventTypeRepository.findByEventType(eventType)
        return if(entity == null)  null
        else EventType(eventType = entity.eventType, rml = entity.rml, shacl = entity.shacl, schemaDefinition = entity.schemaDefinition)
    }
}