package nl.tno.federated.api

import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.rules.BroadcastToAllEventDistributionRule
import nl.tno.federated.api.graphdb.GraphDBSPARQLClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment

/**
 * BDI API Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = ["nl.tno.federated.api"])
class Server {

    /**
     * Rules are ordered, the first rule to return true will be used.
     */
    @Bean
    fun rules(cordaNodeService: CordaNodeService) = setOf(
        // SparqlEventDistributionRule("ASK { <http://example.org/some/namespace> ?p ?o> }", listOf(CordaEventDestination(CordaX500Name("TNO", "Den Haag", "NL")))),
        // KeywordMatchEventDistributionRule("NL", listOf(CordaEventDestination(CordaX500Name("TNO", "Den Haag", "NL"))
        BroadcastToAllEventDistributionRule(cordaNodeService)
    )

    @Bean
    fun graphDBSPARQLClient(environment: Environment) = GraphDBSPARQLClient(environment.getProperty("graphdb.sparql.url")!!)
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}