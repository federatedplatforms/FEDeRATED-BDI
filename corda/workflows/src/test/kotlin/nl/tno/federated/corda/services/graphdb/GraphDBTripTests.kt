package nl.tno.federated.corda.services.graphdb

import nl.tno.federated.corda.services.TTLRandomGenerator
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GraphDBTripTests : GraphDBTestContainersSupport() {
    companion object {
        private val generator = TTLRandomGenerator()
        private val generatedTripTTL = generator.generateTripEvents()
    }

    @Before
    fun before() {
        graphDBService.insertEvent(generatedTripTTL.constructedTTL, false)
    }

    @Test
    fun `Create trip, check if country of each event is SPARQL-ed correctly`() {
        val eventsInTrip = generatedTripTTL.eventsIdentifiers
        val eventsAtCountries = generatedTripTTL.eventsAtCountries

        for (eventIdentifier in eventsInTrip) {
            val country = eventsAtCountries[eventIdentifier]!!
            val countrySparqlResult = graphDBService.queryCountryGivenEventId(eventIdentifier)
            val countryFromSparql = graphDBService.unpackCountriesFromSPARQLresult(countrySparqlResult)
            assertTrue("Country of event $eventIdentifier was not correctly queried: $country", country == countryFromSparql.single())
        }
    }

    @Test
    fun `Extract country with parser, validate with SPARQL`() {
        val eventsAtCountries = generatedTripTTL.eventsAtCountries
        val modelMapEventsCountry = GraphDBEventConverter.parseRDFtoMapEventsCountry(generatedTripTTL.constructedTTL)

        for (eventIdentifier in eventsAtCountries.keys) {
            assertTrue("Country ${eventsAtCountries[eventIdentifier]!!} of event id $eventIdentifier was not correctly parsed",
                eventsAtCountries[eventIdentifier]!! in modelMapEventsCountry[eventIdentifier]!!)
        }
    }
}