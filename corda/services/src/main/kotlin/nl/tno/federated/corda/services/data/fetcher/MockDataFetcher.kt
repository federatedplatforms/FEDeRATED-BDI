package nl.tno.federated.corda.services.data.fetcher

class MockDataFetcher : DataFetcher {

    override fun fetch(input: String): String {
        return input
    }
}