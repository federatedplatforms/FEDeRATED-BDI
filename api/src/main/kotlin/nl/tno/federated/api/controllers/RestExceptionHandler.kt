package nl.tno.federated.api.controllers

import nl.tno.federated.api.event.InvalidEventDataException
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
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
        return ResponseEntity(Problem(type = t.javaClass.name, title = t.message, detail = "See error logs for more details."), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun authenticationException(e: AuthenticationException): ResponseEntity<Problem> {
        log.info("Request not authorized. Message: {}", e.message)
        return ResponseEntity(Problem(type = e.javaClass.name, title = e.message), HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(InvalidEventDataException::class)
    fun invalidEventDataException(e: InvalidEventDataException): ResponseEntity<Problem> {
        log.debug("Invalid Event data provided. Message: {}", e.message)
        return ResponseEntity(Problem(type = e.javaClass.name, title = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UnsupportedEventTypeException::class)
    fun unsupportedEventTypeException(e: UnsupportedEventTypeException): ResponseEntity<Problem> {
        log.debug("Unsupported Event type provided. Message: {}", e.message)
        return ResponseEntity(Problem(type = e.javaClass.name, title = e.message), HttpStatus.BAD_REQUEST)
    }
}