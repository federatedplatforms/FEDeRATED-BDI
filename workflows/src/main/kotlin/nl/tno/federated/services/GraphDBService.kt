package nl.tno.federated.services

import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets.UTF_8
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap


object GraphDBService {

    fun isRepositoryAvailable(): Boolean {
        val uri = getRepositoryURI()
        val body = retrieveUrlBody(uri.toURL(), RequestMethod.GET)
        return body == "Missing parameter: query"
    }

    private fun getRepositoryURI(): URI {
        val propertyFile = File("database.properties").inputStream()
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
        con.setRequestProperty("Content-Type", "text/turtle")
        con.setRequestProperty("Accept", "application/json")

        if (body.isNotBlank()) {
            con.doOutput = true
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

        val missingParams = mutableListOf("milestone", "timestamps", "id")

        for(line in lines) {

            // Extract Event ID
            if(line.toLowerCase().contains(":event-")) {
                id = line.toLowerCase().substringAfter(":event-").split(" ")[0]
                missingParams.remove("id")
            }

            // Extract Timestamp
            if(line.contains(":hasTimestamp")) {
                val stringDate = line
                        .substringAfter(":hasTimestamp")
                        .substringAfter("\"")
                    .substringBefore("\"")

                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
                val eventDate = formatter.parse(stringDate)

                // Extract the Type of timestamp
                for(lineSecondScan in lines) {
                    if(lineSecondScan.contains(":hasDateTimeType")) {
                        val stringType = lineSecondScan
                                .substringAfter(":hasDateTimeType ")
                                .split(":",";")[1]
                        when(stringType.toLowerCase()) {
                            "actual" -> timestamps[EventType.ACTUAL] = eventDate
                            "estimated" -> timestamps[EventType.ESTIMATED] = eventDate
                            "planned" -> timestamps[EventType.PLANNED] = eventDate
                            else -> throw IllegalArgumentException("The type of the timestamp must be specified")
                        }
                    }
                }
                missingParams.remove("timestamps")
            }

            // Extract Milestone
            if(line.contains(":hasMilestone")) {
                milestone = if(line.toLowerCase().contains("end")) Milestone.STOP else Milestone.START

                missingParams.remove("milestone")
            }

            // Extract Digital Twins
            if(line.contains(":involvesDigitalTwin")) {

                val listOfDigitalTwins = line.toLowerCase().substringAfter(":involvesdigitaltwin ").split(" ")

                for(digitalTwin in listOfDigitalTwins) {
                    val DTuuid = digitalTwin.substringAfter("digitaltwin-").substring(0,36)

                    for(lineSecondScan in lines.map { it.toLowerCase() }) {
                        if(lineSecondScan.contains(DTuuid) && lineSecondScan.contains("a digitaltwin:")) {
                            val DTtype = lineSecondScan.substringAfter("a digitaltwin:").split(" ",",",";",".")[0]

                            when (DTtype) {
                                "goods" -> goods.add( UUID.fromString(DTuuid) )
                                "equipment" -> goods.add( UUID.fromString(DTuuid) )
                                "transportmeans" -> transportMeans.add( UUID.fromString(DTuuid) )
                                else -> otherDT.add( UUID.fromString(DTuuid) )
                            }
                        }
                    }
                }
            }

            // Extract Location
            if(line.contains(":involvesPhysicalInfrastructure")) {
                val words = line.toLowerCase().split(" ")
                for (word in words) {
                    if (word.contains(":physicalinfrastructure-")) {
                        val physicalInfrastructureCode = word.substringAfter(":physicalinfrastructure-").split(" ",",",";",".")[0]

                        location.add(physicalInfrastructureCode)
                    }
                }
            }
        }

        require(missingParams.isEmpty()) { "The following params are missing: " + missingParams }

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