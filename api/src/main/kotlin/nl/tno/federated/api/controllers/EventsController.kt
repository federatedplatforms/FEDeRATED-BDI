package nl.tno.federated.api.controllers

import com.github.jsonldjava.utils.JsonUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.tags.Tag
import nl.tno.federated.api.ArrivalEventApi
import nl.tno.federated.api.LoadEventApi
import nl.tno.federated.api.event.EventService
import nl.tno.federated.api.event.EventWithDestinations
import nl.tno.federated.api.event.query.EventQuery
import nl.tno.federated.api.model.ArrivalEvent
import nl.tno.federated.api.model.LoadEvent
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.*

@RestController
@RequestMapping("/events")
@Tag(name = "EventsController")
class EventsController(
    private val eventService: EventService
) : ArrivalEventApi, LoadEventApi {

    companion object {
        private val log = LoggerFactory.getLogger(EventsController::class.java)
    }

    override fun arrivalEventPost(arrivalEvent: ArrivalEvent): ResponseEntity<ArrivalEvent> {
        val rdf = eventService.convertEventToRDF(arrivalEvent)
        val uuid = eventService.publishRDFEvent(rdf, ArrivalEvent::class.java.simpleName)
        log.info("ArrivalEvent published with UUID: {}", uuid)
        return ResponseEntity
            .created(URI("/events/ArrivalEvent/${uuid}"))
            .body(arrivalEvent)
    }

    override fun arrivalEventResourceIdGet(resourceId: String): ResponseEntity<String> {
        return ResponseEntity.ok(JsonUtils.toString(eventService.findEventById(resourceId)))
    }

    override fun loadEventPost(loadEvent: LoadEvent): ResponseEntity<LoadEvent> {
        val rdf = eventService.convertEventToRDF(loadEvent)
        val uuid = eventService.publishRDFEvent(rdf, LoadEvent::class.java.simpleName)
        log.info("LoadEvent published with UUID: {}", uuid)
        return ResponseEntity
            .created(URI("/events/LoadEvent/${uuid}"))
            .body(loadEvent)
    }

    override fun loadEventResourceIdGet(resourceId: String): ResponseEntity<String> {
        return ResponseEntity.ok(JsonUtils.toString(eventService.findEventById(resourceId)))
    }

    @Operation(summary = "Return the event data in compacted JSONLD format.")
    @GetMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEvents(@RequestParam("page", defaultValue = "1") page: Int, @RequestParam("size", defaultValue = "100") size: Int): ResponseEntity<List<Map<String, Any>>> {
        log.info("Get all events, page: {}, size: {}", page, size)
        if(page < 1) throw InvalidPageCriteria("Page size should be greater than 0.")
        return ResponseEntity.ok().body(eventService.findAll(page, size))
    }

    @Operation(summary = "Submit a new event in application/json format. Need to specify the eventType and destination(s), the receivers of the event.")
    @PostMapping(path = [""], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [
                ExampleObject(name = "Event with single destination", description = "Event destinations should match any of the identities listed in the '/corda/peers' endpoint. Format for the destination is: <organisation>/<locality>/<country>, for example: TNO/Soesterberg/NL", value = """{ "event" : "text/turtle", "eventType" : "EventType", "eventDestinations" : ["TNO/Soesterberg/NL"] }"""),
                ExampleObject(name = "Event with multiple destinations", description = "Event destinations should match any of the identities listed in the '/corda/peers' endpoint. Format for the destination is: <organisation>/<locality>/<country>, for example: TNO/Soesterberg/NL", value = """{ "event" : "text/turtle", "eventType" : "EventType", "eventDestinations" : ["TNO/Soesterberg/NL", "TNO/Utrecht/NL", "TNO/Groningen/NL"] }""")
            ]
        )]
    )
    fun postEvent(@RequestBody event: EventWithDestinations): ResponseEntity<UUID> {
        log.info("Received EventWithDestinations: {}", event)
        return ResponseEntity.ok().body(eventService.publishRDFEvent(event))
    }

    @PostMapping(path = ["/query"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [
                ExampleObject(name = "Query additional information from the node related to specified event UUID.", description = "eventUUID should exit in the Corda Vault.", value = """{ "sparql" : "select * where { ?s ?p ?o . } limit 100", "eventUUID" : "asasd-asas234cda-sasw233ds" }""")
            ]
        )]
    )
    fun query(@RequestBody eventQuery: EventQuery): ResponseEntity<String> {
        log.info("Executing event query: {}", eventQuery)
        return ResponseEntity.ok(JsonUtils.toString(eventService.query(eventQuery)))
    }
}