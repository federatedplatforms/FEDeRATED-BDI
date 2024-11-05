package nl.tno.federated.api

import nl.tno.federated.api.event.type.EventTypeMappingConfig
import nl.tno.federated.api.graphdb.GraphDBSPARQLClient
import nl.tno.federated.api.user.UserMappingConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ApplicationEventMulticaster
import org.springframework.context.event.SimpleApplicationEventMulticaster
import org.springframework.core.env.Environment
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories
import org.springframework.scheduling.annotation.EnableScheduling


/**
 * FEDeRATED Node API Spring Boot application.
 */
@EnableConfigurationProperties(EventTypeMappingConfig::class, UserMappingConfig::class)
@EnableJdbcRepositories
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["nl.tno.federated.api"])
class Server {

    @Bean(name = ["applicationEventMulticaster"])
    fun simpleApplicationEventMulticaster(): ApplicationEventMulticaster {
        val eventMulticaster = SimpleApplicationEventMulticaster()
        eventMulticaster.setTaskExecutor(SimpleAsyncTaskExecutor())
        return eventMulticaster
    }

    @Bean
    fun graphDBSPARQLClient(environment: Environment) = GraphDBSPARQLClient(environment.getProperty("federated.node.graphdb.sparql.url")!!)
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    SpringApplication.run(Server::class.java, *args)
}