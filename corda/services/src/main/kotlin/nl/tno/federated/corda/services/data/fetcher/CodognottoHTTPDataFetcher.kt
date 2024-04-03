package nl.tno.federated.corda.services.data.fetcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.*

class CodognottoHTTPDataFetcher(
    client: HttpClient =  HttpClientBuilder.create().build(),
    mapper: ObjectMapper = jacksonObjectMapper(),
    private val properties: Properties
) : HTTPDataFetcher(client, mapper) {

    private val get = URI(properties.getProperty("get.endpoint.url"))
    private val rml = URI(properties.getProperty("rml.endpoint.url"))
    private val log = LoggerFactory.getLogger(CodognottoHTTPDataFetcher::class.java)

    init {
        log.info("CodognottoHTTPDataFetcher get endpoint: {}, rml endpoint: {}", get, rml)
    }

    override fun fetch(input: String): String? {
        val httpAnswer = callCodognotto() ?: return null
        log.info("Received response from: {} calling: {}", get, rml)
        return runTranslateLab(httpAnswer)
    }

    private fun callCodognotto(): String? {
        return get(get)?.bodyAsString
    }

    private fun runTranslateLab(input: String): String? {
        val responseBody = post(rml, input)
        val body = responseBody?.bodyAsString!!
        return extractPayload(body, "/payload")
    }
}
