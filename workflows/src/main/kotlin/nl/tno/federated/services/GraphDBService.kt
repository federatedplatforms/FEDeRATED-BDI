package nl.tno.federated.services

import nl.tno.federated.states.EventState
import java.io.FileReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*


object GraphDBService {

    fun isRepositoryAvailable(): Boolean {
        val uri = getRepositoryURI()
        val body = retrieveUrlBody(uri.toURL(), RequestMethod.GET)
        return body == "Missing parameter: query"
    }

    private fun getRepositoryURI(): URI {
        val propertyFile = FileReader("src/test/resources/database.properties") // TODO automatically grab the right properties file from the right resources folder
        val properties = Properties()
        properties.load(propertyFile)
        val protocol = properties.getProperty("triplestore.protocol")
        val host = properties.getProperty("triplestore.host")
        val port = properties.getProperty("triplestore.port")
        val repository = properties.getProperty("triplestore.repository")

        return URI("$protocol://$host:$port/repositories/$repository")
    }

    fun isDataValid(eventState: EventState): Boolean { // TODO ideally match eventstate contents to its eventString too
        val sparql = ""
        val result = performSparql(sparql, RequestMethod.GET)
        return "fail" !in result
    }

    fun queryEventIds(): Map<String, String> { // TODO return only list of event ids?
        val sparql = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            SELECT ?x ?z WHERE {
              ?x rdfs:label ?z.
              ?x a Event:Event
            }         
        """.trimIndent()
        val results = performSparql(sparql, RequestMethod.GET)
        return parseEventLabels(results)
    }

    private fun parseEventLabels(json: String): Map<String, String> { // or List<String>, you decide
        return emptyMap() // TODO return parsed event ids and rdf labels
    }

    fun insertEvent(ttl: String): Boolean {
        val uri = getRepositoryURI()
        val url = URL("$uri/statements")
        val result = retrieveUrlBody(url,
            RequestMethod.POST,
            ttl
        )
        return result.isEmpty()
    }

    private fun performSparql(sparql: String, requestMethod: RequestMethod): String {
        val url = URI("http", "//federated.sensorlab.tno.nl:7200/repositories/federated-shacl?query=$sparql", null).toURL()
        return retrieveUrlBody(url, requestMethod)
    }

    private fun retrieveUrlBody(url: URL, requestMethod: RequestMethod, body: String = ""): String {
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = requestMethod.toString()
        con.connectTimeout = 5000
        con.readTimeout = 5000
        con.setRequestProperty("Content-Type", "text/turtle");
        con.setRequestProperty("Accept", "application/json")

        if (body.isNotBlank()) {
            con.doOutput = true;
            con.outputStream.use { os ->
                val input: ByteArray = body.toByteArray(UTF_8)
                os.write(input, 0, input.size)
            }
        }

        if (con.responseCode in 200..299) {
            con.inputStream.bufferedReader().use {
                return it.readText()
            }
        }
        else {
            con.errorStream.bufferedReader().use {
                return it.readText()
            }
        }
    }

    enum class RequestMethod {
        GET, POST
    }
}