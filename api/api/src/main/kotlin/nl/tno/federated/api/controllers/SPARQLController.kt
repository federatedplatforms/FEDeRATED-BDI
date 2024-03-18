package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import nl.tno.federated.api.graphdb.GraphDBSPARQLClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sparql")
class SPARQLController(private val graphDBSPARQLClient: GraphDBSPARQLClient) {

    @Operation(summary = "Allows for executing SPARQL against the local GraphDB instance (see: application.properties -> graphdb.sparql.url)")
    @PostMapping(consumes = ["text/plain"], produces = ["application/sparql-results+json"])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [ExampleObject(name = "SPARQL SELECT", value = "select * where { ?s ?p ?o . } limit 100")]
        )]
    )
    fun sparql(@RequestBody sparql: String): String {
        return graphDBSPARQLClient.executeSPARQL(sparql)
    }
}