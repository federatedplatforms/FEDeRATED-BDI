package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import nl.tno.federated.api.event.EventTypeMapping
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/event-types", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "EventTypesController", description = "Returns info regarding the supported event types by this node.")
class EventTypesController(private val eventTypeMapping: EventTypeMapping) {
    @GetMapping()
    fun getEventTypes() = eventTypeMapping.getEventTypes()

    @GetMapping("/{type}/shacl", produces = ["text/turtle"])
    fun getShacl(@PathVariable type: String) = eventTypeMapping.getEventTypes()[type]?.let {
        eventTypeMapping.readShacl(it)
    }

    @GetMapping("/{type}/rml", produces = ["text/turtle"])
    fun getRml(@PathVariable type: String) = eventTypeMapping.getEventTypes()[type]?.let {
        eventTypeMapping.readRml(it)
    }
}