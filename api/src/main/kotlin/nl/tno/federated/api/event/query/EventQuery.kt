package nl.tno.federated.api.event.query

enum class EventQueryType {
    SPARQL
}

data class EventQuery(val queryString: String, val queryType: EventQueryType = EventQueryType.SPARQL, val eventId: String? = null)