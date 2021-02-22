package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.Builder.notNull
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.CreateCargoFlow
import nl.tno.federated.flows.CreateTruckFlow
import nl.tno.federated.states.Cargo
import nl.tno.federated.states.DigitalTwinSchemaV1
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.Truck
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Create and query digital twins, such as containers, trucks, planes, et cetera.
 */
@RestController
@RequestMapping("/twins")
@Api(value = "DigitalTwinController", tags = ["Digital twin details"])
class DigitalTwinController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new digital truck")
    @PostMapping(value = ["/trucks"])
    private fun newTruck(@RequestBody truck : Truck) : ResponseEntity<String> {
        return try {
            proxy.startFlowDynamic(
                CreateTruckFlow::class.java,
                truck
            ).returnValue.get()
            ResponseEntity("Digital twin created", HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Create new cargo")
    @PostMapping(value = ["/cargo"])
    private fun newCargo(@RequestBody cargo : Cargo) : ResponseEntity<String> {
        return try {
            proxy.startFlowDynamic(
                CreateCargoFlow::class.java,
                cargo
            ).returnValue.get()
            ResponseEntity("Digital twin created", HttpStatus.CREATED)
        } catch (e: Exception) {
            ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Return all cargo")
    @GetMapping(value = ["/cargo"])
    private fun cargo(): List<Cargo> {
        val hasCargo = QueryCriteria.VaultCustomQueryCriteria(DigitalTwinSchemaV1.PersistentDigitalTwin::cargo.notNull())
        return proxy.vaultQueryByCriteria(hasCargo, DigitalTwinState::class.java).states.map { it.state.data.cargo!! }
    }

    @ApiOperation(value = "Return all trucks")
    @GetMapping(value = ["/trucks"])
    private fun trucks(): List<Truck> {
        val hasCargo = QueryCriteria.VaultCustomQueryCriteria(DigitalTwinSchemaV1.PersistentDigitalTwin::truck.notNull())
        return proxy.vaultQueryByCriteria(hasCargo, DigitalTwinState::class.java).states.map { it.state.data.truck!! }
    }

    @ApiOperation(value = "Return a piece of cargo")
    @GetMapping(value = ["/cargo/{id}"])
    private fun cargo(@PathVariable id: UUID): List<Cargo> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        val states = proxy.vaultQueryBy<DigitalTwinState>(criteria).states.map { it.state.data }
        return states.filter { it.cargo != null }.map { it.cargo!! }
    }

    @ApiOperation(value = "Return a truck")
    @GetMapping(value = ["/trucks/{id}"])
    private fun truck(@PathVariable id: UUID): List<Truck> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        val states = proxy.vaultQueryBy<DigitalTwinState>(criteria).states.map { it.state.data }
        return states.filter { it.truck != null }.map { it.truck!! }
    }
}

