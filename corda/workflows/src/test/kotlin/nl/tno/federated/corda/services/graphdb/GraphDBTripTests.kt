package nl.tno.federated.corda.services.graphdb

import nl.tno.federated.corda.services.TTLRandomGenerator
import org.junit.Test

class GraphDBTripTests : GraphDBTestContainersSupport() {
    companion object {
        private val generator = TTLRandomGenerator()
    }

    @Test
    fun `Query something out of consignment`() {

    }
}