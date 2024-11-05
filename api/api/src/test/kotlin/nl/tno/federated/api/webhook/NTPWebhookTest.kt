package nl.tno.federated.api.webhook

import io.mockk.mockk
import nl.tno.federated.api.webhook.Token.AccessToken
import nl.tno.federated.api.webhook.Token.JwtHelper
import nl.tno.federated.api.webhook.Token.RefreshToken
import nl.tno.federated.api.webhook.Token.TokenException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import java.net.URL
import java.nio.charset.Charset
import kotlin.test.assertNotNull

class NTPWebhookTest {

    private val eventType = "federated.events.minimal.v1"
    private val tokenURL = URL("https://api-test.ntp.gov.sg//oauth/v1/token")
    private val refreshTokenURL = URL("https://api-test.ntp.gov.sg//oauth/v1/refreshToken")
    private val callBackURL = URL("https://call.back/to/me")
    private val clientId = "L7UQQHHOMK65YW4NEM3T"
    private val webhook = Webhook(clientId,eventType, callBackURL,true, tokenURL,refreshTokenURL,"NTP")

    @Test
    fun getPrivateKeyFromFileTest() {
        assertDoesNotThrow( {JwtHelper.readRSAPrivateKey(ClassPathResource("rsa2048/NTP_UAT_dev.pem").file) } )
    }

    @Test
    fun createJWT() {
        assertDoesNotThrow( {
            val privateKey = JwtHelper.readRSAPrivateKey(ClassPathResource("rsa2048/NTP_UAT_dev.pem").file)
            JwtHelper.createJWT(webhook.clientId,"NTP",privateKey) })
    }

    @Test
    fun testAcquireAccessToken() {
        val privateKey = JwtHelper.readRSAPrivateKey(ClassPathResource("rsa2048/NTP_UAT_dev.pem").file)
        val jwt = JwtHelper.createJWT(webhook.clientId, webhook.aud!!,privateKey)

        val restClient = RestClient.create()
        val token = restClient.post()
            .uri(webhook.tokenURL.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .headers {
                it.set("clientid", webhook.clientId)
                it.setBearerAuth(jwt)
            }
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->

            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                //WebhookHttpClient.log.warn("Requesting token to tokenURL: ${webhook.tokenURL.toString()} failed with ${response.statusCode}")
            }
            .body(AccessToken::class.java)
        assertNotNull(token)
    }

    @Test
    fun testRefreshAccessToken() {
        val privateKey = JwtHelper.readRSAPrivateKey(ClassPathResource("rsa2048/NTP_UAT_dev.pem").file)
        val jwt = JwtHelper.createJWT(webhook.clientId, webhook.aud!!,privateKey)

        val restClient = RestClient.create()
        val token = restClient.post()
            .uri(webhook.tokenURL.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .headers {
                it.set("clientid", webhook.clientId)
                it.setBearerAuth(jwt)
            }
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->
                //WebhookHttpClient.log.warn("Requesting token to tokenURL: ${webhook.tokenURL.toString()} failed with ${response.statusCode}")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                //WebhookHttpClient.log.warn("Requesting token to tokenURL: ${webhook.tokenURL.toString()} failed with ${response.statusCode}")
            }
            .body(AccessToken::class.java)

        val data = "{ \"refreshToken\"': \"${token!!.refreshToken}\", \"grantType\": \"refreshToken\" }"
        val refreshToken = restClient.post()
            .uri(webhook.refreshURL.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .headers {
                it.set("clientid", webhook.clientId)
                it.setBearerAuth(token.token)
            }
            .body(data)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) { _, response ->

                throw TokenException("Unable to`refresh access token: ${response.getStatusCode()}")
            }
            .onStatus(HttpStatusCode::is5xxServerError) { _, response ->
                throw TokenException("Unable to refresh access token: ${response.getStatusCode()}")
            }
            .body(RefreshToken::class.java)
        assertNotNull(refreshToken)
    }

    @Test
    fun registerWebhookWithoutToken () {

    }

    @Test
    fun registerWebHookWithToken() {

    }

    @Test
    fun triggerWebhooksWithoutToken() {

    }

    @Test
    fun triggerWebhooksWithToken() {

    }


}