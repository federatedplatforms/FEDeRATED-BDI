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
            val pairs = eventDestination.split(",")
                .map { it.trim() }
                .associate { it.split("=").let { keyValue -> keyValue[0].uppercase() to keyValue[1] } }

            if (listOf(ORGANISATION, COUNTRY, LOCALITY).any { it !in pairs }) {
                throw InvalidEventDestinationFormat("Invalid format for: $eventDestination, expected format: O=Cargobase,L=Dusseldorf,C=DE")
            }

            val cordaX500Name = CordaX500Name(
                pairs[COMMON_NAME],
                pairs[ORGANISATION_UNIT],
                pairs[ORGANISATION]!!,
                pairs[LOCALITY]!!,
                pairs[STATE],
                pairs[COUNTRY]!!
            )

            return CordaEventDestination(cordaX500Name)
        }

        private const val ORGANISATION = "O"
        private const val ORGANISATION_UNIT = "OU"
        private const val LOCALITY = "L"
        private const val STATE = "S"
        private const val COUNTRY = "C"
        private const val COMMON_NAME = "CN"
    }
}