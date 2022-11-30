package nl.tno.federated.ishare.utils

import nl.tno.federated.ishare.config.ISHAREConfig
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.ssl.SSLContexts

class ISHAREHTTPClient(private val ishareConfig: ISHAREConfig) {

    private val httpClient by lazy {
        HttpClientBuilder
            .create()
            .setSSLSocketFactory(
                SSLConnectionSocketFactory(
                    SSLContexts
                        .custom()
                        .loadTrustMaterial(null) { _, _ -> true }
                        .build(),
                    NoopHostnameVerifier.INSTANCE
                )
            )
            .setMaxConnPerRoute(30) // TODO make configurable using ishareConfig
            .setMaxConnTotal(120) // TODO make configurable using ishareConfig
            .build()
    }

    fun sendRequest(request: HttpUriRequest, accessToken: String? = null): HttpResponse {
        if(accessToken != null)  request.setHeader("Authorization", "Bearer $accessToken")
        return httpClient.execute(request)
    }
}