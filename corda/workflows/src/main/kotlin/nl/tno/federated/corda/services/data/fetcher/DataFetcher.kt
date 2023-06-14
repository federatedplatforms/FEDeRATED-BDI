package nl.tno.federated.corda.services.data.fetcher

interface DataFetcher {
    fun fetch(input: String): String?
}