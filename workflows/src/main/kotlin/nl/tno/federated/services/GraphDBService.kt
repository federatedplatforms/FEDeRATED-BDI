package nl.tno.federated.services

import nl.tno.federated.states.*
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl.SimpleLiteral
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.RDFFormat.TURTLE
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
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

    fun parseRDFtoEventNew(rdfFullData: String) : List<Event> {
        val model = parseRDFToModel(rdfFullData)

        val eventIds = eventIdsFromModel(model)
        require(eventIds.isNotEmpty()) { "No events found in RDF data. "}

        val factory = SimpleValueFactory.getInstance()

        return eventIds.map {
            eventFromModel(model, it)
        }
    }

    private fun eventFromModel(
        model: Model,
        eventId: String
    ): Event {
        val dts = digitalTwinsFromModel(model, eventId)
        val goods = dts[PhysicalObject.GOOD]!!.map { uuidFromModel(it) }.toSet()
        val transportMeans = dts[PhysicalObject.TRANSPORTMEAN]!!.map { uuidFromModel(it) }.toSet()
        val otherDigitalTwins = dts[PhysicalObject.OTHER]!!.map { uuidFromModel(it) }.toSet()

        return Event(
            // TODO CARGO?
            goods = goods,
            transportMean = transportMeans,
            location = locationsFromModel(model, eventId),
            otherDigitalTwins = otherDigitalTwins,
            timestamps = timestampFromModel(model, eventId),
            ecmruri = "ecmruri", // TODO?
            milestone = milestoneFromModel(model, eventId),
            fullEvent = fullEventFromModel(model, eventId),
            id = uuidFromModel(eventId).toString()
        )
    }

    private fun locationsFromModel(model: Model, eventId: String): Set<String> {
        val factory = SimpleValueFactory.getInstance()
        val locations = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#involvesPhysicalInfrastructure"),
            null
        )

        return locations.objects().map { it.toString().substringAfter("-") }.toSet()
    }

    private fun milestoneFromModel(model: Model, eventId: String): Milestone {
        val factory = SimpleValueFactory.getInstance()
        val milestones = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasMilestone"),
            null
        )

        require(milestones.size == 1) { "Found multiple milestones for event $eventId" }
        val milestone = milestones.objects().first().toString()
        return if (milestone == "https://ontology.tno.nl/logistics/federated/Event#End") Milestone.STOP else Milestone.START
    }

    private fun fullEventFromModel(model: Model, eventId: String) : String {
        return eventId // TODO
    }

    private fun uuidFromModel(fullString: String) : UUID {
        val uuidAsString = fullString.substringAfter("-")
        return UUID.fromString(uuidAsString)
    }

    private fun timestampFromModel(
        model: Model,
        eventId: String
    ): LinkedHashMap<EventType, Date> {
        val factory = SimpleValueFactory.getInstance()
        val timestampType = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasDateTimeType"),
            null
        ).objects()
        require(timestampType.size == 1) { "Found multiple timestamptypes for event $eventId" }
        val eventType = when(timestampType.first().toString().substringAfter("#").toLowerCase()) {
            "actual" -> EventType.ACTUAL
            "estimated" -> EventType.ESTIMATED
            "planned" -> EventType.PLANNED
            else -> throw IllegalArgumentException("Unknown eventtype found for event $eventId. Found ${timestampType.first().toString()}.")
        }

        val timestamps = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasTimestamp"),
            null
        ).objects()
        require(timestamps.size == 1) { "Found multiple timestamps for event $eventId" }
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val timestamp = timestamps.first() as SimpleLiteral
        val eventDate = formatter.parse(timestamp.label)
        return linkedMapOf(eventType to eventDate)
    }

    private fun digitalTwinsFromModel(
        model: Model,
        eventId: String
    ): Map<PhysicalObject, Set<String>> {
        val factory = SimpleValueFactory.getInstance()
        val digitalTwins = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#involvesDigitalTwin"),
            null
        )

        val digitalTwinIds = digitalTwins.objects().map { it.toString() }
        val typedDigitalTwins = digitalTwinIds.map { it to digitalTwinTypeFromId(model, it)}.toMap()

        return mapOf(
            PhysicalObject.GOOD to typedDigitalTwins.filterValues { it == PhysicalObject.GOOD }.keys,
            PhysicalObject.TRANSPORTMEAN to typedDigitalTwins.filterValues { it == PhysicalObject.TRANSPORTMEAN }.keys,
            PhysicalObject.LOCATION to typedDigitalTwins.filterValues { it == PhysicalObject.LOCATION }.keys,
            PhysicalObject.OTHER to typedDigitalTwins.filterValues { it == PhysicalObject.OTHER }.keys,
            PhysicalObject.CARGO to typedDigitalTwins.filterValues { it == PhysicalObject.CARGO }.keys)
    }

    private fun digitalTwinTypeFromId(model: Model, id: String) : PhysicalObject {
        val factory = SimpleValueFactory.getInstance()
        val digitalTwins = model.filter(
            factory.createIRI(id),
            factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            null
        )

        val objectTypes = digitalTwins.objects().map { it.toString()}

        return if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#TransportMeans")) PhysicalObject.TRANSPORTMEAN
        else if (objectTypes.any { it in listOf("https://ontology.tno.nl/logistics/federated/DigitalTwin#Good", "https://ontology.tno.nl/logistics/federated/DigitalTwin#Equipment") })  PhysicalObject.GOOD
        else if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#Location")) PhysicalObject.LOCATION
        else if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#Cargo")) PhysicalObject.CARGO
        else PhysicalObject.OTHER
    }

    private fun eventIdsFromModel(model: Model): List<String> {
        val subjects = model.subjects().map { it.toString() }
        return subjects.filter { it.toLowerCase().contains("#event-") }
    }

    private fun parseRDFToModel(rdfFullData: String): Model {
        val model: Model = LinkedHashModel()
        val rdfParser = Rio.createParser(TURTLE)
        rdfParser.setRDFHandler(StatementCollector(model))
        rdfParser.parse(rdfFullData.byteInputStream())
        return model
    }

    fun parseRDFtoEvent(rdfFullData: String ) : Event {

        val goods = emptyList<UUID>().toMutableSet()
        val transportMeans = emptyList<UUID>().toMutableSet()
        val location = emptyList<String>().toMutableSet()
        val otherDT = emptyList<UUID>().toMutableSet()
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
                                "goods", "equipment" -> goods.add( UUID.fromString(DTuuid) )
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
                otherDT,
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