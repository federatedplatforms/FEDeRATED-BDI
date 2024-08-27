package nl.tno.federated.api.webhook

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.net.URL
import java.nio.charset.Charset
import java.security.KeyStore

class WebhookTest {

    private val keyStorePassword = "password"
    private val keyStoreLocation = "node1.p12"
    private val keyStoreAlias = "node1"
    private val event = ClassPathResource("test-data/MinimalEvent.json").getContentAsString(Charset.defaultCharset())
    private val eventType = "federated.events.minimal.v1"
    private val tokenURL = URL("https://api-test.ntp.gov.sg//oauth/v1/token")
    private val callBackURL = URL("https://call.back/to/me")
    private val webhookHttpClient = mockk<WebhookHttpClient>()
    private val webhook = Webhook("clientId",eventType, callBackURL,tokenURL,"{'aud':'NTP'}")


    @Test
    fun getPrivateKeyFromKeyStore() {
        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load( ClassPathResource(keyStoreLocation).inputStream, keyStorePassword.toCharArray())
        keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray())

    }

    @Test
    fun testAcquireAccessToken() {

        val keyStore = KeyStore.getInstance("PKCS12")
        keyStore.load( ClassPathResource(keyStoreLocation).inputStream, keyStorePassword.toCharArray())
        val privKey = keyStore.getKey(keyStoreAlias, keyStorePassword.toCharArray())
        webhookHttpClient.getAccessToken(webhook,"NTP",privKey)
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