package nl.tno.federated.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.corda.core.internal.toPath
import nl.tno.federated.states.Event
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.HttpClientBuilder
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8
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

/**
 * TODO change object to class and split into a @CordaService and Spring @Service component.
 */
object GraphDBService {

    private val log = LoggerFactory.getLogger(GraphDBService::class.java)

    private val properties: Properties by lazy {
        getInputStreamFromClassPathResource("database.properties").use {
            if (it == null) log.warn("database.properties could not be found!")
            val properties = Properties()
            properties.load(it)

            with(System.getProperties()) {
                getProperty("triplestore.protocol")?.run {
                    properties.setProperty("triplestore.protocol", this)
                }
                getProperty("triplestore.host")?.run {
                    properties.setProperty("triplestore.host", this)
                }
                getProperty("triplestore.port")?.let {
                    properties.setProperty("triplestore.port", it)
                }
            }
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

    /**
     * Does not really belong here.
     */
    fun parseRDFToEvents(rdfFullData: String): List<Event> = GraphDBEventConverter.parseRDFToEvents(rdfFullData)

    fun queryEventIds(): String {
        val sparql = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/event#>
            SELECT ?x WHERE {
              ?x a Event:Event
            }         
        """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    fun generalSPARQLquery(query: String, privateRepo: Boolean = false): String {
        return performSparql(query.trimIndent(), privateRepo) ?: ""
    }

    fun queryEventById(id: String): String {
        assertSPARQLInput(id)

        assertSPARQLInput(id)

        val sparql = """
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            select ?s where {
            ?s a Event:Event .
            FILTER regex (STR(?s), "$id")
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    fun queryAllEventPropertiesById(id: String): String {
        val sparql = """
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            PREFIX DigitalTwin: <https://ontology.tno.nl/logistics/federated/DigitalTwin#>
            SELECT DISTINCT ?subject ?object ?object1 ?object2
	        WHERE {
                ?subject a Event:Event .
                ?subject Event:involvesBusinessTransaction ?object .
                ?subject Event:involvesDigitalTwin ?object1, ?object2 .
                ?object1 a DigitalTwin:Equipment .
                ?object2 a DigitalTwin:TransportMeans .
                FILTER regex (STR(?subject), "$id")
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
    }

    fun queryEventComponent(id: String): String {
        val sparql = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            PREFIX ex: <http://example.com/base#>
            PREFIX businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#>
            SELECT ?subject
            WHERE {
                ?subject a owl:NamedIndividual
                FILTER (?subject = ex:$id)
            }
            """.trimIndent()
        return performSparql(sparql) ?: ""
        return performSparql(sparql) ?: ""
    }

    fun isQueryResultEmpty(queryResult: String): Boolean {
        val mapper = jacksonObjectMapper().readTree(queryResult)

        val queryBindings = mapper["results"]["bindings"].elements()

        return !queryBindings.hasNext()
    }

    // order of the arguments in queryResult: object = business transaction, object1 = equipment, object2 = transport means
    fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean {
        val mapper = jacksonObjectMapper().readTree(queryResult)

        val bindings = mapper["results"]["bindings"]

        val businessTransactionFromQueryURI = bindings[0]["object"]["value"].asText()
        val businessTransactionFromQuery = businessTransactionFromQueryURI.split("#")[1]
        if (businessTransaction != businessTransactionFromQuery) return false

        val equipmentFromQueryURI = bindings[0]["object1"]["value"].asText()
        val equipmentFromQuery = equipmentFromQueryURI.split("#")[1]
        if (equipmentUsed != equipmentFromQuery) return false

        val transportMeansFromQueryURI = bindings[0]["object2"]["value"].asText()
        val transportMeansFromQuery = transportMeansFromQueryURI.split("#")[1]
        if (transportMeans != transportMeansFromQuery) return false

        return true
    }

    fun insertEvent(ttl: String, privateRepo: Boolean): Boolean {
        val uri = getRepositoryURI(privateRepo)
        val result = client.post(URI("$uri/statements"), ttl)
        return result.bodyAsString.isNullOrEmpty()
    }

    private fun performSparql(sparql: String, privateRepo: Boolean = false): String? {
        val uri = getRepositoryURI(privateRepo)
        return client.get(URI("$uri?query=${URLEncoder.encode(sparql, UTF_8.toString())}"))?.bodyAsString
    }

    /**
     * Creates the repository from the provided file. Since the GraphDB repository manager
     * does not support this we do it here.
     *
     * Usage: GraphDBService.createRemoteRepositoryFromConfig("bdi-repository-config.ttl")
     * Where the filename should exist on the classpath.
     */
    fun createRemoteRepositoryFromConfig(filename: String) {
        val config = getResourceFromClassPath(filename).toPath().toFile()
        log.info("Creating repository from: $config")
        val httpPost = HttpPost("${getGraphDBBaseUri()}/rest/repositories")

        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("config", config, ContentType.APPLICATION_OCTET_STREAM, config.name)
        httpPost.entity = builder.build()

        val response: HttpResponse = client.execute(httpPost)
        val body = response.entity.contentAsString

        when (response.statusLine.statusCode) {
            in 200..299 -> log.info("Repository created successfully!")
            400 -> log.info("Bad request: {}", body)
            else -> {
                log.warn("Repository creation failed. StatusCode: ${response.statusLine.statusCode}, ResponseBody: $body")
            }
        }
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

    fun importGraphFile(filename: String, context: String) {
        require(uploadFile(filename))
        require(importFile(filename, context))
    }

    private fun getGraphDBBaseUri(): String {
        return with(properties) {
            val protocol = getProperty("triplestore.protocol")
            val host = getProperty("triplestore.host")
            val port = getProperty("triplestore.port")
            "$protocol://$host:$port"
        }
    }

    fun uploadFile(filename: String): Boolean {
        val graph = getResourceFromClassPath(filename).toPath().toFile()
        log.info("Uploading file $filename")
        val httpPost = HttpPost(URL("${getGraphDBBaseUri()}/rest/repositories/bdi/import/upload/update/file").toURI())

        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", graph, ContentType.APPLICATION_OCTET_STREAM, graph.name)
        val textBody = """
            {"name":"$filename",
            "status":"NONE",
            "message":"",
            "context":null,
            "replaceGraphs":[],
            "baseURI":null,
            "forceSerial":false,
            "type":null,
            "format":null,
            "data":null,
            "timestamp":1667834639926,
            "parserSettings":
                {
                "preserveBNodeIds":false,
                "failOnUnknownDataTypes":false,
                "verifyDataTypeValues":false,
                "normalizeDataTypeValues":false,
                "failOnUnknownLanguageTags":false,
                "verifyLanguageTags":true,
                "normalizeLanguageTags":false,
                "stopOnError":true
                },
            "requestIdHeadersToForward":null}
        """.trimIndent()
        builder.addTextBody("importSettings", textBody, ContentType.APPLICATION_JSON)
        httpPost.entity = builder.build()

        val response: HttpResponse = client.execute(httpPost)
        val body = response.entity.contentAsString

        when (response.statusLine.statusCode) {
            200 -> {
                log.info("$filename uploaded successfully!").also { return true }
            }

            400 -> log.info("Bad request: {}", body)
            else -> {
                log.warn("Uploading of $filename failed. StatusCode: ${response.statusLine.statusCode}, ResponseBody: $body")
            }
        }

        return false
    }

    fun importFile(filename: String, context: String): Boolean {
        log.info("Importing $filename")
        val httpPost = HttpPost(URL("${getGraphDBBaseUri()}/rest/repositories/bdi/import/upload/file").toURI())
        val textBody = """
            {"name":"$filename",
            "type":"file",
            "file":null,
            "status":"NONE",
            "message":"",
            "context":"$context",
            "replaceGraphs":[],
            "baseURI":null,
            "forceSerial":false,
            "format":null,
            "data":"c23b728d-0f1f-4f91-ad7e-bcc9599393f2",
            "timestamp":1667836557602,
            "parserSettings":
                {
                "preserveBNodeIds":false,
                "failOnUnknownDataTypes":false,
                "verifyDataTypeValues":false,
                "normalizeDataTypeValues":false,
                "failOnUnknownLanguageTags":false,
                "verifyLanguageTags":true,
                "normalizeLanguageTags":false,
                "stopOnError":true
                },
            "requestIdHeadersToForward":null}
        """.trimIndent()
        val builder = MultipartEntityBuilder.create()
        builder.addTextBody("importSettings", textBody, ContentType.APPLICATION_JSON)
        httpPost.entity = builder.build()

        val response: HttpResponse = client.execute(httpPost)
        val body = response.entity.contentAsString

        when (response.statusLine.statusCode) {
            202 -> log.info("$filename imported successfully!").also { return true }
            400 -> log.info("Bad request: {}", body)
            else -> {
                log.warn("Repository creation failed. StatusCode: ${response.statusLine.statusCode}, ResponseBody: $body")
            }
        }

        return false
    }

    private fun getInputStreamFromClassPathResource(filename: String): InputStream? {
        return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    }

    private fun getResourceFromClassPath(filename: String): URL {
        return Thread.currentThread().contextClassLoader.getResource(filename)!!
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