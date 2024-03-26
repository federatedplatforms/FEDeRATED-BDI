package nl.tno.federated.api.controllers

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoint that allows for receiving a request (GET/POST) and prints the request body and http headers.
 *
 * Used for testing the Webhook
 */
@RestController
class EchoController {

    @RequestMapping("/api/echo", method = [RequestMethod.GET, RequestMethod.POST])
    fun echo(@RequestBody body: String, request: HttpServletRequest) {
        val headerNames = request.headerNames
        val map = headerNames.asSequence().associateWith { request.getHeader(it) }
        log.info("Echoing incoming request, body: {}, headers: {}", body, map)
    }

    companion object {
        private val log = LoggerFactory.getLogger(EchoController::class.java)
    }
}