package nl.tno.federated.ishare.model.token

data class ISHARETokenRequest(
    val grant_type: String,
    val scope: String,
    val client_id: String,
    val client_assertion_type: String,
    val client_assertion: String
)