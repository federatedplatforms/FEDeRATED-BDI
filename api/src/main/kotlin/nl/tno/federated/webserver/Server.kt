package nl.tno.federated.webserver

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
 * Our Spring Boot application.
 */
@SpringBootApplication
@EnableSwagger2
class Server {

    @Autowired
    lateinit var restTemplateBuilder: RestTemplateBuilder

    @Autowired
    lateinit var environment: Environment

    /**
     * Configures the ishare connection details.
     */
    @Bean("ishareRestTemplate")
    fun ishareRestTemplate() : RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(environment.getProperty("ishare.connectTimeoutMillis")?.toLongOrNull() ?: 5000))
            .setReadTimeout(Duration.ofMillis(environment.getProperty("ishare.readTimeoutMillis")?.toLongOrNull() ?: 5000))
            .interceptors(ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
                request.headers.set("Content-Type", "application/json")
                request.headers.set("Accept", "application/json")
                execution.execute(request, body)
            })
            .rootUri(with(environment) {
                val protocol = getProperty("ishare.protocol")
                val host = getProperty("ishare.host")
                val port = getProperty("ishare.port")
                "$protocol://$host:$port"
            })
            .build()
    }

    @Bean("semanticAdapterRestTemplate")
    fun semanticAdapterRestTemplate() : RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(environment.getProperty("semanticadapter.connectTimeoutMillis")?.toLongOrNull() ?: 5000))
            .setReadTimeout(Duration.ofMillis(environment.getProperty("semanticadapter.readTimeoutMillis")?.toLongOrNull() ?: 5000))
            .interceptors(ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
                request.headers.add("Content-Type", "application/json")
                request.headers.add("Accept", "application/json")
                execution.execute(request, body)
            })
            .rootUri(with(environment) {
                val protocol = getProperty("semanticadapter.protocol")
                val host = getProperty("semanticadapter.host")
                val port = getProperty("semanticadapter.port")
                "$protocol://$host:$port"
            })
            .build()
    }

    @Bean("tradelensRestTemplate")
    fun tradelensRestTemplate() : RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(environment.getProperty("tradelens.connectTimeoutMillis")?.toLongOrNull() ?: 5000))
            .setReadTimeout(Duration.ofMillis(environment.getProperty("tradelens.readTimeoutMillis")?.toLongOrNull() ?: 5000))
            .interceptors(ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
                request.headers.set("Content-Type", "application/json")
                request.headers.set("Accept", "application/json")
                execution.execute(request, body)
            })
            .rootUri(with(environment) {
                val protocol = getProperty("tradelens.protocol")
                val host = getProperty("tradelens.host")
                val port = getProperty("tradelens.port")
                "$protocol://$host:$port"
            })
            .build()
    }

    @Bean("ibmIdentityTokenRestTemplate")
    fun ibmIdentityTokenRestTemplate() : RestTemplate {
        return restTemplateBuilder
            .setConnectTimeout(Duration.ofMillis(environment.getProperty("ibm.connectTimeoutMillis")?.toLongOrNull() ?: 5000))
            .setReadTimeout(Duration.ofMillis(environment.getProperty("ibm.readTimeoutMillis")?.toLongOrNull() ?: 5000))
            .interceptors(ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
                request.headers.set("Content-Type", "application/x-www-form-urlencoded")
                request.headers.set("charset", "utf-8")
                execution.execute(request, body)
            })
            .rootUri(with(environment) {
                val protocol = getProperty("ibm.protocol")
                val host = getProperty("ibm.host")
                val port = getProperty("ibm.port")
                "$protocol://$host:$port"
            })
            .build()
    }

    @Bean
    fun api(): Docket = Docket(DocumentationType.SWAGGER_2)
        .select()
        .apis(RequestHandlerSelectors.any())
        .paths(PathSelectors.any())
        .build()
}

/**
 * Starts our Spring Boot application.
 */
fun main(args: Array<String>) {
    runApplication<Server>(*args)
}