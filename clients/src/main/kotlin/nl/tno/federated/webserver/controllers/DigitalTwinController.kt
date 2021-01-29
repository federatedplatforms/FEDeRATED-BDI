package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.CreateFlow
import nl.tno.federated.states.Cargo
import nl.tno.federated.states.CargoState
import nl.tno.federated.states.DigitalTwin
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    private fun newTwin(@RequestBody digitalTwin : DigitalTwin) : ResponseEntity<String> {
        return try {
            proxy.startFlowDynamic(
                CreateFlow::class.java,
                digitalTwin
            ).returnValue.get()
            ResponseEntity("Digital twin created", HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Return all cargo")
    @GetMapping(value = ["/cargo"])
    private fun cargo(): List<Cargo> {
        return proxy.vaultQuery(CargoState::class.java).states.map { it.state.data }
    }

    @ApiOperation(value = "Return a piece of cargo")
    @GetMapping(value = ["/{id}"])
    private fun cargo(@PathVariable id: UUID): List<Cargo> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        return proxy.vaultQueryBy<CargoState>(criteria).states.map { it.state.data }
    }
}

