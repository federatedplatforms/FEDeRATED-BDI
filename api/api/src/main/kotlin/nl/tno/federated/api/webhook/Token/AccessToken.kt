package nl.tno.federated.api.webhook.Token

data class AccessToken (
    var token: String,
    val tokenType: String,
    val refreshToken: String
)