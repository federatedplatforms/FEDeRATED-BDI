package nl.tno.federated.ishare.utils

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts
import org.apache.http.ssl.TrustStrategy
import javax.net.ssl.SSLContext

class ISHAREHTTPClientUtil {

    private val httpClient by lazy {
        val acceptingTrustStrategy = TrustStrategy { _, _ -> true }
        val sslContext: SSLContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build()

        HttpClientBuilder
            .create()
            .setSSLSocketFactory(SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
            .setMaxConnPerRoute(20)
            .setMaxConnTotal(100)
            .build()
    }

    fun sendRequest(request: HttpUriRequest, accessToken: String): HttpResponse {
        request.setHeader("Authorization", "Bearer $accessToken")
        return sendRequest(request)
    }

    fun sendRequest(request: HttpUriRequest): HttpResponse {
        return httpClient.execute(request)
    }
}