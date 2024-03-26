package nl.tno.federated.api.webhook

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/webhooks")
class WebhookController(private val webhookService: WebhookService) {

    @GetMapping
    fun getWebhooks(): List<Webhook> {
        return webhookService.getWebhooks()
    }

    @PostMapping
    fun registerWebhook(@RequestBody(required = true) registration: Webhook): Webhook {
        return webhookService.register(registration)
    }

    @DeleteMapping("/{client_id}")
    fun unregisterWebhook(@PathVariable("client_id") clientId: String): ResponseEntity<Void> {
        return if (webhookService.unregister(clientId)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
    }
}