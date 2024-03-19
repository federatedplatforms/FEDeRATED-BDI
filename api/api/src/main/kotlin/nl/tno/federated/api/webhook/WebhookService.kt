package nl.tno.federated.api.webhook

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

data class GenericEvent<T>(val eventType: String, @JsonIgnore val eventData: T, val eventUUID: String)

@Service
class WebhookService {

    @EventListener
    fun handleEvent(event: GenericEvent<Any>) {
        log.info("Event of type: {} with UUID: {} received for publication...", event.eventType, event.eventUUID)
        val filter = webhooks.values.filter { it.eventType == event.eventType }
        log.info("{} webhooks registered for eventType: {}", filter.size, event.eventType)
        filter.forEach { notify(event, it) }
    }

    fun getWebhooks(): List<Webhook> {
        return webhooks.values.toList()
    }

    fun register(registration: Webhook) {
        webhooks[registration.clientId] = registration
    }

    fun unregister(clientId: String): Boolean {
        return webhooks.remove(clientId) != null
    }

    fun notify(event: GenericEvent<*>, webhook: Webhook) {
        log.info("Sending event: {} with UUID: {} to: {}", event.eventType, event.eventUUID, webhook.callbackURL)

        try {
            restClient.post()
                .uri(webhook.callbackURL.toString())
                .contentType(APPLICATION_JSON)
                .headers {
                    it.location = URI("/api/events/${event.eventUUID}")
                }
                .body(event)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                    log.warn("Sending event to callback: ${webhook.callbackURL} failed with ${response.statusCode}")
                }
                .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                    log.warn("Sending event to callback: ${webhook.callbackURL} failed with ${response.statusCode}")
                }
                .toBodilessEntity()
        } catch (e: Exception) {
            log.warn("Unable to notify Webhook: {}, for event with UUID: {}, error message: {}", webhook, event.eventUUID, e.message)
        }
    }

    companion object {
        private val restClient: RestClient = RestClient.create()
        private val webhooks = ConcurrentHashMap<String, Webhook>()
        private val log = LoggerFactory.getLogger(WebhookService::class.java)
    }
}