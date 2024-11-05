package nl.tno.federated.api.webhook

import nl.tno.federated.api.webhook.Token.AccessToken
import nl.tno.federated.api.webhook.Token.AcquireJwtException
import nl.tno.federated.api.webhook.Token.JwtHelper
import nl.tno.federated.api.webhook.Token.TokenHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI


@Service
class WebhookHttpClient () {

    var tokenRepo = mutableMapOf<String, AccessToken>()
    val jwtHelper = JwtHelper
    val tokenHelper = TokenHelper

    fun send(event: GenericEvent<*>, webhook: Webhook) {
        log.info("Sending event: {} with UUID: {} to: {}", event.eventType, event.eventUUID, webhook.callbackURL)
        try {
            if (webhook.tokenURL != null) {
                try {
                    val token: AccessToken? = tokenRepo.get(webhook.tokenURL.toString())?: tokenHelper.getAccessToken(webhook, jwtHelper)
                    if (JwtHelper.isTokenExpired(token!!.token)) {
                        tokenHelper.renewAccessToken(webhook, jwtHelper,token)
                    }
                } catch (e: Exception) {
                    log.warn("Unable to notify Webhook: {}, for event with UUID: {},Unable to acquire accessToken", webhook, event.eventUUID, e.message)
                    throw AcquireJwtException(e.message)
                }
            }

            restClient.post()
                .uri(webhook.callbackURL.toString())
                .contentType(APPLICATION_JSON)
                .headers {
                    it.location = URI("/api/events/${event.eventUUID}")
                    tokenRepo.get(webhook.tokenURL.toString())?.let { token-> it.setBearerAuth(token.token) }
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