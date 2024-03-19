package nl.tno.federated.api.controllers

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.tags.Tag
import nl.tno.federated.api.event.EventService
import nl.tno.federated.api.event.EventTypeMapping
import nl.tno.federated.api.event.mapper.EventType
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import nl.tno.federated.api.event.query.EventQuery
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

const val EVENT_TYPE_HEADER = "Event-Type"
const val EVENT_DESTINATION_HEADER = "Event-Destinations"

@RestController
@RequestMapping("/api/events")
@Tag(name = "EventsController", description = "Allows for creation, distribution and retrieval of events. See the /event-types endpoint for all supported event types by this node.")
class EventsController(
    private val eventService: EventService,
    private val eventTypeMapping: EventTypeMapping
) {

    companion object {
        private val log = LoggerFactory.getLogger(EventsController::class.java)
    }

    @Operation(summary = "Return the event data in compacted JSONLD format.")
    @GetMapping(path = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEventById(@PathVariable("id") id: String): ResponseEntity<JsonNode> {
        log.info("Get event by ID: {}", id)
        return ResponseEntity.ok(eventService.findEventById(id))
    }

    @Operation(summary = "Return the event data in compacted JSONLD format.")
    @GetMapping(path = [""], produces = [APPLICATION_JSON_VALUE])
    fun getEvents(@RequestParam("page", defaultValue = "1") page: Int, @RequestParam("size", defaultValue = "100") size: Int): ResponseEntity<List<JsonNode>> {
        log.info("Get all events, page: {}, size: {}", page, size)
        if (page < 1) throw InvalidPageCriteria("Page size should be greater than 0.")
        return ResponseEntity.ok().body(eventService.findAll(page, size))
    }

    @Operation(summary = "Create a new event and distribute to peers according to the distribution rules. The Event-Type header specifies the Event type e.g: federated.events.load-event.v1. See the /event-types endpoint for all supported event types by this node. Event-Destinations header specifies the node names to send the even to. Node names can be separated with a semi-colon (;). An example: O=Cargobase,L=Dusseldorf,C=DE")
    @PostMapping(path = [""], consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun postEvent(@RequestBody event: String, @RequestHeader(EVENT_TYPE_HEADER) eventType: String, @RequestHeader(name = EVENT_DESTINATION_HEADER, required = false) eventDestinations: String?): ResponseEntity<Void> {
        log.info("Received new event: {}", event)
        val type = contentTypeToEventType(eventType)
        val destinations: Set<String>? = eventDestinationsToSet(eventDestinations)
        val enrichedEvent = eventService.newJsonEvent(event, type, destinations)
        log.info("New event created with UUID: {}", enrichedEvent.eventUUID)
        return ResponseEntity.created(URI("/events/${enrichedEvent.eventUUID}")).build()
    }

    @PostMapping(path = ["/query"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [
                ExampleObject(
                    name = "Query additional information from the node related to specified event UUID.",
                    description = "eventUUID should exit in the Corda Vault.",
                    value = """{ "sparql" : "select * where { ?s ?p ?o . } limit 100", "eventUUID" : "asasd-asas234cda-sasw233ds" }"""
                )
            ]
        )]
    )
    fun query(@RequestBody eventQuery: EventQuery): ResponseEntity<JsonNode> {
        log.info("Executing event query: {}", eventQuery)
        return ResponseEntity.ok(eventService.query(eventQuery))
    }

    @Operation(summary = "Validate an event without distribution, returns the generated RDF if no validation errors occur. The Event-Type header specifies the type of event e.g: federated.events.load-event.v1. See the /event-types endpoint for all supported event types by this node.")
    @PostMapping(path = ["/validate"], consumes = [APPLICATION_JSON_VALUE], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun validateEvent(@RequestBody event: String, @RequestHeader(EVENT_TYPE_HEADER) eventType: String): ResponseEntity<String> {
        log.info("Validate new event: {}", event)
        val type = contentTypeToEventType(eventType)
        val rdf = eventService.validateNewJsonEvent(event, type)
        return ResponseEntity.ok(rdf.eventRDF)
    }

    private fun eventDestinationsToSet(eventDestinations: String?): Set<String>? {
        return eventDestinations?.split(";")?.toSet()
    }

    private fun contentTypeToEventType(contentType: String): EventType {
        return eventTypeMapping.getEventType(contentType) ?: throw UnsupportedEventTypeException(contentType)
    }
}