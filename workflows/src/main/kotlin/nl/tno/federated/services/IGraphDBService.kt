package nl.tno.federated.services

import nl.tno.federated.states.Event

interface IGraphDBService {
    /**
     * Does not really belong here.
     */
    fun parseRDFToEvents(rdfFullData: String): List<Event>
    fun queryEventIds(): String
    fun generalSPARQLquery(query: String, privateRepo: Boolean = false): String
    fun queryEventById(id: String): String
    fun queryAllEventPropertiesById(id: String): String
    fun queryEventComponent(id: String): String
    fun isQueryResultEmpty(queryResult: String): Boolean
    fun areEventComponentsAccurate(queryResult: String, businessTransaction: String, transportMeans: String, equipmentUsed: String): Boolean
    fun insertEvent(ttl: String, privateRepo: Boolean): Boolean

}