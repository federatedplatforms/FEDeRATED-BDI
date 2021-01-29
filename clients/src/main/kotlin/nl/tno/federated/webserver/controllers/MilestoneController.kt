package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import nl.tno.federated.flows.ArrivalFlow
import nl.tno.federated.states.MilestoneDTO
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Create and query events.
 */
@RestController
@RequestMapping("/events")
@Api(value = "MilestoneController", tags = ["Event details"])
class MilestoneController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new event")
    @PostMapping(value = ["/"])
    private fun newMilestone(@RequestBody event : MilestoneDTO) : ResponseEntity<String> {
        return if (event.type == MilestoneType.ARRIVE) {
            try {
                proxy.startFlowDynamic(
                    ArrivalFlow::class.java,
                    event.digitalTwins,
                    event.location
                ).returnValue.get()

                ResponseEntity("Event created", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } else ResponseEntity("Unimplemented milestone type", HttpStatus.BAD_REQUEST)
    }

    @ApiOperation(value = "Return all known events")
    @GetMapping(value = ["/"])
    private fun events() : List<MilestoneDTO> {
        val eventStates = proxy.vaultQuery(MilestoneState::class.java).states.map { it.state.data }
        return eventStates.map { MilestoneDTO(it.type, it.digitalTwins, it.time, it.location) }
    }
}