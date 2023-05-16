package nl.tno.federated.api

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import nl.tno.federated.api.event.distribution.rules.BroadcastToAllEventDistributionRule
import nl.tno.federated.api.event.distribution.rules.EventDistributionRule
import nl.tno.federated.api.event.distribution.rules.SparqlEventDistributionRule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

/**
 * BDI API Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = ["nl.tno.federated.api"])
class Server {

    @Bean
    fun rules(cordaNodeService: CordaNodeService) = listOf(
        SparqlEventDistributionRule("SELECT *", listOf(CordaEventDestination(CordaX500Name("TNO", "Den Haag", "NL")))),
        BroadcastToAllEventDistributionRule(cordaNodeService)
    )
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}