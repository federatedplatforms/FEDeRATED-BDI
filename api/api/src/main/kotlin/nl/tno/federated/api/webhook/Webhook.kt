package nl.tno.federated.api.webhook

import java.net.URL

data class Webhook(
    val clientId: String,
    val eventType: String,
    val callbackURL: URL,
    val useAuthentication: Boolean,
    val tokenURL: URL?,
    val refreshURL: URL?,
    val aud: String?
)