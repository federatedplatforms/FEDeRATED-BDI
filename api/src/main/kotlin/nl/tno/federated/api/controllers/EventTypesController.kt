package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.tags.Tag
import nl.tno.federated.api.event.ContentTypeToEventType
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event-types", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "EventTypesController", description = "Returns info regarding the supported event types by this node.")
class EventTypesController(private val contentTypeToEventType: ContentTypeToEventType) {
    @GetMapping()
    fun getEventTypes() = contentTypeToEventType.getEventTypes()

}