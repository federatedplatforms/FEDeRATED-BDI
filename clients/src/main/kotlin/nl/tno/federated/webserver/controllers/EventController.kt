package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.ExecuteEventFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.UpdateEstimatedTimeFlow
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.NewEvent
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
    private fun newEvent(@RequestBody event: NewEvent): ResponseEntity<String> {
        if (event.uniqueId && event.id.isNotBlank()) {
            if (eventById(event.id).isNotEmpty()) {
                return ResponseEntity("Event with this id already exists. If you want to insert anyway, unset the uniqueId parameter.", HttpStatus.BAD_REQUEST)
            }
        }

        return try {
                val newEventTx = proxy.startFlowDynamic(
                        NewEventFlow::class.java,
                        event.digitalTwins,
                        event.time,
                        event.eCMRuri,
                        event.milestone,
                        UniqueIdentifier(event.id, UUID.randomUUID())
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Update an event estimated time")
    @PutMapping(value = ["/updatetime"])
    private fun updateEvent(@RequestBody eventId: String, time: Date): ResponseEntity<String> {
        return try {
                val updateEventTx = proxy.startFlowDynamic(
                        UpdateEstimatedTimeFlow::class.java,
                        eventId,
                        time
                ).returnValue.get()
                val createdEventId = (updateEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Execute an event")
    @PutMapping(value = ["/execute"])
    private fun executeEvent(@RequestBody eventId: String, time: Date): ResponseEntity<String> {
        return try {
                val executeEventTx = proxy.startFlowDynamic(
                        ExecuteEventFlow::class.java,
                        eventId,
                        time
                ).returnValue.get()
                val createdEventId = (executeEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }


    @ApiOperation(value = "Return all known events")
    @GetMapping(value = [""])
    private fun events() : Map<UUID, Event> {
        val eventStates = proxy.vaultQuery(EventState::class.java).states.map { it.state.data }

        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    private fun eventById(@PathVariable id: String): Map<UUID, Event> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = proxy.vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return eventStatesToEventMap(state)
    }

    @ApiOperation(value = "Return events by digital twin UUID")
    @GetMapping(value = ["/digitaltwin/{dtuuid}"])
    private fun eventBydtUUID(@PathVariable dtuuid: UUID): Map<UUID, Event> {
        val eventStates = proxy.vaultQueryBy<EventState>().states.filter {
                    it.state.data.goods.contains(dtuuid) ||
                    it.state.data.transportMean.contains(dtuuid) ||
                    it.state.data.location.contains(dtuuid) ||
                    it.state.data.otherDigitalTwins.contains(dtuuid)
        }.map{ it.state.data }

        return eventStatesToEventMap(eventStates)
    }

    private fun eventStatesToEventMap(eventStates: List<EventState>) =
        eventStates.associate {
            it.linearId.id to Event(
                it.goods,
                it.transportMean,
                it.location,
                it.otherDigitalTwins,
                it.eventCreationtime,
                it.timestamps,
                it.startTimestamps,
                it.ecmruri,
                it.milestone,
                it.linearId.externalId ?: it.linearId.id.toString()
            )
        }
}
