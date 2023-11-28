package nl.tno.federated.api.webhook

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

class GenericEvent<T>(val eventType: String, val event: T) {
    companion object
}

@Service
class WebhookService(private val applicationEventPublisher: ApplicationEventPublisher) {

    fun getWebhooks(): List<WebHookRegistration> {
        return webhooks.values.toList()
    }

    fun register(registration: WebHookRegistration) {
        webhooks[registration.clientId] = registration
    }

    fun unregister(clientId: String): Boolean {
        return webhooks.remove(clientId) != null
    }

    fun publishNewEvent(event: GenericEvent<*>) {
        applicationEventPublisher.publishEvent(event)
    }

    fun notify(event: GenericEvent<*>, webhook: WebHookRegistration) {
        println("Sending event: ${event} to: ${webhook}")
    }

    @EventListener
    fun handleSuccessful(event: GenericEvent<String>) {
        for (webhook in webhooks) {
            val filter = webhooks.values.filter { it.eventType == event.eventType }
            filter.forEach { notify(event, it) }
        }
    }

    companion object {
        private val webhooks = ConcurrentHashMap<String, WebHookRegistration>()
    }
}