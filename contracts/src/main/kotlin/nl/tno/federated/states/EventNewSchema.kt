package nl.tno.federated.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.io.Serializable
import java.util.*
import javax.persistence.*

// Family of schemas
object EventNewSchema


// first version of the schema
object EventNewSchemaV1 : MappedSchema(
        schemaFamily = EventNewSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentEvent::class.java)
) {

    override val migrationResource: String
        get() = "event.changelog-master"


    @Entity
    @Table(name = "EVENT_DETAIL")
    class PersistentEvent(
        @Column(name = "event_id")
        @Type(type = "uuid-char")
        val linearId: UUID,
        @ElementCollection
        val goods: List<UUID>,
        @ElementCollection
        val transportMean: List<UUID>,
        @ElementCollection
        val location: List<UUID>,
        @ElementCollection
        val otherDigitalTwins: List<UUID>,
        @Column(name = "time")
        val time: Date,
        @Column(name = "eCMRuri")
        val eCMRuri: String,
        @Column(name = "milestone")
        val milestone: MilestoneNew
    ) : PersistentState(), Serializable {
        constructor() : this(
            UUID.randomUUID(),
            emptyList<UUID>(),
            emptyList<UUID>(),
            emptyList<UUID>(),
            emptyList<UUID>(),
            Date(),
            "no URI provided",
            MilestoneNew.START
        )
    }
}