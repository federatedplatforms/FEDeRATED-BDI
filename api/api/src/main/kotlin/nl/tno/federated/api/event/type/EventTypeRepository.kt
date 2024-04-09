package nl.tno.federated.api.event.type

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository


@Repository
interface EventTypeRepository : CrudRepository<EventTypeEntity, Long> {

    fun findByEventType(eventType: String): EventTypeEntity?
}