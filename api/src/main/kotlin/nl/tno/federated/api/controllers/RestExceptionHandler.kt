package nl.tno.federated.api.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter
import javax.naming.AuthenticationException

/**
 * Partial implementation of https://www.rfc-editor.org/rfc/rfc7807
 */
data class Problem(val type: String?, val title: String?, val detail: String? = null)

@ControllerAdvice(basePackages = ["nl.tno.federated"])
class RestExceptionHandler {

    private val log = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(Throwable::class)
    fun handleUncaught(t: Throwable): ResponseEntity<Problem> {
        log.info("Uncaught exception while executing request: {}", t.message, t)
        val stackTrace = StringWriter().apply { PrintWriter(this).apply { t.printStackTrace(this) } }
        return ResponseEntity(Problem(type = t.javaClass.name, title = t.message, detail = stackTrace.toString()), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun authenticationException(ae: AuthenticationException): ResponseEntity<Problem> {
        log.info("Request not authorized. Message: {}", ae.message)
        return ResponseEntity(Problem(type = ae.javaClass.name, title = ae.message), HttpStatus.FORBIDDEN)
    }
}