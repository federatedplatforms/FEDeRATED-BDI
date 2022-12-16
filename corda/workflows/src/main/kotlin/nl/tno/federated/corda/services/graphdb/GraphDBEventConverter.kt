package nl.tno.federated.corda.services.graphdb

import nl.tno.federated.states.*
import org.eclipse.rdf4j.common.text.StringUtil
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl.SimpleLiteral
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

object GraphDBEventConverter {

    private val log = LoggerFactory.getLogger(GraphDBEventConverter::class.java)

    fun parseRDFToEvents(rdfFullData: String): List<Event> {
        val model = parseRDFToModel(rdfFullData)

        val eventIds = eventIdsFromModel(model)
        require(eventIds.isNotEmpty()) { "No events found in RDF data. " }.also { log.debug("No events found in RDF data. ") }

        return eventIds.map {
            eventFromModel(model, it)
        }
    }

    // for GraphDBCordaService
    fun parseRDFtoCountries(rdfFullData: String): List<String> {
        val model = parseRDFToModel(rdfFullData)
        val eventIDs = eventIdsFromModel(model)
        val eventIdentifier = eventIDs.single().substringAfter("-")
        val mappedEventsToCountries = parseModelToMapEventsCountry(model, eventIDs)
        return mappedEventsToCountries[eventIdentifier]!!

    }

    // for quicker tests in GraphDBTripTests
    fun parseRDFtoMapEventsCountry(rdfFullData: String): Map<String, List<String>> {
        val model = parseRDFToModel(rdfFullData)
        val eventIDs = eventIdsFromModel(model)
        return parseModelToMapEventsCountry(model, eventIDs)
    }

    private fun parseModelToMapEventsCountry(model: Model, eventIDs: List<String>): Map<String, List<String>> {
        val countriesFromModel = mutableMapOf<String, List<String>>()

        for (eventID in eventIDs) {
            val locationsFromModelForEvent = locationsFromModel(model, eventID)
            val mappedLocations = locationsFromModelForEvent.flatMap { countryFromModel(model, it) }
            countriesFromModel[eventID.substringAfter("-")] = mappedLocations.map { StringUtil.trimDoubleQuotes(it) }
        }

        return countriesFromModel
    }

    fun parseRDFToEventIDs(rdfFullData: String): List<String> {
        val model = parseRDFToModel(rdfFullData)

        val eventIds = eventIdsFromModel(model).map { it.toLowerCase().substringAfter("event-") }
        require(eventIds.isNotEmpty()) { "No events found in RDF data. " }.also { log.debug("No events found in RDF data. ") }

        return eventIds
    }

    fun eventFromModel(
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
            timestamps = setOf(timestampFromModel(model, eventId)),
            ecmruri = "ecmruri", // TODO?
            milestone = milestoneFromModel(model, eventId),
            businessTransaction = businessTransactionIdFromModel(model, eventId),
            fullEvent = fullEventFromModel(model, eventId),
            labels = labelFromModel(model, eventId)
        )
    }

    private fun labelFromModel(model: Model, eventId: String): Set<String> {
        val factory = SimpleValueFactory.getInstance()
        val labels = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("http://www.w3.org/2000/01/rdf-schema#label"),
            null
        )
        return labels.objects().mapTo(HashSet<String>()) { StringUtil.trimDoubleQuotes(it.toString()) }
    }

    private fun businessTransactionIdFromModel(model: Model, eventId: String): String {
        val factory = SimpleValueFactory.getInstance()
        val businessTransactions = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#involvesBusinessTransaction"),
            null
        )
        require(businessTransactions.size in 0..1) { "Found multiple businesstransactions for event $eventId" }.also {
            log.debug("Found multiple" +
                "businessTransactions for event $eventId")
        }
        return if (businessTransactions.size == 0) "" else businessTransactions.first().`object`.toString().substringAfter("-").replace("\"", "")
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

    private fun countryFromModel(model: Model, physicalInfrastructureName: String): List<String> {
        val factory = SimpleValueFactory.getInstance()
        val cities = model.filter(
            factory.createIRI("http://example.com/base#PhysicalInfrastructure-$physicalInfrastructureName"),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#countryName"),
            null
        )

        return cities.objects().map { it.toString().substringAfter("-") }
    }

    private fun milestoneFromModel(model: Model, eventId: String): Milestone {
        val factory = SimpleValueFactory.getInstance()
        val milestones = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasMilestone"),
            null
        )

        require(milestones.size == 1) { "Found multiple milestones for event $eventId" }.also { log.debug("Found multiple milestones for event $eventId") }
        val milestone = milestones.objects().first().toString()
        return if (milestone == "https://ontology.tno.nl/logistics/federated/Event#End") Milestone.END else Milestone.START
    }

    private fun fullEventFromModel(model: Model, eventId: String): String {
        return eventId // TODO
    }

    private fun uuidFromModel(fullString: String): UUID {
        val uuidAsString = fullString.substringAfter("-")
        return UUID.fromString(uuidAsString)
    }

    private fun timestampFromModel(
        model: Model,
        eventId: String
    ): Timestamp {
        val factory = SimpleValueFactory.getInstance()
        val timestampType = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasDateTimeType"),
            null
        ).objects()
        require(timestampType.size == 1) { "Found multiple timestamptypes for event $eventId" }.also { log.debug("Found multiple timestamptypes for event $eventId") }
        val eventType = when (timestampType.first().toString().substringAfter("#").toLowerCase()) {
            "actual" -> EventType.ACTUAL
            "estimated" -> EventType.ESTIMATED
            "planned" -> EventType.PLANNED
            else -> throw IllegalArgumentException("Unknown eventtype found for event $eventId. Found ${timestampType.first()}.")
        }

        val timestamps = model.filter(
            factory.createIRI(eventId),
            factory.createIRI("https://ontology.tno.nl/logistics/federated/Event#hasTimestamp"),
            null
        ).objects()
        require(timestamps.size == 1) { "Found multiple timestamps for event $eventId" }.also { log.debug("Found multiple timestamps for event $eventId") }
        val timestamp = timestamps.first() as SimpleLiteral
        val formatter = if (timestamp.toString().length == 71) SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX") else SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        val eventDate = formatter.parse(timestamp.label)
        return Timestamp(uuidFromModel(eventId).toString(), eventDate, eventType)
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
        val typedDigitalTwins = digitalTwinIds.map { it to digitalTwinTypeFromId(model, it) }.toMap()

        return mapOf(
            PhysicalObject.GOOD to typedDigitalTwins.filterValues { it == PhysicalObject.GOOD }.keys,
            PhysicalObject.TRANSPORTMEAN to typedDigitalTwins.filterValues { it == PhysicalObject.TRANSPORTMEAN }.keys,
            PhysicalObject.LOCATION to typedDigitalTwins.filterValues { it == PhysicalObject.LOCATION }.keys,
            PhysicalObject.OTHER to typedDigitalTwins.filterValues { it == PhysicalObject.OTHER }.keys,
            PhysicalObject.CARGO to typedDigitalTwins.filterValues { it == PhysicalObject.CARGO }.keys)
    }

    private fun digitalTwinTypeFromId(model: Model, id: String): PhysicalObject {
        val factory = SimpleValueFactory.getInstance()
        val digitalTwins = model.filter(
            factory.createIRI(id),
            factory.createIRI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            null
        )

        val objectTypes = digitalTwins.objects().map { it.toString() }

        return if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#TransportMeans")) PhysicalObject.TRANSPORTMEAN
        else if (objectTypes.any { it in listOf("https://ontology.tno.nl/logistics/federated/DigitalTwin#Goods", "https://ontology.tno.nl/logistics/federated/DigitalTwin#Equipment") }) PhysicalObject.GOOD
        else if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#Location")) PhysicalObject.LOCATION
        else if (objectTypes.contains("https://ontology.tno.nl/logistics/federated/DigitalTwin#Cargo")) PhysicalObject.CARGO
        else PhysicalObject.OTHER
    }

    fun eventIdsFromModel(model: Model): List<String> {
        val subjects = model.subjects().map { it.toString() }
        return subjects.filter { it.toLowerCase().contains("#event-") }
    }

    fun parseRDFToModel(rdfFullData: String): Model {
        val rdfParser = Rio.createParser(RDFFormat.TURTLE)
        val model: Model = LinkedHashModel()

        rdfFullData.byteInputStream().use {
            rdfParser.setRDFHandler(StatementCollector(model))
            rdfParser.parse(rdfFullData.byteInputStream())
        }

        return model
    }
}