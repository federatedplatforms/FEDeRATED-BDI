package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.DataPullQueryFlow
import nl.tno.federated.states.DataPullState
import nl.tno.federated.webserver.L1Services
import nl.tno.federated.webserver.NodeRPCConnection
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/datapull")
@Api(value = "DataPullController", tags = ["Data pull endpoints"])
class DataPullController(private val rpc: NodeRPCConnection, private val l1service: L1Services) {

    @ApiOperation(value = "Request data and run a SPARQL query on another node")
    @PostMapping(value = ["/request/{destination}"])
    private fun request(@RequestBody query: String, @PathVariable destination: String?, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        l1service.verifyAccessToken(authorizationHeader)

        val dataPull = rpc.client().startFlowDynamic(
            DataPullQueryFlow::class.java,
            destination,
            query
        ).returnValue.get()
        val uuidOfStateWithResult = (dataPull.coreTransaction.getOutput(0) as DataPullState).linearId.id
        return ResponseEntity("State with result: $uuidOfStateWithResult", HttpStatus.ACCEPTED)
    }

    @ApiOperation(value = "Retrieve data from a previous request")
    @GetMapping(value = ["/retrieve/{uuid}"], produces =  [MediaType.APPLICATION_JSON_VALUE])
    fun retrieve(@PathVariable uuid: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<List<String>> {
        l1service.verifyAccessToken(authorizationHeader)

        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(UUID.fromString(uuid)))
        val datapullResults = rpc.client().vaultQueryBy<DataPullState>(criteria).states
                .flatMap{ it.state.data.result }

        return ResponseEntity(datapullResults, HttpStatus.OK)
    }
}