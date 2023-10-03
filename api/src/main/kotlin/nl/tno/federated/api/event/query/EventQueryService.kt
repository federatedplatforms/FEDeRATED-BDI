package nl.tno.federated.api.event.query

interface EventQueryService {
    fun executeQuery(query: EventQuery): String?
    fun findById(id: String): String?
    fun findAll(): List<String>
}