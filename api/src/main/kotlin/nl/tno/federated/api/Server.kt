package nl.tno.federated.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.time.Duration

/**
 * BDI API Spring Boot application.
 */
@SpringBootApplication(scanBasePackages = ["nl.tno.federated.api", "nl.tno.federated.semantic.adapter"])
@EnableSwagger2
class Server {

    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    lateinit var environment: Environment

    @Bean("tradelensRestTemplate")
    fun tradelensRestTemplate(): RestTemplate = restTemplateFor("tradelens")

    @Bean("ibmIdentityTokenRestTemplate")
    fun ibmIdentityTokenRestTemplate(): RestTemplate = restTemplateFor("ibm", ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
        request.headers.set("Content-Type", "application/x-www-form-urlencoded")
        execution.execute(request, body)
    })

    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()

    private fun restTemplateFor(propertiesPrefixName: String, interceptor: ClientHttpRequestInterceptor? = null) = restTemplateBuilder
        .setConnectTimeout(Duration.ofMillis(environment.getProperty("$propertiesPrefixName.connectTimeoutMillis")?.toLongOrNull() ?: 5000))
        .setReadTimeout(Duration.ofMillis(environment.getProperty("$propertiesPrefixName.readTimeoutMillis")?.toLongOrNull() ?: 5000))
        .interceptors(interceptor ?: ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            request.headers.set("Content-Type", "application/json")
            request.headers.set("Accept", "application/json")
            execution.execute(request, body)
        })
        .rootUri(with(environment) {
            val protocol = getProperty("$propertiesPrefixName.protocol")
            val host = getProperty("$propertiesPrefixName.host")
            val port = getProperty("$propertiesPrefixName.port")
            "$protocol://$host:$port"
        })
        .build()
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}