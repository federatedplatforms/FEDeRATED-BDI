package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.GeneralSPARQLqueryFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.QueryGraphDBbyIdFlow
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.L1Services
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.SemanticAdapterService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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

    @ApiOperation(value = "Create a new event")
    @PostMapping(value = ["/"])
    fun newEvent(@RequestBody event: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        return newEvent(event, null, authorizationHeader)
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
    fun events(@RequestHeader("Authorization") authorizationHeader: String): Map<UUID, Event> {
        l1service.verifyAccessToken(authorizationHeader)

        val eventStates = rpc.client().vaultQuery(EventState::class.java).states.map { it.state.data }
        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    fun eventById(@PathVariable id: String, @RequestHeader("Authorization") authorizationHeader: String): Map<UUID, Event> {
        l1service.verifyAccessToken(authorizationHeader)

        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = rpc.client().vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return eventStatesToEventMap(state)
    }

    @ApiOperation(value = "Return events by digital twin UUID")
    @GetMapping(value = ["/digitaltwin/{dtuuid}"])
    fun eventBydtUUID(@PathVariable dtuuid: UUID, @RequestHeader("Authorization") authorizationHeader: String): Map<UUID, Event> {
        l1service.verifyAccessToken(authorizationHeader)

        val eventStates = rpc.client().vaultQueryBy<EventState>().states.filter {
            it.state.data.goods.contains(dtuuid) ||
                it.state.data.transportMean.contains(dtuuid) ||
                it.state.data.otherDigitalTwins.contains(dtuuid)
        }.map { it.state.data }

        return eventStatesToEventMap(eventStates)
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

    private fun eventStatesToEventMap(eventStates: List<EventState>) =
        eventStates.associate {
            it.linearId.id to Event(
                it.goods,
                it.transportMean,
                it.location,
                it.otherDigitalTwins,
                it.timestamps,
                it.ecmruri,
                it.milestone,
                "",
                it.fullEvent
            )
        }
}
