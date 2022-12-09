package nl.tno.federated.corda.services.graphdb

interface IGraphDBService {

    fun queryEventIds(): String
    fun generalSPARQLquery(query: String, privateRepo: Boolean = false): String
    fun queryEventById(id: String): String
    fun queryAllEventPropertiesById(id: String): String
    fun queryEventComponent(id: String): String
    fun queryCountryGivenEventId(eventId: String): String
    fun unpackCountriesFromSPARQLresult(sparqlResult: String): List<String>
    fun isQueryResultEmpty(queryResult: String): Boolean
    fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean
    fun queryCityName(locationName: String): String
    fun queryCountryName(locationName: String): String
    fun insertEvent(ttl: String, privateRepo: Boolean): Boolean

}