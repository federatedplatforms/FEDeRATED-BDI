package nl.tno.federated.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.io.Serializable
import java.util.*
import javax.persistence.*

// Family of schemas
object EventSchema


// first version of the schema
object EventSchemaV1 : MappedSchema(
        schemaFamily = EventSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentEvent::class.java, PersistentLocation::class.java)) {

    override val migrationResource: String
        get() = "event.changelog-master"

    @Entity
    @Table(name = "LOCATION_DETAIL")
    class PersistentLocation(
            @Id @Column(name = "location_id")
            @Type(type = "uuid-char")
            val id: UUID,
            @Column(name = "country")
            val country: String,
            @Column(name = "city")
            val city: String
    ) {
        // Default constructor required by hibernate.
        constructor() : this(
                UUID.randomUUID(),
                "Antarctica",
                "FrozenBase"
        )
    }


    @Entity
    @Table(name = "EVENT_DETAIL")
    class PersistentEvent(
        @Column(name = "event_id")
        @Type(type = "uuid-char")
        val linearId: UUID,
        @Column(name = "eventType")
        val type: EventType,
        @ElementCollection
        val digitalTwins: List<UUID>,
        @Column(name = "time")
        val time: Date,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumn(name = "location_id", referencedColumnName = "location_id")
        val location: PersistentLocation,
        @Column(name = "eCMRuri")
        val eCMRuri: String,
        @Column(name = "milestone")
        val milestone: Milestone
    ) : PersistentState(), Serializable {
        constructor() : this(
            UUID.randomUUID(),
            EventType.OTHER,
            emptyList<UUID>(),
            Date(),
            PersistentLocation(),
            "no URI provided",
            Milestone.EXECUTED
        )
    }
}