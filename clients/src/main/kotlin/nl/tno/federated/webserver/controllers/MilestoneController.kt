package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import nl.tno.federated.flows.ArrivalFlow
import nl.tno.federated.states.MilestoneDTO
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import nl.tno.federated.webserver.APIResponse
import nl.tno.federated.webserver.dtos.MilestoneDTO
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

/**
 * Create and query milestones / events.
 */
@RestController
@RequestMapping("/milestones")
@Api(value = "MilestoneController", tags = ["Milestone / event details"])
class MilestoneController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new milestone")
    @PostMapping(value = ["/"])
    private fun newMilestone(@RequestBody milestone : MilestoneDTO) : APIResponse<String> {
        return if (milestone.type == MilestoneType.ARRIVE) {
            try {
                proxy.startFlowDynamic(
                    ArrivalFlow::class.java,
                    milestone.digitalTwins,
                    milestone.location
                ).returnValue.get()

                APIResponse.success("Milestone created")
            } catch (e: Exception) {
                return APIResponse.error("Something went wrong: $e")
            }
        } else APIResponse.error("Unimplemented milestone type")
    }

    @ApiOperation(value = "Return all known milestones")
    @GetMapping(value = ["/"])
    private fun milestones() : List<MilestoneDTO> {
        val milestoneStates = proxy.vaultQuery(MilestoneState::class.java).states.map { it.state.data }
        return milestoneStates.map { MilestoneDTO(it.type, it.digitalTwins, it.time, it.location) }
    }
}