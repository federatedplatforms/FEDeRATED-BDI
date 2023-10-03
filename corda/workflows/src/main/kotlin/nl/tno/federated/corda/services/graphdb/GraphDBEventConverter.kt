package nl.tno.federated.corda.services.graphdb

import org.eclipse.rdf4j.common.text.StringUtil
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.slf4j.LoggerFactory

object GraphDBEventConverter {

    private val log = LoggerFactory.getLogger(GraphDBEventConverter::class.java)

    // for GraphDBCordaService
    fun getCountryCodesFromRDFEventData(rdfFullData: String): List<String> {
        val model = parseRDFToModel(rdfFullData)
        val eventIDs = eventIdsFromModel(model)
        val eventIdentifier = eventIDs.single().substringAfter("-")
        val mappedEventsToCountries = parseModelToMapEventsCountry(model, eventIDs)
        return mappedEventsToCountries[eventIdentifier]!!
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