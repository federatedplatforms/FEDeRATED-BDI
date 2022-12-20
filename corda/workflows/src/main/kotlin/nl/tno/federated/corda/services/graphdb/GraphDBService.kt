package nl.tno.federated.corda.services.graphdb

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import nl.tno.federated.services.PrefixHandlerQueries
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * Exception thrown when anything happens in the GraphDBService
 */
open class GraphDBException(message: String) : Exception(message)

/**
 * Exception that might occur when the GraphDB server returns an exception (500..599)
 */
class GraphDBClientException(message: String) : GraphDBException(message)

/**
 * Exception that might occur when an invalid request is made to the GraphDB server (400..499)
 */
class GraphDBServerException(message: String) : GraphDBException(message)

class GraphDBService : IGraphDBService {

    private val log = LoggerFactory.getLogger(GraphDBService::class.java)

    private val properties: Properties by lazy {
        getInputStreamFromClassPathResource("database.properties").use {
            if (it == null) log.warn("database.properties could not be found!")
            val properties = Properties()
            properties.load(it)

            with(System.getProperties()) {
                getProperty("triplestore.protocol")?.run {
                    log.info("Overriding database.properties with System properties: triplestore.protocol: {}", this)
                    properties.setProperty("triplestore.protocol", this)
                }
                getProperty("triplestore.host")?.run {
                    log.info("Overriding database.properties with System properties: triplestore.host: {}", this)
                    properties.setProperty("triplestore.host", this)
                }
                getProperty("triplestore.port")?.let {
                    log.info("Overriding database.properties with System properties: triplestore.port: {}", this)
                    properties.setProperty("triplestore.port", it)
                }
            }

            log.info("Loaded database.properties: triplestore.protocol: {}, triplestore.host: {}, triplestore.port: {}", properties.get("triplestore.protocol"), properties.get("triplestore.host"), properties.get("triplestore.port"))
            properties
        }
    }

    private var externalHttpClient: HttpClient? = null

    /**
     * See: https://www.baeldung.com/httpclient-timeout
     * And: https://www.baeldung.com/httpclient-connection-management
     */
    private val client: HttpClient by lazy {
        if (externalHttpClient != null) externalHttpClient!!
        else {
            HttpClientBuilder.create()
                .setMaxConnPerRoute(properties.getProperty("triplestore.maxConnectionsPerRoute").toIntOrNull() ?: 50)
                .setMaxConnTotal(properties.getProperty("triplestore.maxConnectionsTotal").toIntOrNull() ?: 100)
                .setDefaultRequestConfig(
                    RequestConfig.custom()
                        .setConnectTimeout(properties.getProperty("triplestore.connectTimeoutMillis").toIntOrNull() ?: 5000)
                        .setConnectionRequestTimeout(properties.getProperty("triplestore.connectTimeoutMillis").toIntOrNull() ?: 5000)
                        .setSocketTimeout(properties.getProperty("triplestore.socketTimeoutMillis").toIntOrNull() ?: 5000)
                        .build()
                )
                .build()
        }
    }

    override fun queryEventIds(): String {
        val sparql = """
            ${PrefixHandlerQueries.getPrefixesEvent()}
            select ?s where { 
                ?s a Event:Event
            }   
        """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    override fun generalSPARQLquery(query: String, privateRepo: Boolean): String {
        return performSparql(query.trimIndent(), privateRepo) ?: ""
    }

    override fun queryEventById(id: String): String {
        assertSPARQLInput(id)

        assertSPARQLInput(id)

        val sparql = """
            ${PrefixHandlerQueries.getPrefixesEvent()}
            select ?s where {
            ?s a Event:Event .
            FILTER regex (STR(?s), "$id")
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    override fun queryAllEventPropertiesById(id: String): String {
        val sparql = """
            ${PrefixHandlerQueries.getPrefixesEvent()}
            ${PrefixHandlerQueries.getPrefixesDigitalTwin()}
            SELECT DISTINCT ?subject ?object ?object1 ?object2
	        WHERE {
                ?subject a Event:Event .
                ?subject Event:involvesBusinessTransaction ?object .
                ?subject Event:involvesDigitalTwin ?object1, ?object2 .
                ?object1 a dt:Equipment .
                ?object2 a dt:TransportMeans .
                FILTER regex (STR(?subject), "$id")
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    override fun queryEventComponent(id: String): String {
        val sparql = """
            ${PrefixHandlerQueries.getPrefixesSemanticElements()}
            SELECT ?subject
            WHERE {
                ?subject a owl:NamedIndividual
                FILTER regex (STR(?subject), "$id")
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    override fun isQueryResultEmpty(queryResult: String): Boolean {
        val mapper = jacksonObjectMapper().readTree(queryResult)

        val queryBindings = mapper["results"]["bindings"].elements()

        return !queryBindings.hasNext()
    }

    // order of the arguments in queryResult: object = business transaction, object1 = equipment, object2 = transport means
    override fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean {
        val mapper = jacksonObjectMapper().readTree(queryResult)

        val bindings = mapper["results"]["bindings"]

        val businessTransactionFromQueryURI = bindings[0]["object"]["value"].asText()
        val businessTransactionFromQuery = businessTransactionFromQueryURI.split("#")[1]
        if (businessTransaction !in businessTransactionFromQuery) return false

        val equipmentFromQueryURI = bindings[0]["object1"]["value"].asText()
        val equipmentFromQuery = equipmentFromQueryURI.split("#")[1]
        if (equipmentUsed !in equipmentFromQuery) return false

        val transportMeansFromQueryURI = bindings[0]["object2"]["value"].asText()
        val transportMeansFromQuery = transportMeansFromQueryURI.split("#")[1]
        if (transportMeans !in transportMeansFromQuery) return false

        return true
    }

    override fun queryCityName(locationName: String): String {
        val queryCity = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
    PREFIX pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#>
    select ?oCity where { 
        ?s a Event:Event .
        ?s Event:involvesPhysicalInfrastructure ?o .
        ?o pi:cityName ?oCity .
        FILTER regex(str(?o), "$locationName")
        }""".trimIndent()

        return performSparql(queryCity) ?: ""
    }

    override fun queryCountryName(locationName: String): String {
        val queryCountry = """
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
    PREFIX pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#>
    select ?oCountry where { 
        ?s a Event:Event .
        ?s Event:involvesPhysicalInfrastructure ?o .
        ?o pi:countryName ?oCountry .
        FILTER regex(str(?o), "$locationName")
        }""".trimIndent()

        return performSparql(queryCountry) ?: ""
    }

    override fun queryCountryGivenEventId(eventId: String): String {
        val queryCountryByEventId = """
    PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
    PREFIX pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#>
    select ?oCountry where {
        ?s a Event:Event .
        ?s Event:involvesPhysicalInfrastructure ?o .
        ?o pi:countryName ?oCountry .
        FILTER regex(str(?s), "$eventId")
    }""".trimIndent()

        return performSparql(queryCountryByEventId) ?: ""
    }

    override fun unpackCountriesFromSPARQLresult(sparqlResult: String): List<String> {
        val mapper = jacksonObjectMapper().readTree(sparqlResult)
        val bindings = mapper["results"]["bindings"]

        val extractedCountries = mutableListOf<String>()

        for (country in bindings.elements()) {
            extractedCountries.add(country["oCountry"]["value"].asText())
        }

        return extractedCountries
    }

    override fun insertEvent(ttl: String, privateRepo: Boolean): Boolean {
        val uri = getRepositoryURI(privateRepo)
        val result = client.post(URI("$uri/statements"), ttl)
        log.info("Insert into GraphDB, statusCode: {}, responseBody: {}", result.statusLine.statusCode, result.bodyAsString)
        return result.bodyAsString.isNullOrEmpty()
    }

    private fun performSparql(sparql: String, privateRepo: Boolean = false): String? {
        val uri = getRepositoryURI(privateRepo)
        return client.get(URI("$uri?query=${URLEncoder.encode(sparql, UTF_8.toString())}"))?.bodyAsString
    }

    private fun getRepositoryURI(privateRepo: Boolean): String {
        return with(properties) {
            val protocol = getProperty("triplestore.protocol")
            val host = getProperty("triplestore.host")
            val port = getProperty("triplestore.port")
            val repository = if (privateRepo) getProperty("triplestore.private-repository") else getProperty("triplestore.repository")
            "$protocol://$host:$port/repositories/$repository"
        }
    }

    private fun getInputStreamFromClassPathResource(filename: String): InputStream? {
        val file = Paths.get(filename)
        if (Files.exists(file)) {
            log.info("Using file: {}", file.toAbsolutePath())
            return Files.newInputStream(file)
        }
        log.info("Using classpath resource: {}", filename)
        return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    }

    /**
     * Check for possible to S(PAR)QL injection.
     */
    private fun assertSPARQLInput(str: String) {
        val suspiciousChars = listOf('?', '{', '}', ':')
        if (str.any { it in suspiciousChars }) {
            throw GraphDBException("Suspicious character detected in SPARQL input.")
        }
    }
}

private val HttpEntity.contentAsString: String
    get() = String(content.readBytes(), UTF_8)

private fun HttpClient.get(uri: URI): HttpResponse? {
    val request = HttpGet(uri).apply {
        setHeader("Accept", "application/json")
    }
    return execute(request)
}

private fun HttpClient.post(uri: URI, body: String): HttpResponse {
    return post(
        uri, StringEntity(body), mapOf(
        "Accept" to "application/json",
        "Content-Type" to "text/turtle"
    )
    )
}

private fun HttpClient.post(uri: URI, entity: HttpEntity, headers: Map<String, String>): HttpResponse {
    val request = HttpPost(uri).apply {
        this.entity = entity
        headers.forEach { (key, value) ->
            setHeader(key, value)
        }
    }
    return execute(request)
}

private val HttpResponse.bodyAsString: String?
    get() {
        return when (statusLine.statusCode) {
            in 400..499 -> throw GraphDBClientException(entity?.contentAsString ?: "Bad request when accessing GraphDB")
            in 500..599 -> throw GraphDBServerException(entity?.contentAsString ?: "Internal server error when accessing GraphDB")
            else -> entity?.contentAsString
        }
    }