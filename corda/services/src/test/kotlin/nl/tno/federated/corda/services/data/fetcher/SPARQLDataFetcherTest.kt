package nl.tno.federated.corda.services.data.fetcher

import org.junit.Ignore
import org.junit.Test
import java.util.*

class SPARQLDataFetcherTest {

    val properties = Properties().apply {
        put("graphdb.sparql.url", "http://localhost:7200/repositories/federated")
    }

    @Test
    @Ignore("Need to setup a mock http client ")
    fun testSPARQLDataFetcher() {
        val fetcher = SPARQLDataFetcher(properties = properties)
        val fetch = fetcher.fetch()
        // assert value
    }

}