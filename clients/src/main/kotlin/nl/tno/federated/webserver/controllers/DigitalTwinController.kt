package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.ArrivalFlow
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import nl.tno.federated.webserver.APIResponse
import nl.tno.federated.webserver.dtos.MilestoneDTO
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.DigitalTwinDTO
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Create and query digital twins, such as containers, trucks, planes, hubs, et cetera.
 */
@RestController
@RequestMapping("/twins")
@Api(value = "DigitalTwinController", tags = ["Digital twin details"])
class DigitalTwinController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new digital twin")
    @PostMapping(value = ["/"])
    private fun newMilestone(@RequestBody digitalTwin : DigitalTwinDTO) : APIResponse<String> {
            try {
                proxy.startFlowDynamic(
                    NewContainer::class.java,
                    "args go here"
                ).returnValue.get()

                APIResponse.success("Digital twin (container) created")
            } catch (e: Exception) {
                return APIResponse.error("Something went wrong: $e")
            }
    }

    @ApiOperation(value = "Return all cargo")
    @GetMapping(value = ["/cargo"])
    private fun cargo() : List<DigitalTwinDTO> {
        val milestoneStates = proxy.vaultQuery(CargoState::class.java).states.map { it.state.data }
        return milestoneStates.map { DigitalTwinDTO(it) }
    }

    @ApiOperation(value = "Return a digital twin")
    @GetMapping(value = ["/{id}"])
    private fun twin(@PathVariable id:UUID) : List<DigitalTwinDTO> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid= listOf(id))
        val milestoneStates = proxy.vaultQueryBy<CargoState::class>(criteria).states.map { it.state.data }
        return milestoneStates.map { DigitalTwinDTO(it) }
    }
}