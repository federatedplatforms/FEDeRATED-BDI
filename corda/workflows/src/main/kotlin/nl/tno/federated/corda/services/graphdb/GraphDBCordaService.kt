package nl.tno.federated.corda.services.graphdb

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken

@CordaService
class GraphDBCordaService(serviceHub: AppServiceHub) : IGraphDBService, SingletonSerializeAsToken() {

    private var graphDBService: IGraphDBService? = null

    fun setGraphDBService(graphDBService: IGraphDBService) {
        this.graphDBService = graphDBService
    }

    private fun graphdb(): IGraphDBService {
        if (graphDBService == null) graphDBService = GraphDBService()
        return graphDBService!!
    }

    override fun queryEventIds(): String = graphdb().queryEventIds()

    override fun generalSPARQLquery(query: String, privateRepo: Boolean): String = graphdb().generalSPARQLquery(query, privateRepo)

    override fun queryEventById(id: String): String = graphdb().queryEventById(id)

    override fun queryAllEventPropertiesById(id: String): String = graphdb().queryAllEventPropertiesById(id)

    override fun queryCountryGivenEventId(eventId: String): String = graphdb().queryCountryGivenEventId(eventId)

    override fun unpackCountriesFromSPARQLresult(sparqlResult: String): List<String> = graphdb().unpackCountriesFromSPARQLresult(sparqlResult)

    override fun queryEventComponent(id: String): String = graphdb().queryEventComponent(id)

    override fun isQueryResultEmpty(queryResult: String): Boolean = graphdb().isQueryResultEmpty(queryResult)

    override fun queryCityName(locationName: String): String = graphdb().queryCityName(locationName)
    override fun queryCountryName(locationName: String): String = graphdb().queryCountryName(locationName)

    override fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean =
        graphdb().areEventComponentsAccurate(queryResult, businessTransaction, transportMeans, equipmentUsed)

    override fun insertEvent(ttl: String, privateRepo: Boolean): Boolean = graphdb().insertEvent(ttl, privateRepo)

}