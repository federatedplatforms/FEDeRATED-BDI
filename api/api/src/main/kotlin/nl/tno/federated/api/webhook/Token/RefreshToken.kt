package nl.tno.federated.api.webhook.Token

data class RefreshToken(
        val token: String,
        val tokenType: String
)