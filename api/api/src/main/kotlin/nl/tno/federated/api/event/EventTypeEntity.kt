package nl.tno.federated.api.event

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("EVENT_TYPE")
class EventTypeEntity(
    @Id
    var id: Long? = null,
    val eventType: String,
    val rml: String,
    val shacl: String?
)