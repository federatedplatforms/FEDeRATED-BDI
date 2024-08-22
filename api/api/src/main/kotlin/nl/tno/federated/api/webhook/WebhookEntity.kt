package nl.tno.federated.api.webhook

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.net.URL

@Table("WEBHOOK")
class WebhookEntity(

    @Id
    var id: Long? = null,
    @Column
    val clientId: String,
    val eventType: String,
    val callbackURL: String,
    val tokenURL: String? = null,
    val extraVariables: String? = null // json format key/value pairs  e.g. "{"aud":"value", "iss":"value"}"
) {
    fun toWebhook() = Webhook(clientId, eventType, URL(callbackURL), URL(tokenURL), extraVariables)
}