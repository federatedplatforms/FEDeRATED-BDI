package nl.tno.federated.corda.services.data.fetcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.HospitalizeFlowException
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@CordaService
class DataFetcherCordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private var dataFetcher: DataFetcher
    private val log = LoggerFactory.getLogger(DataFetcherCordaService::class.java)

    init {
        val properties =
            getInputStreamFromClassPathResource("database.properties").use {
                if (it == null) log.warn("database.properties could not be found!")
                val properties = Properties()
                properties.load(it)

                with(System.getProperties()) {
                    getProperty("graphdb.sparql.url")?.run {
                        log.info("Overriding database.properties with System properties: graphdb.sparql.url: {}", this)
                        properties.setProperty("graphdb.sparql.url", this)
                    }
                }

                log.info("Loaded database.properties: graphdb.sparql.url: {}", properties.get("graphdb.sparql.url"))
                properties
            }

        dataFetcher = when (properties.getProperty("datafetcher.type")) {
            "sparql" -> SPARQLDataFetcher(properties)
            "codognotto" -> CodognottoHTTPDataFetcher(properties = properties)
            "mock" -> MockDataFetcher()
            else -> throw RuntimeException("Invalid DataFetcher type specified: ${properties.getProperty("datafetcher.type")}")
        }
    }

    // Delegate all calls to the non Corda object.
    fun fetch(deduplicationId: String, input: String): String? {
        try {
            return dataFetcher.fetch(input)
        } catch (e: Exception) {
            throw HospitalizeFlowException("External API call failed", e)
        }
    }

    fun getInputStreamFromClassPathResource(filename: String): InputStream? {
        val file = Paths.get(filename)
        if (Files.exists(file)) {
            DataFetcher.log.info("Using file: {}", file.toAbsolutePath())
            return Files.newInputStream(file)
        }
        DataFetcher.log.info("Using classpath resource: {}", filename)
        return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    }
}

fun FlowLogic<*>.dataFetcher(): DataFetcherCordaService = serviceHub.cordaService(DataFetcherCordaService::class.java)