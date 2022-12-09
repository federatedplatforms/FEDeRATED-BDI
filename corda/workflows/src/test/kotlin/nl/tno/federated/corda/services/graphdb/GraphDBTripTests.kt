package nl.tno.federated.corda.services.graphdb

import nl.tno.federated.corda.services.TTLRandomGenerator
import org.junit.Assert
import org.junit.Test

class GraphDBTripTests : GraphDBTestContainersSupport() {
    companion object {
        private val generator = TTLRandomGenerator()
    }

    @Test
    fun `Create trip, check if country of each event is SPARQL-ed correctly`() {
        val generatedTripTTL = generator.generateTripEvents()
        val tripTTL = generatedTripTTL.constructedTTL
        graphDBService.insertEvent(tripTTL, false)
        val eventsInTrip = generatedTripTTL.eventsIdentifiers
        val eventsAtCities = generatedTripTTL.eventsAtCities
        val eventsAtCountries = generatedTripTTL.eventsAtCountries

        for (eventIdentifier in eventsInTrip) {
            val countries = eventsAtCountries[eventIdentifier]!!
            val countrySparqlResult = graphDBService.queryCountryGivenEventId(eventIdentifier)
            val countriesFromSparql = graphDBService.unpackCountriesFromSPARQLresult(countrySparqlResult)
            for (country in countries) {
                Assert.assertTrue("Country was not correctly queried: $country", country in countriesFromSparql)
            }
        }
    }

    @Test
    fun `Extract country with parser, validate with SPARQL`() {

    }
}