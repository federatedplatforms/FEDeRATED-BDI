package nl.tno.federated.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Table

// Family of schemas
object EventSchema


// first version of the schema
object EventSchemaV1 : MappedSchema(
        schemaFamily = EventSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentEvent::class.java)
) {

    override val migrationResource: String
        get() = "event.changelog-master"


    @Entity
    @Table(name = "EVENT_DETAIL")
    class PersistentEvent(
        @Column(name = "event_id")
        val linearId: String,
        @ElementCollection
        val goods: List<UUID>,
        @ElementCollection
        val transportMean: List<UUID>,
        @ElementCollection
        val location: List<String>,
        @ElementCollection
        val otherDigitalTwins: List<UUID>,
        @Column(name = "eCMRuri")
        val eCMRuri: String,
        @Column(name = "milestone")
        val milestone: Milestone
    ) : PersistentState(), Serializable {
        constructor() : this(
            "no ID provided",
            emptyList<UUID>(),
            emptyList<UUID>(),
            emptyList<String>(),
            emptyList<UUID>(),
            "no URI provided",
            Milestone.START
        )
    }
}