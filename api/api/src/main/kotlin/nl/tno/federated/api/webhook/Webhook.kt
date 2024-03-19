package nl.tno.federated.api.webhook

import java.net.URL

data class Webhook(
    val clientId: String,
    val eventType: String,
    val callbackURL: URL
)