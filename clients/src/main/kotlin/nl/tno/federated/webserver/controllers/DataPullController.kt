package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import nl.tno.federated.flows.DataPullQueryFlow
import nl.tno.federated.states.DataPullState
import nl.tno.federated.webserver.L1Services.extractAccessTokenFromHeader
import nl.tno.federated.webserver.L1Services.userIsAuthorized
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.naming.AuthenticationException


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/datapull")
@Api(value = "DataPullController", tags = ["Data pull endpoints"])
class DataPullController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Request data and run a SPARQL query on another node")
    @PostMapping(value = ["/request/{destination}"])
    private fun request(@RequestBody query : String, @PathVariable destination: String?, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        return try {
            val dataPull = proxy.startFlowDynamic(
                    DataPullQueryFlow::class.java,
                    destination,
                    query
            ).returnValue.get()
            val uuidOfStateWithResult = (dataPull.coreTransaction.getOutput(0) as DataPullState).linearId.id
            ResponseEntity("State with result: $uuidOfStateWithResult", HttpStatus.ACCEPTED)
        } catch (e: Exception) {
            return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }


    @ApiOperation(value = "Retrieve data from a previous request")
    @GetMapping(value = ["/retrieve/{uuid}"])
    private fun retrieve(@PathVariable uuidOfStateWithResult: UUID, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<List<String>> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        val datapullResults = proxy.vaultQueryBy<DataPullState>().states.filter {
            it.state.data.linearId.id == uuidOfStateWithResult
        }.flatMap{ it.state.data.result }

        return ResponseEntity(datapullResults, HttpStatus.OK)
    }


}
