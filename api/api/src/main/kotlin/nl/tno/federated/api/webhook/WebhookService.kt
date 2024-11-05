package nl.tno.federated.api.webhook

import com.fasterxml.jackson.annotation.JsonIgnore
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

data class GenericEvent<T>(val eventType: String, @JsonIgnore val eventData: T, val eventUUID: String)

@Service
class WebhookService(
    private val webhookHttpClient: WebhookHttpClient,
    private val webhookRepository: WebhookRepository

) {

    /**
     * This method is being invoked whenever new GenericEvent's are published by the ApplicationEventPublisher
     */
    @EventListener
    fun handleEvent(event: GenericEvent<Any>) {
        log.info("Event of type: {} with UUID: {} received for publication...", event.eventType, event.eventUUID)
        val filter = getWebhooks().filter { it.eventType == event.eventType }
        log.info("{} webhooks registered for eventType: {}", filter.size, event.eventType)
        filter.forEach { webhookHttpClient.send(event, it) }
    }

    fun getWebhooks(): List<Webhook> {
        return webhookRepository.findAll().map { it.toWebhook() }
    }

    fun register(w: Webhook): Webhook {
        val save = webhookRepository.save(WebhookEntity(clientId = w.clientId, eventType = w.eventType, callbackURL = w.callbackURL.toString(), tokenURL = w.tokenURL?.toString(),refreshURL = w.tokenURL?.toString() , aud= w.aud,  id = null))
        log.info("New Webhook saved with id: {}", save.id)
        return save.toWebhook()
    }

    fun unregister(clientId: String): Boolean {
        val webhooks = webhookRepository.findByClientId(clientId)
        if(!webhooks.isEmpty()) {
            webhooks.forEach {
                webhookRepository.delete(it)
            }
            return true
        }
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(WebhookService::class.java)
    }
}