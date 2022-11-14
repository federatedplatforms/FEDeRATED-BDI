package nl.tno.federated

import nl.tno.federated.services.GraphDBService
import nl.tno.federated.services.TTLRandomGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.io.File

class GraphDBAccuracyTests : GraphDBTestContainersSupport() {

    companion object {

        private val validSampleTtl = File("src/test/resources/correct-event.ttl").readText()

        @JvmStatic
        @BeforeClass
        fun setup() {
            // Override database.properties with docker properties
            System.setProperty("triplestore.host",  graphDB.host)
            System.setProperty("triplestore.port",  graphDB.exposedPorts?.firstOrNull()?.toString() ?: "7200")

            // 1. Create repositories
            GraphDBService.createRemoteRepositoryFromConfig("bdi-repository-config.ttl")
            GraphDBService.createRemoteRepositoryFromConfig("private-repository-config.ttl")
            // 2. Insert data
            GraphDBService.insertEvent(validSampleTtl, false)
        }
    }

    private val generator = TTLRandomGenerator()
    // change value below to generate a diff number of events for the test
    private val numberOfEvents = 6

    @Test
    fun `Query events empty at first, insert events, query events inserted successfully`() {
        val (constructedTTL, eventsIdentifiers, legalPerson,
                businessTransaction, equipmentUsed, digitalTwinTransportMeans) =
                generator.generateRandomEvents(numberOfEvents)

        // 1. query for all 4 events in part, everything should be false
        for (eventIdentifier in eventsIdentifiers) {
            val sparqlQuery = GraphDBService.queryEventById(eventIdentifier)

            // expect the answer to the question "is the query result empty?" to be TRUE
            assertTrue("Query made illegal results", GraphDBService.isQueryResultEmpty(sparqlQuery))
        }

        assertTrue("Constructed TTL is incorrect", GraphDBService.insertEvent(constructedTTL,false))

        // 3. check if all items required for events are correctly saved (legalPerson, businessTransaction and equipmentUsed)

        val businessTransactionSparqlQuery = GraphDBService.queryEventComponent(businessTransaction)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Business transaction incorrectly saved", GraphDBService.isQueryResultEmpty(businessTransactionSparqlQuery))

        val legalPersonSparqlQuery = GraphDBService.queryEventComponent(legalPerson)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Legal person incorrectly saved", GraphDBService.isQueryResultEmpty(legalPersonSparqlQuery))

        val equipmentSparqlQuery = GraphDBService.queryEventComponent(equipmentUsed)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Equipment used incorrectly saved", GraphDBService.isQueryResultEmpty(equipmentSparqlQuery))

        // 4. query for all 4 events in part, they should all be there
        for (i in eventsIdentifiers.indices) {
            val transportMeansIdentifierSparqlQuery = GraphDBService.queryEventComponent(digitalTwinTransportMeans[i])

            assertFalse("Transport mean with identifier ${digitalTwinTransportMeans[i]} incorrectly inserted",
                    GraphDBService.isQueryResultEmpty(transportMeansIdentifierSparqlQuery))

            val eventIdentifierSparqlQuery = GraphDBService.queryEventById(eventsIdentifiers[i])

            // expect the answer to the question "is the query for event identifier result empty?" to be FALSE
            assertFalse("Event with id ${eventsIdentifiers[i]} incorrectly inserted", GraphDBService.isQueryResultEmpty(eventIdentifierSparqlQuery))

            val allEventPropertiesSparqlQuery = GraphDBService.queryAllEventPropertiesById(eventsIdentifiers[i])

            assertTrue("Event with id ${eventsIdentifiers[i]} inaccurately saved",
                    GraphDBService.areEventComponentsAccurate(allEventPropertiesSparqlQuery, businessTransaction,
                            digitalTwinTransportMeans[i], equipmentUsed))
        }

    }

}