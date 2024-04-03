package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import org.springframework.stereotype.Service

@Service
class EventTypeService(private val eventTypeRepository: EventTypeRepository) {

    fun addEventType(e: EventType): EventTypeEntity {
        return eventTypeRepository.save(EventTypeEntity(eventType = e.eventType, rml = e.rml, shacl = e.shacl) )
    }

    fun getAllEventTypes(): List<EventType> {
        return eventTypeRepository.findAll().map { EventType(eventType = it.eventType, rml = it.rml, shacl = it.shacl) }
    }

    fun delete(eventType: String) {
        eventTypeRepository.deleteByEventType(eventType)
    }
}