package nl.tno.semantic.adapter

import nl.tno.semantic.adapter.DataType.*
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class SemanticAdapterController(private val trippleService: TrippleService) {

    @PostMapping("/", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createTriplesFromPreMappings(@RequestParam("base_uri", required = false, defaultValue = "") base_uri: String = "", @RequestBody body: String): ResponseEntity<String> {
        return ResponseEntity.ok(trippleService.createTripples(jsonData = body, type = DEFAULT, baseUri = base_uri))
    }

    @PostMapping("/tradelens-events", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createTriplesFromTradelensEvents(@RequestParam("base_uri", required = false, defaultValue = "") base_uri: String = "", @RequestBody body: String): ResponseEntity<String> {
        return ResponseEntity.ok(trippleService.createTripples(jsonData = body, type = TRADELENS_EVENTS, baseUri = base_uri))
    }

    @PostMapping("/tradelens-containers", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createTriplesFromTradelensContainers(@RequestParam("base_uri", required = false, defaultValue = "") base_uri: String = "", @RequestBody body: String): ResponseEntity<String> {
        return ResponseEntity.ok(trippleService.createTripples(jsonData = body, type = TRADELENS_CONTAINERS, baseUri = base_uri))
    }
}