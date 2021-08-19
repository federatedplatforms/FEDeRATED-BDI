package nl.tno.federated.services

import nl.tno.federated.states.EventState
import java.io.FileReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.*


object GraphDBService {



    fun isRepositoryAvailable(): Boolean {
        val propertyFile = FileReader("/home/graafewd/platform-corda2/workflows/src/test/resources/database.properties")
        val properties = Properties()
        properties.load(propertyFile)
        val protocol = properties.getProperty("triplestore.protocol")
        val host = properties.getProperty("triplestore.host")
        val port = properties.getProperty("triplestore.port")
        val repository = properties.getProperty("triplestore.repository")

        val body = retrieveUrlBody(URL("$protocol://$host:$port/repositories/$repository"), RequestMethod.GET)
        return body == "Missing parameter: query"
    }

    fun isDataValid(eventState: EventState): Boolean {
        val sparql = ""
        val result = performSparql(sparql, RequestMethod.GET)
        return "fail" !in result
    }

    fun queryData(): String {
        val sparql = "select * where { " +
                "?s ?p ?o ." +
                "} limit 10 "
        return performSparql(sparql, RequestMethod.GET)
    }

    fun insertEvent(): Boolean {
        val result = performSparql(
                """
                    PREFIX ex: <http://www.example.com/>
                    INSERT {
                        ex:Jeroen ex:likes ex:Erik .
                    }  
                    WHERE {}
                """, RequestMethod.POST
        )
        return true
    }

    private fun performSparql(sparql: String, requestMethod: RequestMethod): String {
        val url = URI("http", "//federated.sensorlab.tno.nl:7200/repositories/jeroentest?query=$sparql", null).toURL()
        return retrieveUrlBody(url, requestMethod)
    }

    private fun retrieveUrlBody(url: URL, requestMethod: RequestMethod): String {
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = requestMethod.toString()
        con.connectTimeout = 5000
        con.readTimeout = 5000

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
        GET, POST, PUT, OPTION
    }
}