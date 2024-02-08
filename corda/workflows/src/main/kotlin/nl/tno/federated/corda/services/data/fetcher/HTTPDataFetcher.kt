package nl.tno.federated.corda.services.data.fetcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.tno.federated.corda.services.graphdb.GraphDBClientException
import nl.tno.federated.corda.services.graphdb.GraphDBServerException
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.HttpClientBuilder
import org.jose4j.json.internal.json_simple.JSONObject
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class HTTPDataFetcher : DataFetcher {

    // for codgnotto => call their GET endpoint (HTTP data fetcher)
    private val propertiesFileName = "database.properties"
    private val logHTTPDataFetcher = LoggerFactory.getLogger(HTTPDataFetcher::class.java)

    override fun fetch(input: String): String? {
        val societa = properties["societa"].toString().toInt()
        val anno = properties["anno"].toString().toInt()
        val numero = properties["numero"].toString().toInt()
        val httpAnswer = executeHTTPGET(societa, anno, numero) ?: ""
        // TODO: how to handle if httpAnswer null? => Codognotto endpoint is down => lazy implementation?
        return runTranslateLab(httpAnswer)
    }

    private fun executeHTTPGET(societa: Int, anno: Int, numero: Int): String? {
        val uri = properties["get.endpoint.url"]
        return client.get(URI("$uri/${societa}/${anno}/${numero}"))?.bodyAsString
    }

    private fun runTranslateLab(input: String): String? {
        val uri = properties["rml.endpoint.url"]
        val responseBody = client.post(URI("$uri"))?.bodyAsString
        return if (responseBody.isNullOrEmpty()) null
        else extractPayload(responseBody!!)
    }

    private val properties: Properties by lazy {
        getInputStreamFromClassPathResource(propertiesFileName).use {
            if (it == null) logHTTPDataFetcher.warn("${propertiesFileName} could not be found!")
            val properties = Properties()
            properties.load(it)

            with(System.getProperties()) {
                getProperty("get.endpoint.url")?.run {
                    logHTTPDataFetcher.info("Overriding ${propertiesFileName} with System properties: get.endpoint.url: {}", this)
                    properties.setProperty("get.endpoint.url", this)
                }
            }

            logHTTPDataFetcher.info("Loaded ${propertiesFileName}: get.endpoint.url: {}", properties.get("get.endpoint.url"))
            logHTTPDataFetcher.info("Loaded ${propertiesFileName}: societa: {}", properties.get("societa"))
            logHTTPDataFetcher.info("Loaded ${propertiesFileName}: anno: {}", properties.get("anno"))
            logHTTPDataFetcher.info("Loaded ${propertiesFileName}: numero: {}", properties.get("numero"))

            properties
        }
    }
    private var externalHttpClient: HttpClient? = null

    private val client: HttpClient by lazy {
        if (externalHttpClient != null) externalHttpClient!!
        else {
            HttpClientBuilder.create().build()
        }
    }

    private fun HttpClient.get(uri: URI): HttpResponse? {
        val request = HttpGet(uri).apply {
            setHeader("Accept", "application/json")
        }
        return execute(request)
    }

    private fun HttpClient.post(uri: URI): HttpResponse? {
        val request = HttpPost(uri).apply {
            setHeader("Accept", "application/json")
        }
        return  execute(request)
    }

    private val HttpEntity.contentAsString: String
        get() = String(content.readBytes(), UTF_8)

    private fun extractPayload(response: String): String?
    {
        val mapper = jacksonObjectMapper()
        val jsonResponse = mapper.readTree(response).at("/payload")
        return if (!jsonResponse.isMissingNode) jsonResponse.textValue()
        else null
    }

    private val HttpResponse.bodyAsString: String?
        get() {
            return when (statusLine.statusCode) {
                in 400..499 -> throw GraphDBClientException(entity?.contentAsString ?: "Bad request when accessing GraphDB")
                in 500..599 -> throw GraphDBServerException(entity?.contentAsString ?: "Internal server error when accessing GraphDB")
                else -> entity?.contentAsString
            }
        }
}
