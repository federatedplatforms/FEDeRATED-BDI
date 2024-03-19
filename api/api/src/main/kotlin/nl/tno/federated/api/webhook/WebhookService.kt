package nl.tno.federated.api.webhook

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.util.concurrent.ConcurrentHashMap


data class GenericEvent<T>(val eventType: String, val eventData: T)

@Service
class WebhookService() {

    fun getWebhooks(): List<WebHookRegistration> {
        return webhooks.values.toList()
    }

    fun register(registration: WebHookRegistration) {
        webhooks[registration.clientId] = registration
    }

    fun unregister(clientId: String): Boolean {
        return webhooks.remove(clientId) != null
    }

    fun notify(event: GenericEvent<*>, webhook: WebHookRegistration) {
        log.info("Sending event: ${event.eventType} to: ${webhook.callbackURL}")

        restClient.post()
            .uri(webhook.callbackURL.toString())
            .contentType(APPLICATION_JSON)
            .body(event.eventData!!)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                log.warn("Sending event to callback: ${webhook.callbackURL} failed with ${response.statusCode}")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                log.warn("Sending event to callback: ${webhook.callbackURL} failed with ${response.statusCode}")
            }
            .toBodilessEntity()
    }

    @EventListener
    fun handleSuccessful(event: GenericEvent<Any>) {
        log.info("Event received for publication...")
        val filter = webhooks.values.filter { it.eventType == event.eventType }
        log.info("{} webhooks registered for eventType: {}", filter.size, event.eventType)
        filter.forEach { notify(event, it) }
    }

    companion object {
        private val restClient: RestClient = RestClient.create()
        private val webhooks = ConcurrentHashMap<String, WebHookRegistration>()
        private val log = LoggerFactory.getLogger(WebhookService::class.java)

    }
}