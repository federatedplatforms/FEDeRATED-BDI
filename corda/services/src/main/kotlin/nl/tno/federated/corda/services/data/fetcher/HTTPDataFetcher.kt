package nl.tno.federated.corda.services.data.fetcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.tno.federated.corda.services.graphdb.GraphDBClientException
import nl.tno.federated.corda.services.graphdb.GraphDBServerException
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8

abstract class HTTPDataFetcher(
    private val client: HttpClient =  HttpClientBuilder.create().build(),
    private val mapper: ObjectMapper = jacksonObjectMapper()
) : DataFetcher {

    private val log = LoggerFactory.getLogger(HTTPDataFetcher::class.java)

    fun get(uri: URI): HttpResponse? {
        log.info("HttpClient.get: {}", uri.toString())
        val request = HttpGet(uri).apply {
            setHeader("Accept", "application/json")
        }
        return client.execute(request)
    }

    fun post(uri: URI, payload: String? = null): HttpResponse? {
        log.info("HttpClient.post: {}", uri.toString())
        val request = HttpPost(uri).apply {
            setHeader("Accept", "application/json")
            if(!payload.isNullOrEmpty()) {
                entity = StringEntity(payload)
            }
        }
        return client.execute(request)
    }

    private val HttpEntity.contentAsString: String
        get() = String(content.readBytes(), UTF_8)

    fun extractPayload(response: String, jsonPointerExpression: String): String? {
        val jsonResponse = mapper.readTree(response).at(jsonPointerExpression)
        return when {
            jsonResponse.isMissingNode -> null
            else -> jsonResponse.textValue()
        }
    }

    val HttpResponse.bodyAsString: String?
        get() {
            return when (statusLine.statusCode) {
                in 400..499 -> throw GraphDBClientException(entity?.contentAsString ?: "Bad request when accessing GraphDB")
                in 500..599 -> throw GraphDBServerException(entity?.contentAsString ?: "Internal server error when accessing GraphDB")
                else -> entity?.contentAsString
            }
        }
}
