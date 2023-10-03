package nl.tno.federated.api.controllers

import net.corda.core.CordaRuntimeException
import net.corda.core.CordaThrowable
import net.corda.core.flows.UnexpectedFlowEndException
import nl.tno.federated.api.event.InvalidEventDataException
import nl.tno.federated.api.event.mapper.UnsupportedEventTypeException
import nl.tno.federated.api.event.validation.ShaclValidationException
import nl.tno.federated.api.util.InvalidRDFException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.util.concurrent.ExecutionException

/**
 * Partial implementation of https://www.rfc-editor.org/rfc/rfc7807
 *
 * In Spring Boot 3 this comes built-in
 */
data class ProblemDetail(val type: String?, val title: String?, val detail: String? = null)

@ControllerAdvice(basePackages = ["nl.tno.federated"])
class RestExceptionHandler {

    private val log = LoggerFactory.getLogger(RestExceptionHandler::class.java)

    @ExceptionHandler(Throwable::class)
    fun handleUncaught(t: Throwable): ResponseEntity<ProblemDetail> {
        log.info("Uncaught exception while executing request: {}", t.message, t)
        return ResponseEntity(ProblemDetail(type = t.javaClass.name, title = t.message, detail = "See error logs for more details."), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(InvalidEventDataException::class)
    fun invalidEventDataException(e: InvalidEventDataException): ResponseEntity<ProblemDetail> {
        log.debug("Invalid Event data provided. Message: {}", e.message)
        return ResponseEntity(ProblemDetail(type = e.javaClass.name, title = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ShaclValidationException::class)
    fun shaclValidationException(e: ShaclValidationException): ResponseEntity<ProblemDetail> {
        log.debug("SHACL validation failed. Message: {}", e.message)
        return ResponseEntity(ProblemDetail(type = e.javaClass.name, title = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidRDFException::class)
    fun invalidRDFException(e: InvalidRDFException): ResponseEntity<ProblemDetail> {
        log.debug("Invalid Event data provided. Message: {}", e.message)
        return ResponseEntity(ProblemDetail(type = e.javaClass.name, title = "Invalid RDF event data supplied, expected text/turtle.", detail = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(UnsupportedEventTypeException::class)
    fun unsupportedEventTypeException(e: UnsupportedEventTypeException): ResponseEntity<ProblemDetail> {
        log.debug("Unsupported Event type provided. Message: {}", e.message)
        return ResponseEntity(ProblemDetail(type = e.javaClass.name, title = "EventType that was supplied is not supported!", detail = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(InvalidPageCriteria::class)
    fun invalidPageCriteriaException(e: InvalidPageCriteria): ResponseEntity<ProblemDetail> {
        log.debug("Unsupported Event type provided. Message: {}", e.message)
        return ResponseEntity(ProblemDetail(type = e.javaClass.name, title = "Page size should be greater than zero.", detail = e.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ExecutionException::class)
    fun executionException(ee: ExecutionException): ResponseEntity<ProblemDetail> {
        return when (val e = ee.cause) {
            is UnexpectedFlowEndException -> {
                log.warn("UnexpectedFlowEndException occurred Event type provided. Message: {}", e.message)
                ResponseEntity(ProblemDetail(type = e.javaClass.name, title = "Flow ended unexpectedly, please check the logs. Message: "+ e.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
            is CordaRuntimeException -> {
                log.warn("CordaRuntimeException occurred. Message: {}", e.message)
                ResponseEntity(ProblemDetail(type = e.javaClass.name, title = "Runtime exception executing the flow, please check the logs."+ e.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
            is CordaThrowable -> {
                log.warn("CordaThrowable occurred. Message: {}", e.message)
                ResponseEntity(ProblemDetail(type = e.javaClass.name, title = e.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
            else -> {
                log.warn("ExecutionException occurred. Message: {}", ee.message)
                ResponseEntity(ProblemDetail(type = ee.javaClass.name, title = ee.message), HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}