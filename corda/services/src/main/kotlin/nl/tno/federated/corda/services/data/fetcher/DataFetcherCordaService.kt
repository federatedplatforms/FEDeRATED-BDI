package nl.tno.federated.corda.services.data.fetcher

import net.corda.core.flows.FlowLogic
import net.corda.core.flows.HospitalizeFlowException
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import nl.tno.federated.corda.services.properties.PropertiesReader

@CordaService
class DataFetcherCordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    // Since we cant mock @CordaService classes, we need to have some way of overriding the internals.
    private var dataFetcher: DataFetcher

    init {
        val properties = PropertiesReader().readProperties("database.properties")

        dataFetcher = when (properties.getProperty("datafetcher.type")) {
            "sparql" -> SPARQLDataFetcher()
            else -> HTTPDataFetcher()
        }
    }

    // Delegate all calls to the non Corda object.
    fun fetch(deduplicationId: String, input: String): String {
        try {
            return dataFetcher.fetch(input)
        } catch (e: Exception) {
            throw HospitalizeFlowException("External API call failed", e)
        }
    }
}

fun FlowLogic<*>.dataFetcher(): DataFetcherCordaService = serviceHub.cordaService(DataFetcherCordaService::class.java)