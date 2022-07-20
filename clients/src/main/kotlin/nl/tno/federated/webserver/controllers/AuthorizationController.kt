package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.*
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.L1Services
import nl.tno.federated.webserver.L1Services.extractAuthorizationResult
import nl.tno.federated.webserver.L1Services.isJWTFormatValid
import nl.tno.federated.webserver.L1Services.isLettersOrDigits
import nl.tno.federated.webserver.L1Services.retrieveUrlBody
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.NewEvent
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
@RequestMapping("/accesstoken")
@Api(value = "AuthorizationController", tags = ["Access token validation"])
class AuthorizationController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Validate an access token")
    @PostMapping(value = ["/tokenisvalid"])
    private fun validateToken(@RequestBody token: String) : Boolean {
        return L1Services.validateToken(token)
    }

}
