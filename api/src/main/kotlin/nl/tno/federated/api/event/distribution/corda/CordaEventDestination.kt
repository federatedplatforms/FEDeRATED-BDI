package nl.tno.federated.api.event.distribution.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.event.distribution.EventDestination
import nl.tno.federated.api.event.distribution.InvalidEventDestinationFormat

class CordaEventDestination(destination: CordaX500Name) : EventDestination<CordaX500Name>(destination) {

    companion object {
        fun parse(eventDestination: String): CordaEventDestination {
            val result = eventDestination.split("/")
            return when (val size = result.size) {
                3 -> CordaEventDestination(CordaX500Name(result[0], result[1], result[2]))
                4 -> CordaEventDestination(CordaX500Name(result[0], result[1], result[2], result[3]))
                else -> throw InvalidEventDestinationFormat("Invalid format for: ${eventDestination}, expected format: ORGANISATION/LOCALITY/COUNTRY")
            }
        }
    }
}