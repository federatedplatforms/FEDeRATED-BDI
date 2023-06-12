package nl.tno.federated.corda.services.data.fetcher

import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class DataFetcherCordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    // Since we cant mock @CordaService classes, we need to have some way of overriding the internals.
    private val dataFetcher: DefaultDataFetcher by lazy { externalDataFetcher ?: DefaultDataFetcher() }
    private var externalDataFetcher: DefaultDataFetcher? = null

    fun init(dataFetcher: DefaultDataFetcher) {
        this.externalDataFetcher = dataFetcher
    }

    // Delegate all calls to the non Corda object.
    fun fetch(sparql: String) = dataFetcher.fetch(sparql)
}

fun FlowLogic<*>.dataFetcher() = serviceHub.cordaService(DataFetcherCordaService::class.java)