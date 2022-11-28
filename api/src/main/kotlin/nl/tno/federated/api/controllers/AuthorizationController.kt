package nl.tno.federated.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import nl.tno.federated.api.L1Services
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/accesstoken")
@Api(value = "AuthorizationController", tags = ["Access token validation"])
class AuthorizationController(private val l1services: L1Services) {

    @ApiOperation(value = "Validate an access token")
    @PostMapping(value = ["/tokenisvalid"])
    fun validateToken(@RequestBody token: String): Boolean {
        return l1services.validateToken(token)
    }

}
