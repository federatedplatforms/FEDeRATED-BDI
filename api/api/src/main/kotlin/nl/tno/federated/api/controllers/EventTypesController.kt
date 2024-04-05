package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import nl.tno.federated.api.event.EventTypeMapping
import nl.tno.federated.api.event.mapper.EventType
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/event-types", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "EventTypesController", description = "Returns info regarding the supported event types by this node.")
class EventTypesController(private val eventTypeMapping: EventTypeMapping) {

    @GetMapping
    fun getEventTypes(): List<EventType> {
        return eventTypeMapping.getEventTypes()
    }

    @PostMapping
    fun newEventType(@Valid @RequestBody eventType: EventType) {
        eventTypeMapping.addEventType(eventType)
    }

    @DeleteMapping("/{eventType}")
    fun deleteEventType(@PathVariable eventType: String) {
        eventTypeMapping.deleteEventType(eventType)
    }

    @GetMapping("/{type}/shacl", produces = ["text/turtle"])
    fun getShacl(@PathVariable type: String): String? {
        return eventTypeMapping.getEventTypes().firstOrNull { it.eventType == type }?.shacl
    }

    @PostMapping("/{type}/shacl", consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun updateShacl(@PathVariable type: String, @RequestBody shacl: String) {
        eventTypeMapping.updateShacl(type, shacl)
    }

    @GetMapping("/{type}/rml", produces = ["text/turtle"])
    fun getRml(@PathVariable type: String): String? {
        return eventTypeMapping.getEventType( type )?.rml
    }

    @PostMapping("/{type}/rml", consumes = [MediaType.TEXT_PLAIN_VALUE])
    fun updateRml(@PathVariable type: String, @RequestBody rml: String) {
        eventTypeMapping.updateRml(type, rml)
    }
}