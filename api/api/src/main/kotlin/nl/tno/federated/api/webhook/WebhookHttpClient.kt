package nl.tno.federated.api.webhook

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI

@Service
class WebhookHttpClient {

    fun send(event: GenericEvent<*>, webhook: Webhook) {
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
        private val log = LoggerFactory.getLogger(WebhookHttpClient::class.java)
    }
}