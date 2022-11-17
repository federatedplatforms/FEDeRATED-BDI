package nl.tno.federated.webserver.dtos

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class NewEvent(
        val fullEvent: String,
        val countriesInvolved : Set<String>
)
