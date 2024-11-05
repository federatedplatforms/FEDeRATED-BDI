package nl.tno.federated.api.webhook.Token

import nl.tno.federated.api.webhook.Webhook
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.client.RestClient

object TokenHelper {
    fun getAccessToken(webhook: Webhook, jwtHelper: JwtHelper): AccessToken? {

        val jwt = jwtHelper.createJWT(webhook.clientId, webhook.aud!!)
        val restClient = RestClient.create()

        return restClient.post()
            .uri(webhook.tokenURL.toString())
            .contentType(APPLICATION_JSON)
            .headers {
                it.set("clientid", webhook.clientId)
                it.setBearerAuth(jwt)
            }
            .accept(APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                throw TokenException("Unable to acquire access token")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                throw TokenException("Unable to acquire access token")
            }
            .body(AccessToken::class.java)
    }

    fun renewAccessToken(webhook: Webhook, jwtHelper: JwtHelper, accessToken: AccessToken): AccessToken? {

        val restClient = RestClient.create()

        if (webhook.refreshURL != null) {
            val data = "{ \"refreshToken\"': \"${accessToken.refreshToken}\", \"grantType\": \"refreshToken\" }"
            val refreshToken = restClient.post()
                .uri(webhook.refreshURL.toString())
                .contentType(APPLICATION_JSON)
                .headers {
                    it.set("clientid", webhook.clientId)
                    it.setBearerAuth(accessToken.token)
                }
                .body(data)
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                    throw TokenException("Unable to`refresh access token: ${response.getStatusCode()}")
                }
                .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                    throw TokenException("Unable to refresh access token: ${response.getStatusCode()}")
                }
                .body(RefreshToken::class.java)
            accessToken.token = refreshToken!!.token
            return accessToken
        } else {
            return getAccessToken(webhook,jwtHelper)
        }
    }

}