package nl.tno.federated.api.webhook

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nl.tno.federated.api.config.WebHookSecurityConfig
import nl.tno.federated.api.webhook.jwt.AcquireJwtException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.net.URI
import java.util.*
import nl.tno.federated.api.webhook.jwt.JwtHelper
import java.security.Key


@Service
class WebhookHttpClient (val keystoreConfig: WebHookSecurityConfig) {

    var tokenRepo = mutableMapOf<String, String>()
    val jwtHelper = JwtHelper

    fun send(event: GenericEvent<*>, webhook: Webhook) {
        log.info("Sending event: {} with UUID: {} to: {}", event.eventType, event.eventUUID, webhook.callbackURL)
        try {
            var jwtToken = ""
            if (webhook.tokenURL != null) {
                if (tokenRepo.containsKey(webhook.tokenURL.toString())) {
                    // check if token still valid
                    val tokenItems = ObjectMapper().readValue<MutableMap<String, String>>(tokenRepo.get(webhook.tokenURL.toString()).toString())
                    jwtToken = tokenItems.get("token")!!
                }
                val map = ObjectMapper().readValue<MutableMap<String, String>>(webhook.extraVariables!!)
                val privKey = keystoreConfig.privateKey()
                try {
                    if (jwtToken.length == 0 || jwtHelper.isTokenExpired(jwtToken)) {

                        val accessToken = getAccessToken(webhook, map.get("aud")!!, privKey)
                        this.tokenRepo.put(webhook.tokenURL.toString(), accessToken)
                    }

                } catch (e: Exception) {
                    log.warn("Unable to notify Webhook: {}, for event with UUID: {}, error message: {}", webhook, event.eventUUID, e.message)
                    throw AcquireJwtException(e.message)
                }
            }

            jwtToken = ObjectMapper().readValue<MutableMap<String, String>>(tokenRepo.get(webhook.tokenURL.toString()).toString()).get("token")!!

            restClient.post()
                .uri(webhook.callbackURL.toString())
                .contentType(APPLICATION_JSON)
                .headers {
                    it.location = URI("/api/events/${event.eventUUID}")
                    if (jwtToken.length > 0) it.setBearerAuth(jwtToken)
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

    fun getAccessToken(webhook: Webhook, audience: String, privateKey: Key): String {
        val requestToken = Jwts.builder()
            .setHeaderParam("alg", "RS256").setHeaderParam("typ", "JWT")
            .setIssuer(webhook.clientId)
            .setSubject(UUID.randomUUID().toString())
            .setAudience(audience)
            .setIssuedAt(Date())
            .signWith(privateKey, SignatureAlgorithm.RS256)
            .compact()

        return restClient.post()
            .uri(webhook.tokenURL.toString())
            .contentType(APPLICATION_JSON)
            .headers {
                it.set("clientid", webhook.clientId)
                it.setBearerAuth(requestToken)
            }
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                log.warn("Requesting token to tokenURL: ${webhook.tokenURL.toString()} failed with ${response.statusCode}")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                log.warn("Requesting token to tokenURL: ${webhook.tokenURL.toString()} failed with ${response.statusCode}")
            }
            .body(String.javaClass).toString()
    }

    companion object {
        private val restClient: RestClient = RestClient.create()
        private val log = LoggerFactory.getLogger(WebhookHttpClient::class.java)
    }
}