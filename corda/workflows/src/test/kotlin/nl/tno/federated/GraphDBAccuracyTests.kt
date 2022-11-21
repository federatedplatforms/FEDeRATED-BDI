package nl.tno.federated

import nl.tno.federated.services.GraphDBService
import nl.tno.federated.services.TTLRandomGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import java.util.concurrent.TimeUnit

class GraphDBAccuracyTests : GraphDBTestContainersSupport() {

    companion object {
        private val graphdb = GraphDBService()

        @JvmStatic
        @BeforeClass
        fun setup() {
            // Override database.properties with docker properties
            System.setProperty("triplestore.host", graphDB.host)
            System.setProperty("triplestore.port", graphDB.exposedPorts?.firstOrNull()?.toString() ?: "7200")

            // 1. Create repositories
            graphdb.createRemoteRepositoryFromConfig("bdi-repository-config.ttl")
            graphdb.createRemoteRepositoryFromConfig("private-repository-config.ttl")

            // 2. Import graph files
            graphdb.importGraphFile("federated-shacl.zip", "http://rdf4j.org/schema/rdf4j#SHACLShapeGraph")

            graphdb.importGraphFile("ontologies.zip", "")

            await().atMost(10, TimeUnit.SECONDS).until { graphdb.areGraphFilesImported() }
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
            val sparqlQuery = graphdb.queryEventById(eventIdentifier)

            // expect the answer to the question "is the query result empty?" to be TRUE
            assertTrue("Query made illegal results", graphdb.isQueryResultEmpty(sparqlQuery))
        }

        assertTrue("Constructed TTL is incorrect", graphdb.insertEvent(constructedTTL, false))

        // 3. check if all items required for events are correctly saved (legalPerson, businessTransaction and equipmentUsed)

        val businessTransactionSparqlQuery = graphdb.queryEventComponent(businessTransaction)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Business transaction incorrectly saved", graphdb.isQueryResultEmpty(businessTransactionSparqlQuery))

        val legalPersonSparqlQuery = graphdb.queryEventComponent(legalPerson)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Legal person incorrectly saved", graphdb.isQueryResultEmpty(legalPersonSparqlQuery))

        val equipmentSparqlQuery = graphdb.queryEventComponent(equipmentUsed)

        // expect the answer to the question "is the query for business transaction result empty?" to be FALSE
        assertFalse("Equipment used incorrectly saved", graphdb.isQueryResultEmpty(equipmentSparqlQuery))

        // 4. query for all 4 events in part, they should all be there
        for (i in eventsIdentifiers.indices) {
            val transportMeansIdentifierSparqlQuery = graphdb.queryEventComponent(digitalTwinTransportMeans[i])

            assertFalse("Transport mean with identifier ${digitalTwinTransportMeans[i]} incorrectly inserted",
                graphdb.isQueryResultEmpty(transportMeansIdentifierSparqlQuery))

            val eventIdentifierSparqlQuery = graphdb.queryEventById(eventsIdentifiers[i])

            // expect the answer to the question "is the query for event identifier result empty?" to be FALSE
            assertFalse("Event with id ${eventsIdentifiers[i]} incorrectly inserted", graphdb.isQueryResultEmpty(eventIdentifierSparqlQuery))

            val allEventPropertiesSparqlQuery = graphdb.queryAllEventPropertiesById(eventsIdentifiers[i])

            assertTrue("Event with id ${eventsIdentifiers[i]} inaccurately saved",
                graphdb.areEventComponentsAccurate(allEventPropertiesSparqlQuery, businessTransaction,
                    digitalTwinTransportMeans[i], equipmentUsed))
        }

    }

}