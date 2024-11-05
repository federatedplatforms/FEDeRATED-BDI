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
    val useAuthentication: Boolean = false,
    val tokenURL: String? = null,
    val refreshURL: String? = null,
    val aud: String? = null
) {
    fun toWebhook() = Webhook(clientId, eventType, URL(callbackURL),useAuthentication, URL(tokenURL), URL(refreshURL), aud)
}