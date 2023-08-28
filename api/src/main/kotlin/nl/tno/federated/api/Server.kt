package nl.tno.federated.api

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

    @Bean
    fun graphDBSPARQLClient(environment: Environment) = GraphDBSPARQLClient(environment.getProperty("graphdb.sparql.url")!!)
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}