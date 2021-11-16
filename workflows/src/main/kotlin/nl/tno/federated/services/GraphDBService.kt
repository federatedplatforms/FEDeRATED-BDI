package nl.tno.federated.services

import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap
import java.util.*


object GraphDBService {

    fun isRepositoryAvailable(): Boolean {
        val uri = getRepositoryURI()
        val body = retrieveUrlBody(uri.toURL(), RequestMethod.GET)
        return body == "Missing parameter: query"
    }

    private fun getRepositoryURI(): URI {
        val propertyFile = GraphDBService::class.java.classLoader.getResourceAsStream("database.properties")
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

    fun queryEventIds(): String {
        val sparql = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            SELECT ?x ?z WHERE {
              ?x rdfs:label ?z.
              ?x a Event:Event
            }         
        """.trimIndent()
        return performSparql(sparql, RequestMethod.GET)
    }

    fun generalSPARQLquery(query: String): String {
        return performSparql(query.trimIndent(), RequestMethod.GET)
    }

    fun queryEventById(id: String): String {
        val sparql = """
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#>
            PREFIX ex: <http://example.com/base#>
            SELECT ?subject ?object 
            WHERE {
                ?subject rdfs:label ?object .
                FILTER (?subject = ex:Event-$id)
            }
            """.trimIndent()
        return performSparql(sparql, RequestMethod.GET)
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
        val uri = getRepositoryURI()
        val url = URI(uri.scheme, "//" + uri.host + ":" + uri.port + uri.path + "?query=$sparql", null).toURL()
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

    fun parseRDFtoEvent(rdfFullData: String ) : Event {

        val goods = emptyList<UUID>().toMutableList()
        val transportMeans = emptyList<UUID>().toMutableList()
        val location = emptyList<String>().toMutableList()
        val otherDT = emptyList<UUID>().toMutableList()
        var id = ""
        val timestamps: LinkedHashMap<EventType, Date> = linkedMapOf()
        var milestone = Milestone.START

        val lines = rdfFullData.trimIndent().split("\n")

        for(line in lines) {

            // Extract Event ID
            if(line.contains(":Event-")) {
                id = line.substringAfter(":Event-").split(" ")[0]
            }

            // Extract Timestamp
            if(line.contains("Event:hasTimestamp")) {
                val stringDate = line
                        .substringAfter("Event:hasTimestamp")
                        .substringAfter("\"")
                    .substringBefore("\"")

                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                val eventDate = formatter.parse(stringDate)

                // Extract the Type of timestamp
                var type = EventType.PLANNED // Default type if not found
                for(lineSecondScan in lines) {
                    if(lineSecondScan.contains("Event:hasDateTimeType")) {
                        val stringType = lineSecondScan
                                .substringAfter("Event:hasDateTimeType ")
                                .split(":",";")[1]
                        when(stringType.toLowerCase()) {
                            "actual" -> type = EventType.ACTUAL
                            "estimated" -> type = EventType.ESTIMATED
                            // when "planned" it's like default, EventType.PLANNED
                        }
                    }
                }
                timestamps[type] = eventDate
            }

            // Extract Milestone
            if(line.contains("Event:hasMilestone")) {
                if(line.toLowerCase().contains("end")) milestone = Milestone.STOP
                // if it contains "start" it can stay as default (start)
            }

            // Extract Digital Twins
            if(line.contains("Event:involvesDigitalTwin")) {

                val listOfDigitalTwins = line.substringAfter("Event:involvesDigitalTwin ").split(" ")

                for(digitalTwin in listOfDigitalTwins) {
                    val DTuuid = digitalTwin.substringAfter("DigitalTwin-").substring(0,36)

                    for(lineSecondScan in lines) {
                        if(lineSecondScan.contains(DTuuid) && lineSecondScan.contains("a DigitalTwin:")) {
                            val DTtype = lineSecondScan.substringAfter("a DigitalTwin:").split(" ",",",";")[0]

                            if(DTtype == "Equipment") goods.add( UUID.fromString(DTuuid) )
                            if(DTtype == "TransportMeans") transportMeans.add( UUID.fromString(DTuuid) )
                        }
                    }
                }
            }

            // Extract Location
            if(line.contains("Event:involvesPhysicalInfrastructure")) {
                val words = line.split(" ")
                for (word in words) {
                    if (word.contains(":physicalInfrastructure-")) {
                        val physicalInfrastructureCode = word.split(":physicalInfrastructure-", ";", ",")[1]

                        location.add(physicalInfrastructureCode)
                    }
                }
            }
        }

        return Event(
                goods,
                transportMeans,
                location,
                emptyList(),
                timestamps,
                "ecmruri",
                milestone,
                rdfFullData,
                id
        )
    }

    enum class RequestMethod {
        GET, POST
    }
}