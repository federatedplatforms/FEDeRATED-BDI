package nl.tno.federated.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.GeneralSPARQLqueryFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.ParseRDFFlow
import nl.tno.federated.flows.QueryGraphDBbyIdFlow
import nl.tno.federated.services.TTLRandomGenerator
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.api.L1Services
import nl.tno.federated.api.NodeRPCConnection
import nl.tno.federated.api.SemanticAdapterService
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
    private val rpc: NodeRPCConnection,
    private val l1service: L1Services,
    private val semanticAdapterService: SemanticAdapterService
) {

    private val log = LoggerFactory.getLogger(EventController::class.java)

    private val eventGenerator = TTLRandomGenerator()

    @ApiOperation(value = "Generate a new random event")
    @PostMapping(value = ["/random/{destination}"])
    fun generateRandomEvent(@RequestHeader("Authorization") authorizationHeader: String, @RequestParam("start-flow") startFlow: String, @RequestParam("number-events") numberEvents: String): ResponseEntity<String> {
        // 1. check if number of events is a correct integer
        numberEvents.toIntOrNull() ?: return ResponseEntity("number-events was incorrectly specified", HttpStatus.BAD_REQUEST)

        // 2. interpret the startFlow as boolean
        return if (startFlow.ToBooleanOrNull() == null) {
            ResponseEntity("start-flow was incorrectly specified", HttpStatus.BAD_REQUEST)
        } else {
            // 3. generate the random event
            val generatedTTL = eventGenerator.generateRandomEvents(numberEvents.toInt())

            // 4. check if needed to start a new event flow
            if (startFlow.ToBooleanOrNull() == true) {
                newEvent(generatedTTL.constructedTTL, null, authorizationHeader)
            } else {
                ResponseEntity("Event created: ${generatedTTL.constructedTTL}", HttpStatus.CREATED)
            }
        }
        // escape call if everything goes wrong
        return ResponseEntity("Error caused by unknown reasons please report back the reqeust parameters: start flow = $startFlow, number of events = $numberEvents", HttpStatus.BAD_REQUEST)

    }

    @ApiOperation(value = "Create a new event and returns the UUID of the newly created event.")
    @PostMapping(value = ["/{destination}"])
    fun newEvent(@RequestBody event: String, @PathVariable destination: String?, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        l1service.verifyAccessToken(authorizationHeader)

        val recipients = if (destination == null) emptySet() else setOf(destination)

        log.info("Start NewEventFlow, sending event to destination: {}", destination)
        val newEventTx = rpc.client().startFlowDynamic(
            NewEventFlow::class.java,
            event,
            recipients
        ).returnValue.get()

        val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
        log.info("NewEventFlow ready, new event created with UUID: {}", createdEventId)
        return ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
    }

    @ApiOperation(value = "Create new event after passing it through the semantic adapter")
    @PostMapping(value = ["/newUnprocessed/{destination}"])
    fun newUnprocessedEvent(@RequestBody event: String, @PathVariable destination: String?): ResponseEntity<String> {
        // TODO add oauth2 support
        val convertedEvent = semanticAdapterService.processTradelensEvent(event)
        return newEvent(convertedEvent, destination, "Bearer doitanyway")
    }

    @ApiOperation(value = "Create new event after passing it through the semantic adapter")
    @PostMapping(value = ["/newUnprocessed"])
    fun newUnprocessedEvent(@RequestBody event: String): ResponseEntity<String> {
        // TODO add oauth2 support
        return newUnprocessedEvent(event, null)
    }


    @ApiOperation(value = "Return all known events")
    @GetMapping(value = [""])
    fun events(@RequestHeader("Authorization") authorizationHeader: String): Map<UUID, List<Event>> {
        l1service.verifyAccessToken(authorizationHeader)

        val eventStates = rpc.client().vaultQuery(EventState::class.java).states.map { it.state.data }
        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    fun eventById(@PathVariable id: String, @RequestHeader("Authorization") authorizationHeader: String): Map<UUID, List<Event>> {
        l1service.verifyAccessToken(authorizationHeader)

        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = rpc.client().vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return eventStatesToEventMap(state)
    }

    @ApiOperation(value = "Return RDF data by event ID from GraphDB instance")
    @GetMapping(value = ["/rdfevent/{id}"])
    fun gdbQueryEventById(@PathVariable id: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        l1service.verifyAccessToken(authorizationHeader)

        val gdbQuery = rpc.client().startFlowDynamic(
            QueryGraphDBbyIdFlow::class.java,
            id
        ).returnValue.get()
        return ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
    }

    @ApiOperation(value = "Return result of a custom SPARQL query")
    @GetMapping(value = ["/gdbsparql/"])
    fun gdbGeneralSparqlQuery(query: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        l1service.verifyAccessToken(authorizationHeader)

        val gdbQuery = rpc.client().startFlowDynamic(
            GeneralSPARQLqueryFlow::class.java,
            query
        ).returnValue.get()
        return ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
    }

    private fun eventStatesToEventMap(eventStates: List<EventState>): Map<UUID, List<Event>> {
        return eventStates.associate { val parseRDFToEvents = rpc.client().startFlowDynamic(ParseRDFFlow::class.java, it.fullEvent).returnValue.get()
            it.linearId.id to parseRDFToEvents
        }
    }

    private fun String.ToBooleanOrNull(): Boolean? {
        if ((this.toLowerCase() != "true") && (this.toLowerCase() != "false")) return null

        return this.toBoolean()
    }
}
