package nl.tno.federated.semantic.adapter.tradelens

import io.swagger.annotations.Api
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/tradelens")
@Api(value = "TradelensController", tags = ["Tradelens Semantic Adapter endpoints"])
class TradelensController(private val tripleService: TradelensTripleService) {

    @PostMapping("/events", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createTriplesFromTradelensEvents(@RequestParam("base_uri", required = false, defaultValue = "") base_uri: String = "", @RequestBody body: String): ResponseEntity<String> {
        return ResponseEntity.ok(tripleService.createTriplesForEvents(jsonData = body, baseUri = base_uri))
    }

    @PostMapping("/containers", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createTriplesFromTradelensContainers(@RequestParam("base_uri", required = false, defaultValue = "") base_uri: String = "", @RequestBody body: String): ResponseEntity<String> {
        return ResponseEntity.ok(tripleService.createTriplesForContainers(jsonData = body, baseUri = base_uri))
    }
}