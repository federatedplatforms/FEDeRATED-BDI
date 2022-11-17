package nl.tno.federated.webserver.dtos

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class QueryAndOrganization(
        val query: String,
        val organization : String
)
