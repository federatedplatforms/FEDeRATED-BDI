package nl.tno.federated.api.event.distribution.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.event.distribution.EventDestination
import nl.tno.federated.api.event.distribution.InvalidEventDestinationFormat

class CordaEventDestination(destination: CordaX500Name) : EventDestination<CordaX500Name>(destination) {

    companion object {
        /**
         * EventDestination minimal format: O=Cargobase,L=Dusseldorf,C=DE
         */
        fun parse(eventDestination: String): CordaEventDestination {
            val result = eventDestination.split(",")
            val pairs = mutableMapOf<String, String>()

            for(item in result) {
                val split = item.split("=")
                if(split.size == 2) {
                    pairs[split[0].trim().uppercase()] = split[1].trim()
                }
            }

            if(!pairs.containsKey(ORGANISATION) || !pairs.containsKey(COUNTRY) || !pairs.containsKey(LOCALITY) ) {
                throw InvalidEventDestinationFormat("Invalid format for: ${eventDestination}, expected format: O=Cargobase,L=Dusseldorf,C=DE")
            }

            return CordaEventDestination(CordaX500Name(pairs[COMMON_NAME],pairs[ORGANISATION_UNIT],pairs[ORGANISATION]!!, pairs[LOCALITY]!!, pairs[STATE], pairs[COUNTRY]!!))
        }

        const val ORGANISATION = "O"
        const val ORGANISATION_UNIT = "O"
        const val LOCALITY = "L"
        const val STATE = "S"
        const val COUNTRY = "C"
        const val COMMON_NAME = "CN"
    }
}