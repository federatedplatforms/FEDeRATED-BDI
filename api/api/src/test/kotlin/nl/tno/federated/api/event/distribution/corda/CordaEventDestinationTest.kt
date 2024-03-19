package nl.tno.federated.api.event.distribution.corda

import nl.tno.federated.api.event.distribution.InvalidEventDestinationFormat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class CordaEventDestinationTest {

    @Test
    fun parseDestinationMinimumValues() {
        val parse = CordaEventDestination.parse("O=TNO,C=NL,L=Den Haag")
        assertEquals("TNO", parse.destination.organisation)
        assertEquals("NL", parse.destination.country)
        assertEquals("Den Haag", parse.destination.locality)
    }

    @Test
    fun parseDestinationAllValues() {
        val parse = CordaEventDestination.parse("CN=tno.nl,OU=DE,O=TNO,C=NL,S=Zuid Holland,L=Den Haag")
        assertEquals("TNO", parse.destination.organisation)
        assertEquals("NL", parse.destination.country)
        assertEquals("Den Haag", parse.destination.locality)
    }

    @Test
    fun parseDestinationMissingValue() {
        val thrown = Assertions.assertThrows(InvalidEventDestinationFormat::class.java) {
            CordaEventDestination.parse("O=TNO,L=Den Haag")
        }

        assertNotNull(thrown.message);
    }

    @Test
    fun parseDestinationInvalidValue() {
        val thrown = Assertions.assertThrows(InvalidEventDestinationFormat::class.java) {
            CordaEventDestination.parse("org=TNO,C=NL,L=Den Haag")
        }

        assertNotNull(thrown.message);
    }
}