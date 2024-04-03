package nl.tno.federated.api.event

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventTypeRepository : CrudRepository<EventTypeEntity, Long> {
    fun deleteByEventType(eventType: String)
}