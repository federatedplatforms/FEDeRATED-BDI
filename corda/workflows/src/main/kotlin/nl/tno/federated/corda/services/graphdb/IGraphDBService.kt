package nl.tno.federated.corda.services.graphdb

interface IGraphDBService {

    fun queryEventIds(): String
    fun generalSPARQLquery(query: String, privateRepo: Boolean = false): String
    fun queryEventById(id: String): String
    fun queryAllEventPropertiesById(id: String): String
    fun queryEventComponent(id: String): String
    fun isQueryResultEmpty(queryResult: String): Boolean
    fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean
    fun insertEvent(ttl: String, privateRepo: Boolean): Boolean

}