package nl.tno.federated.corda.services.data.fetcher

import org.junit.Ignore
import org.junit.Test
import java.util.*

class CodognottoHTTPDataFetcherTest {

    val properties = Properties().apply {
        put("get.endpoint.url", "https://dummy.url")
        put("rml.endpoint.url", "https://dummy.url")
    }

    @Test
    @Ignore("Need to setup a mock http client ")
    fun testCodognottoHTTPDataFetcher() {
        val fetcher = CodognottoHTTPDataFetcher(properties = properties)
        val fetch = fetcher.fetch()
        // assert value
    }
}