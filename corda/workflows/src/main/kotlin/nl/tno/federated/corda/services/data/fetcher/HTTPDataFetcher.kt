package nl.tno.federated.corda.services.data.fetcher

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class HTTPDataFetcher : DataFetcher {

    private val log = LoggerFactory.getLogger(SPARQLDataFetcher::class.java)
    // for codgnotto => call their GET endpoint (HTTP data fetcher)
    private val propertiesFileName = "database.properties"
    private val log = LoggerFactory.getLogger(HTTPDataFetcher::class.java)

    override fun fetch(): String {
        private val httpAnswer = executeHTTPGET() ?: ""
        // TODO: how to handle if httpAnswer null? => Codognotto get is down => lazy implementation?
        return runTranslateLab(httpAnswer)
    }

    private fun executeHTTPGET(societa: Int, anno: Int, numero: Int): String? {
        val uri = properties.get("get.endpoint.url")
        return client.get(URI("$uri?societa=${societa}&anno=${anno}&numero=${numero}"))?.bodyAsString
    }
    // TODO: insert the uri of the rml endpoint
    private fun runTranslateLab(input: String) {
        val uri = properties.get("rml.endpoint.url")

    }

    private val properties: Properties by lazy {
        getInputStreamFromClassPathResource(propertiesFileName).use {
            if (it == null) log.warn("${propertiesFileName} could not be found!")
            val properties = Properties()
            properties.load(it)

            with(System.getProperties()) {
                getProperty("get.endpoint.url")?.run {
                    log.info("Overriding ${propertiesFileName} with System properties: get.endpoint.url: {}", this)
                    properties.setProperty("get.endpoint.url", this)
                }
            }
            
            log.info("Loaded ${propertiesFileName}: get.endpoint.url: {}", properties.get("get.endpoint.url"))
            properties
        }
    }
    private var externalHttpClient: HttpClient? = null

    private fun HttpClient.get(uri: URI): HttpResponse? {
        val request = HttpGet(uri).apply {
            setHeader("Accept", "application/json")
        }
        return execute(request)
    }

    private val client: HttpClient by lazy {
        if (externalHttpClient != null) externalHttpClient!!
        else {
            HttpClientBuilder.create().build()
        }
    }



        // reverse RML => interpreter
        // configure what data fether to use
    }

}