package nl.tno.federated.api.dtos

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class QueryAndOrganization(
        val query: String,
        val organization : String
)
