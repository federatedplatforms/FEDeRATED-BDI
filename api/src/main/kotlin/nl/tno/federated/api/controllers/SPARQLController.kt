package nl.tno.federated.api.controllers

import nl.tno.federated.api.graphdb.GraphDBClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SPARQLController(private val graphDBClient: GraphDBClient) {

    @PostMapping("/sparql", consumes = ["text/plain"], produces = ["application/sparql-results+json"])
    fun sparql(@RequestBody sparql: String): String {
        return graphDBClient.executeSPARQL(sparql)
    }
}