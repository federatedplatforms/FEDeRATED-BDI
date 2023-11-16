package nl.tno.federated.api

import nl.tno.federated.api.graphdb.GraphDBSPARQLClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * BDI API Spring Boot application.
 */
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = ["nl.tno.federated.api"])
class Server {

    @Bean
    @ConditionalOnProperty(prefix = "bdi.node.cors", name = ["enabled"], havingValue = "true")
    fun corsConfigurer(environment: Environment): WebMvcConfigurer? {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val allowedOrigins = environment.getProperty("bdi.node.cors.allowed-origins")
                if (!allowedOrigins.isNullOrEmpty()) {
                    registry.addMapping("/**").allowedOrigins(*allowedOrigins.split(",").toTypedArray())
                }
            }
        }
    }

    @Bean
    fun graphDBSPARQLClient(environment: Environment) = GraphDBSPARQLClient(environment.getProperty("graphdb.sparql.url")!!)
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}