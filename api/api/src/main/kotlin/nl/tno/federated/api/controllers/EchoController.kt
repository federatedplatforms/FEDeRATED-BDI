package nl.tno.federated.api.controllers

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class EchoController {
    private val log = LoggerFactory.getLogger(EchoController::class.java)


    @RequestMapping("/api/echo")
    fun echo(@RequestBody body: String, request: HttpServletRequest) {
        val headerNames = request.headerNames
        val map = headerNames.asSequence().associateWith { request.getHeader(it) }
        log.info("Incoming request, body: {}, headers: {}", body, map)
    }
}