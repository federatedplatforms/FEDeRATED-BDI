package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.*
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.L1Services.extractAccessTokenFromHeader
import nl.tno.federated.webserver.L1Services.userIsAuthorized
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.NewEvent
import nl.tno.federated.webserver.dtos.QueryAndOrganization
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
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
    @GetMapping(value = ["/pull/"])
    private fun datapull(@RequestBody queryInfo : QueryAndOrganization, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        return try {
            val dataPull = proxy.startFlowDynamic(
                    DataPullQueryFlow::class.java,
                    queryInfo.organization,
                    queryInfo.query
            ).returnValue.get()
            ResponseEntity("Query result: $dataPull", HttpStatus.ACCEPTED)
        } catch (e: Exception) {
            return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
