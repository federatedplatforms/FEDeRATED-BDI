package nl.tno.federated.corda.services.graphdb

import nl.tno.federated.corda.services.TTLRandomGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphDBAccuracyTests : GraphDBTestContainersSupport() {

    companion object {
        private val generator = TTLRandomGenerator()
    }

    // change value below to generate a diff number of events for the test
    private val numberOfEvents = 6

    @Test
    fun `Query events empty at first, insert events, query events inserted successfully`() {
        val (constructedTTL, eventsIdentifiers, legalPerson,
            businessTransaction, equipmentUsed, digitalTwinTransportMeans) =
            generator.generateRandomEvents(numberOfEvents)

        // 1. query for all 4 events in part, everything should be false
        for (eventIdentifier in eventsIdentifiers) {
            val sparqlQuery = graphDBService.queryEventById(eventIdentifier)

            // expect the answer to the question "is the query result empty?" to be TRUE
            assertTrue("Query made illegal results", graphDBService.isQueryResultEmpty(sparqlQuery))
        }

        assertTrue("Constructed TTL is incorrect", graphDBService.insertEvent(constructedTTL, false))

        // 3. check if all items required for events are correctly saved (legalPerson, businessTransaction and equipmentUsed)

        val businessTransactionSparqlQuery = graphDBService.queryEventComponent(businessTransaction)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Business transaction incorrectly saved", graphDBService.isQueryResultEmpty(businessTransactionSparqlQuery))

        val legalPersonSparqlQuery = graphDBService.queryEventComponent(legalPerson)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Legal person incorrectly saved", graphDBService.isQueryResultEmpty(legalPersonSparqlQuery))

        val equipmentSparqlQuery = graphDBService.queryEventComponent(equipmentUsed)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Equipment used incorrectly saved", graphDBService.isQueryResultEmpty(equipmentSparqlQuery))

        // 4. query for all 4 events in part, they should all be there
        for (i in eventsIdentifiers.indices) {
            val transportMeansIdentifierSparqlQuery = graphDBService.queryEventComponent(digitalTwinTransportMeans[i])

            assertFalse(
                "Transport mean with identifier ${digitalTwinTransportMeans[i]} incorrectly inserted",
                graphDBService.isQueryResultEmpty(transportMeansIdentifierSparqlQuery)
            )

            val eventIdentifierSparqlQuery = graphDBService.queryEventById(eventsIdentifiers[i])

            // expect the answer to the question "is the query for event identifier result empty?" to be FALSE
            assertFalse("Event with id ${eventsIdentifiers[i]} incorrectly inserted", graphDBService.isQueryResultEmpty(eventIdentifierSparqlQuery))

            val allEventPropertiesSparqlQuery = graphDBService.queryAllEventPropertiesById(eventsIdentifiers[i])

            assertTrue(
                "Event with id ${eventsIdentifiers[i]} inaccurately saved",
                graphDBService.areEventComponentsAccurate(
                    allEventPropertiesSparqlQuery, businessTransaction,
                    digitalTwinTransportMeans[i], equipmentUsed
                )
            )
        }

    }

}