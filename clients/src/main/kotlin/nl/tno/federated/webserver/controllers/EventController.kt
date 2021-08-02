package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.DigitalTwinPair
import nl.tno.federated.flows.ExecuteEventFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.UpdateEstimatedTimeFlow
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
    private fun newEvent(@RequestBody digitalTwins: List<DigitalTwinPair>, time: Date, eCMRuri: String, milestone: Milestone): ResponseEntity<String> {
        return try {
                val newEventTx = proxy.startFlowDynamic(
                        NewEventFlow::class.java,
                        digitalTwins,
                        time,
                        eCMRuri,
                        milestone
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Update an event estimated time")
    @PostMapping(value = ["/updatetime"])
    private fun updateEvent(@RequestBody eventUUID: UUID, time: Date): ResponseEntity<String> {
        return try {
                val updateEventTx = proxy.startFlowDynamic(
                        UpdateEstimatedTimeFlow::class.java,
                        eventUUID,
                        time
                ).returnValue.get()
                val createdEventId = (updateEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Execute an event")
    @PostMapping(value = ["/executeevent"])
    private fun executeEvent(@RequestBody eventUUID: UUID, time: Date): ResponseEntity<String> {
        return try {
                val executeEventTx = proxy.startFlowDynamic(
                        ExecuteEventFlow::class.java,
                        eventUUID,
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

        return eventStates.map { it.linearId.id to Event(it.goods, it.transportMean, it.location, it.otherDigitalTwins, it.eventCreationtime, it.timestamps, it.ecmruri, it.milestone) }.toMap()
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    private fun event(@PathVariable id: UUID): Map<UUID, Event> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        val state = proxy.vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return state.map { it.linearId.id to Event(it.goods, it.transportMean, it.location, it.otherDigitalTwins, it.eventCreationtime, it.timestamps, it.ecmruri, it.milestone) }.toMap()
    }

    @ApiOperation(value = "Return events by digital twin ID")
    @GetMapping(value = ["/digitaltwin/{dtuuid}"])
    private fun eventBydtUUID(@PathVariable dtuuid: UUID): Map<UUID, Event> {
        val eventStates = proxy.vaultQueryBy<EventState>().states.filter {
                    it.state.data.goods.contains(dtuuid) ||
                    it.state.data.transportMean.contains(dtuuid) ||
                    it.state.data.location.contains(dtuuid) ||
                    it.state.data.otherDigitalTwins.contains(dtuuid)
        }.map{ it.state.data }

        return eventStates.map { it.linearId.id to Event(it.goods, it.transportMean, it.location, it.otherDigitalTwins, it.eventCreationtime, it.timestamps, it.ecmruri, it.milestone) }.toMap()
    }
}
