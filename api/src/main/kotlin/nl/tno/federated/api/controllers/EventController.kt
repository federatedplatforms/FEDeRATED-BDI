package nl.tno.federated.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.distribution.CordaEventDistributionService
import nl.tno.federated.api.semanticadapter.SemanticAdapterService
import nl.tno.federated.corda.services.TTLRandomGenerator
import nl.tno.federated.corda.services.graphdb.GraphDBEventConverter
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/events")
@Api(value = "EventController", tags = ["Event details"])
class EventController(
    private val semanticAdapterService: SemanticAdapterService,
    private val cordaNodeService: CordaNodeService,
    private val cordaEventDistributionService: CordaEventDistributionService
) {
    private val log = LoggerFactory.getLogger(EventController::class.java)
    private val eventGenerator = TTLRandomGenerator()

    @ApiOperation(value = "Generate a new random event with no destination")
    @PostMapping(value = ["/random"])
    fun generateRandomEventNoDestination(
        @RequestParam("start-flow") startFlow: String,
        @RequestParam("number-events") numberEvents: String,
        @RequestParam("country-code") countryCode: String
    ): ResponseEntity<String> {
        // 1. check if number of events is a correct integer
        numberEvents.toIntOrNull() ?: return ResponseEntity("number-events was incorrectly specified", HttpStatus.BAD_REQUEST)
        log.debug("Start flow: {}, numberEvents: {}, no destination info", startFlow, numberEvents)
        // 2. interpret the startFlow as boolean
        return if (startFlow.toBooleanOrNull() == null) {
            ResponseEntity("start-flow was incorrectly specified", HttpStatus.BAD_REQUEST)
        } else {
            // 3. generate the random event
            val generatedTTL = eventGenerator.generateRandomEvents(numberEvents.toInt(), countryCode)

            // 4. check if needed to start a new event flow
            if (startFlow.toBooleanOrNull() == true) {
                newEvent(generatedTTL.constructedTTL, null, null, null)
            } else {
                ResponseEntity(generatedTTL.constructedTTL, HttpStatus.CREATED)
            }
        }
    }

    @ApiOperation(value = "Generate a new random event")
    @PostMapping(
        value = [
            "/random/{destinationOrganisation}/{destinationLocality}/{destinationCountry}",
            "/random/{destinationOrganisation}/{destinationLocality}",
            "/random/{destinationOrganisation}"
        ]
    )
    fun generateRandomEvent(
        @RequestParam("start-flow") startFlow: String,
        @RequestParam("number-events") numberEvents: String,
        @PathVariable destinationOrganisation: String?,
        @PathVariable(required = false) destinationLocality: String?,
        @PathVariable(required = false) destinationCountry: String?
    ): ResponseEntity<String> {
        // 1. check if number of events is a correct integer
        numberEvents.toIntOrNull() ?: return ResponseEntity("number-events was incorrectly specified", HttpStatus.BAD_REQUEST)
        log.debug("Startflow: {}, numberEvents: {}, org: {}, local: {}, country: {}", startFlow, numberEvents, destinationOrganisation, destinationLocality, destinationCountry)
        // 2. interpret the startFlow as boolean
        return if (startFlow.toBooleanOrNull() == null) {
            ResponseEntity("start-flow was incorrectly specified", HttpStatus.BAD_REQUEST)
        } else {
            // 3. generate the random event
            val generatedTTL = eventGenerator.generateRandomEvents(numberEvents.toInt())

            // 4. check if needed to start a new event flow
            if (startFlow.toBooleanOrNull() == true) {
                newEvent(generatedTTL.constructedTTL, destinationOrganisation, destinationLocality, destinationCountry)
            } else {

                if (destinationOrganisation != null && (destinationLocality == null || destinationCountry == null)) {
                    return if (destinationLocality == null) {
                        ResponseEntity("Missing destination fields destinationLocality & destinationCountry", HttpStatus.BAD_REQUEST)
                    } else {
                        ResponseEntity("Missing destination field destinationCountry", HttpStatus.BAD_REQUEST)
                    }
                }
                ResponseEntity(generatedTTL.constructedTTL, HttpStatus.CREATED)
            }
        }
    }

    @ApiOperation(value = "Create a new event without destination and return the UUID of the newly created event.")
    @PostMapping(value = ["/"])
    fun newEventNoDestination(@RequestBody event: String): ResponseEntity<String> {
        log.info("Start NewEventFlow, no destination")
        return newEvent(event, null, null, null)
    }

    @ApiOperation(value = "Create a new event without destination and infer distribution from event content. Return the UUID of the newly created event.")
    @PostMapping(value = ["/autodistributed"])
    fun newEventDestinationImplied(@RequestBody event: String): ResponseEntity<String> {
        log.info("Extract destinations")
        val destinations = cordaEventDistributionService.extractDestinationsFromEvent(event) ?: return ResponseEntity("Could not find party", HttpStatus.BAD_REQUEST)

        log.info("Start NewEventFlow for each destination and return UUIDs")
        val createdEventId = cordaNodeService.startNewEventFlow(event, destinations.map { it.cordaX500Name }.toSet())

        log.info("NewEventFlow ready, new event created with UUID: {}", createdEventId)
        return ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
    }

    @ApiOperation(value = "Create a new event and returns the UUID of the newly created event.")
    @PostMapping(
        value = [
            "/{destinationOrganisation}/{destinationLocality}/{destinationCountry}",
            "/{destinationOrganisation}/{destinationLocality}",
            "/{destinationOrganisation}"
        ]
    )
    fun newEvent(
        @RequestBody event: String,
        @PathVariable destinationOrganisation: String?,
        @PathVariable destinationLocality: String?,
        @PathVariable destinationCountry: String?
    ): ResponseEntity<String> {
        log.info("Start NewEventFlow, sending event to destination: {}, {}, {}", destinationOrganisation, destinationLocality, destinationCountry)
        if (destinationOrganisation != null && (destinationLocality == null || destinationCountry == null)) {
            return if (destinationLocality == null) {
                ResponseEntity("Missing destination fields destinationLocality & destinationCountry", HttpStatus.BAD_REQUEST)
            } else {
                ResponseEntity("Missing destination field destinationCountry", HttpStatus.BAD_REQUEST)
            }
        }

        log.info("Start NewEventFlow, sending event to destination: {}, {}, {}", destinationOrganisation, destinationLocality, destinationCountry)
        val cordaName = if (destinationOrganisation == null) null else CordaX500Name(destinationOrganisation, destinationLocality!!, destinationCountry!!)
        val createdEventId = cordaNodeService.startNewEventFlow(event, cordaName)

        log.info("NewEventFlow ready, new event created with UUID: {}", createdEventId)
        return ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
    }

    @ApiOperation(value = "Create new event after passing it through the semantic adapter")
    @PostMapping(value = ["/unprocessed/{destinationName}/{destinationLocality}/{destinationCountry}"])
    fun newUnprocessedEvent(
        @RequestBody event: String,
        @PathVariable destinationName: String?,
        @PathVariable destinationLocality: String?,
        @PathVariable destinationCountry: String?
    ): ResponseEntity<String> {
        val convertedEvent = semanticAdapterService.processTradelensEvent(event)
        return newEvent(convertedEvent, destinationName, destinationLocality, destinationCountry)
    }

    @ApiOperation(value = "Create new event after passing it through the semantic adapter")
    @PostMapping(value = ["/unprocessed"])
    fun newUnprocessedEvent(@RequestBody event: String): ResponseEntity<String> {
        return newUnprocessedEvent(event, null, null, null)
    }

    @ApiOperation(value = "Return all known events")
    @GetMapping(value = [""])
    fun events(): Map<UUID, List<Event>> {
        val eventStates = cordaNodeService.startVaultQuery()
        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    fun eventById(@PathVariable id: String): Map<UUID, List<Event>> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = cordaNodeService.startVaultQueryBy(criteria)
        return eventStatesToEventMap(state)
    }

    @ApiOperation(value = "Return RDF data by event ID from GraphDB instance")
    @GetMapping(value = ["/rdfevent/{id}"])
    fun gdbQueryEventById(@PathVariable id: String): ResponseEntity<String> {
        val gdbQuery = cordaNodeService.startNewQueryGraphDBbyIdFlow(id)
        return ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
    }

    @ApiOperation(value = "Return result of a custom SPARQL query")
    @GetMapping(value = ["/gdbsparql/"])
    fun gdbGeneralSparqlQuery(query: String): ResponseEntity<String> {
        val gdbQuery = cordaNodeService.startNewGeneralSPARQLqueryFlow(query)
        return ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
    }

    private fun eventStatesToEventMap(eventStates: List<EventState>): Map<UUID, List<Event>> {
        return eventStates.associate {
            val parseRDFToEvents = GraphDBEventConverter.parseRDFToEvents(it.fullEvent)
            it.linearId.id to parseRDFToEvents
        }
    }
}

private fun String.toBooleanOrNull(): Boolean? {
    if ((this.toLowerCase() != "true") && (this.toLowerCase() != "false")) return null
    return this.toBoolean()
}