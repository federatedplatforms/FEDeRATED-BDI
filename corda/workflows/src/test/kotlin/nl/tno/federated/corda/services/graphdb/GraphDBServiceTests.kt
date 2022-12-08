package nl.tno.federated.corda.services.graphdb

import net.corda.core.internal.randomOrNull
import nl.tno.federated.corda.services.TTLRandomGenerator
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File


/**
 * This test only works with a running GraphDB instance (see: database.properties)
 */
class GraphDBServiceTests : GraphDBTestContainersSupport() {

    companion object {
        private val invalidSampleTTL = File("src/test/resources/SHACL_FAIL - TradelensEvents_ArrivalDeparture.ttl").readText()
        private val generatedTTL = TTLRandomGenerator().generateRandomEvents()
        private val validSampleTtl = generatedTTL.constructedTTL
        private val eventPool = generatedTTL.eventIdentifiers
    }

    @Before
    fun before() {
        graphDBService.insertEvent(validSampleTtl, false)
    }

    @Test
    fun `Query everything`() {
        val everythingQueryResult = graphDBService.queryEventIds()
        assertFalse(graphDBService.isQueryResultEmpty(everythingQueryResult))
    }

    @Test
    fun `Query event by ID`() {
        val randomEvent = eventPool.randomOrNull()!!
        val result = graphDBService.queryEventById(randomEvent)
        assertFalse(graphDBService.isQueryResultEmpty(result))
    }

    @Test
    fun `Insert new event`() {
        val successfulInsertion = graphDBService.insertEvent(TTLRandomGenerator().generateRandomEvents(1).constructedTTL, false)
        assert(successfulInsertion)
    }

    @Test
    @Ignore("TODO fix")
    fun `Insert invalid event`() {
        val successfulInsertion = graphDBService.insertEvent(invalidSampleTTL, false)
        assert(!successfulInsertion)
    }
}