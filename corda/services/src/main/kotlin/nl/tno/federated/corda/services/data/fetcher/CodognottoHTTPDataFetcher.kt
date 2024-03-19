package nl.tno.federated.corda.services.data.fetcher

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.tno.federated.corda.services.properties.PropertiesReader
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.HttpClientBuilder
import java.net.URI
import java.util.*

class CodognottoHTTPDataFetcher(
    client: HttpClient =  HttpClientBuilder.create().build(),
    mapper: ObjectMapper = jacksonObjectMapper()
) : HTTPDataFetcher(client, mapper) {

    private val properties: Properties = PropertiesReader().readProperties("database.properties")

    override fun fetch(input: String): String? {
        val httpAnswer = callCodognotto() ?: return null
        return runTranslateLab(httpAnswer)
    }

    private fun callCodognotto(): String? {
        val societa = properties.getProperty("societa")
        val anno = properties.getProperty("anno")
        val numero = properties.getProperty("numero")
        val uri = properties.getProperty("get.endpoint.url").format(societa,anno,numero)
        return get(URI(uri))?.bodyAsString
    }

    private fun runTranslateLab(input: String): String? {
        val uri = properties["rml.endpoint.url"]
        val responseBody = post(URI("$uri"), input)
        val body = responseBody?.bodyAsString!!
        return extractPayload(body, "/payload")
    }
}
