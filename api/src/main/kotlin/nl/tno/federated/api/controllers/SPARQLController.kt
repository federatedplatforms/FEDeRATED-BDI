package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import nl.tno.federated.api.graphdb.GraphSPARQLClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SPARQLController(private val graphSPARQLClient: GraphSPARQLClient) {

    @PostMapping("/sparql", consumes = ["text/plain"], produces = ["application/sparql-results+json"])
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        content = [Content(
            examples = [ExampleObject(name = "SPARQL SELECT", value = "select * where { ?s ?p ?o . } limit 100")]
        )]
    )
    fun sparql(@RequestBody sparql: String): String {
        return graphSPARQLClient.executeSPARQL(sparql)
    }
}