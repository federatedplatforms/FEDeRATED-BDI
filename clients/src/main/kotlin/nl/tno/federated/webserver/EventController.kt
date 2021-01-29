package nl.tno.federated.webserver

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import nl.tno.federated.flows.LoadFlow
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

/**
 * Create and query milestones / events.
 */
@RestController
@RequestMapping("/events")
@Api(value = "EventController", tags = ["Milestone / event details"])
class EventController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new milestone")
    @PostMapping(value = ["/"])
    private fun newEvent(@RequestBody event : Event) : APIResponse<String> {
        return if (event.type == EventType.LOAD) {
            try {
                proxy.startFlowDynamic(
                    LoadFlow::class.java,
                    event.digitalTwins,
                    event.location
                ).returnValue.get()

                APIResponse.success("Event created")
            } catch (e: Exception) {
                return APIResponse.error("Something went wrong: $e")
            }
        } else APIResponse.error("Unimplemented event type")
    }

    @ApiOperation(value = "Return all known events")
    @GetMapping(value = ["/"])
    private fun events() : List<Event> {
        val eventStates = proxy.vaultQuery(EventState::class.java).states.map { it.state.data }
        return eventStates.map { Event(it.type, it.digitalTwins, it.time, it.location) }
    }
}