package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.ExecuteEventFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.states.*
import nl.tno.federated.webserver.NodeRPCConnection
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
class EventController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new event")
    @PostMapping(value = ["/"])
    private fun newEvent(@RequestBody event : Event) : ResponseEntity<String> {
        return if (!listOf(EventType.LOAD, EventType.ARRIVE, EventType.DEPART, EventType.DISCHARGE).contains(event.type))
            ResponseEntity("Unimplemented event type", HttpStatus.BAD_REQUEST)
        else {
            try {
                val newEventTx = proxy.startFlowDynamic(
                    NewEventFlow::class.java,
                    event.type,
                    event.digitalTwins,
                    event.location,
                    event.eCMRuri,
                    event.milestone
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }

    @ApiOperation(value = "Create multiple new events")
    @PostMapping(value = ["/itinerary"])
    private fun newEvents(@RequestBody events: List<Event>) : ResponseEntity<List<String>> {
        val results = events.map { newEvent(it).body!! }
        return ResponseEntity(results, HttpStatus.CREATED)
    }

    @ApiOperation(value = "Execute a planned event")
    @PatchMapping(value = ["/{plannedEventID}/execute"])
    private fun executeEvent(@PathVariable plannedEventID: UUID) : ResponseEntity<String> {
        return try {
            val newEventTx = proxy.startFlowDynamic(
                ExecuteEventFlow::class.java,
                plannedEventID
            ).returnValue.get()
            val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
            ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Return all known events")
    @GetMapping(value = [""])
    private fun events() : Map<UUID, Event> {
        val eventStates = proxy.vaultQuery(EventState::class.java).states.map { it.state.data }

        return eventStates.map { it.linearId.id to Event(it.type, it.digitalTwins, it.time, it.location, it.eCMRuri, it.milestone) }.toMap()
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    private fun event(@PathVariable id: UUID): Map<UUID, Event> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        val state = proxy.vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return state.map { it.linearId.id to Event(it.type, it.digitalTwins, it.time, it.location, it.eCMRuri, it.milestone) }.toMap()
    }

    @ApiOperation(value = "Return events by license plate")
    @GetMapping(value = ["/license/{plate}"])
    private fun eventByTruck(@PathVariable plate: String): Map<UUID, Event> {
        val hasPlate = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(plate))
        val digitalTwinIds = proxy.vaultQueryBy<DigitalTwinState>(hasPlate).states.map { it.state.data.linearId.id }

        val eventStates = proxy.vaultQuery(EventState::class.java).states.map { it.state.data }
        val relevantEventStates = eventStates.filter { it.digitalTwins.map { uniqueIdentifier -> uniqueIdentifier.id }.intersect(digitalTwinIds).isNotEmpty() }
        return relevantEventStates.map { it.linearId.id to Event(it.type, it.digitalTwins, it.time, it.location, it.eCMRuri, it.milestone) }.toMap()
    }

    @ApiOperation(value = "Return cargo by license plate")
    @GetMapping(value = ["/license/{plate}/cargo"])
    private fun cargoByTruck(@PathVariable plate: String): Map<UUID, Cargo> {
        val relevantEvents = eventByTruck(plate).values.filter { it.type == EventType.LOAD }
        val relevantDtStateIds = relevantEvents.flatMap { it.digitalTwins }.map { it.id }

        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = relevantDtStateIds)
        val states = proxy.vaultQueryBy<DigitalTwinState>(criteria).states.map { it.state.data }
        return states.filter { it.cargo != null }.map { it.linearId.id to it.cargo!! }.toMap()    }
}