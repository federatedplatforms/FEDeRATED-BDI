package nl.tno.federated.corda.services.data.fetcher

import net.corda.core.flows.FlowLogic
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class DataFetcherCordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    init {
        serviceHub.cordappProvider.getAppContext().config
    }

    // Since we cant mock @CordaService classes, we need to have some way of overriding the internals.
    private val dataFetcher by lazy { externalDataFetcher ?: SPARQLDataFetcher() }
    private var externalDataFetcher: DataFetcher? = null

    fun init(dataFetcher: DataFetcher) {
        this.externalDataFetcher = dataFetcher
    }

    // Delegate all calls to the non Corda object.
    fun fetch(input: String) = dataFetcher.fetch(input)
}

fun FlowLogic<*>.dataFetcher() = serviceHub.cordaService(DataFetcherCordaService::class.java)