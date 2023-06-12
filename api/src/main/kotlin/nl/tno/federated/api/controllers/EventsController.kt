package nl.tno.federated.api.controllers

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
        val uuid = eventService.handleNewEvent(arrivalEvent)
        log.info("New event flow started for arrivalEvent with UUID: {}", uuid)
        return ResponseEntity
            .created(URI("/events/ArrivalEvent/${uuid}"))
            .body(arrivalEvent)
    }

    override fun arrivalEventResourceIdGet(resourceId: String): ResponseEntity<String> {
        return ResponseEntity.ok(eventService.findEventById(resourceId))
    }

    override fun loadEventPost(loadEvent: LoadEvent): ResponseEntity<LoadEvent> {
        val uuid = eventService.handleNewEvent(loadEvent)
        log.info("New event flow started for loadEvent with UUID: {}", uuid)
        return ResponseEntity
            .created(URI("/events/LoadEvent/${uuid}"))
            .body(loadEvent)
    }

    override fun loadEventResourceIdGet(resourceId: String): ResponseEntity<String> {
        return ResponseEntity.ok(eventService.findEventById(resourceId))
    }

    @GetMapping(path = [""], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEvents(): ResponseEntity<List<String>> {
        log.info("get all events")
        return ResponseEntity.ok().body(eventService.findAll())
    }

    @PostMapping(path = [""], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [
                ExampleObject(name = "Event with single destination", description = "Event destinations should match any of the identities listed in the '/corda/peers' endpoint.", value = """{ "event" : "RDF data", "eventType" : "EventType", "eventDestinations" : ["TNO/Soesterberg/NL"] }"""),
                ExampleObject(name = "Event with multiple destinations", description = "Event destinations should match any of the identities listed in the '/corda/peers' endpoint.", value = """{ "event" : "RDF data", "eventType" : "EventType", "eventDestinations" : ["TNO/Soesterberg/NL", "TNO/Utrecht/NL", "TNO/Groningen/NL"] }""")
            ]
        )]
    )
    fun postEvent(@RequestBody event: EventWithDestinations): ResponseEntity<UUID> {
        log.info("Received EventWithDestinations: {}", event)
        return ResponseEntity.ok().body(eventService.handleNewEvent(event))
    }
}