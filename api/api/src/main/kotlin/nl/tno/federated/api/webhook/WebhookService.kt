package nl.tno.federated.api.webhook

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

data class GenericEvent<T>(val eventType: String, val eventData: T)

@Service
class WebhookService {

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
        println("Sending event: ${event} to: ${webhook}")
    }

    @EventListener
    fun handleSuccessful(event: GenericEvent<Any>) {
        log.info("Event received for publication...")
        val filter = webhooks.values.filter { it.eventType == event.eventType }
        log.info("{} webhooks registered for eventType: {}", filter.size, event.eventType)
        filter.forEach { notify(event, it) }
    }

    companion object {
        private val webhooks = ConcurrentHashMap<String, WebHookRegistration>()
        private val log = LoggerFactory.getLogger(WebhookService::class.java)

    }
}