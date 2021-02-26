package nl.tno.federated.states

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
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
        mappedTypes = listOf(PersistentEvent::class.java, PersistentLocation::class.java, PersistentDigitalTwinUUID::class.java)) {

    override val migrationResource: String
        get() = "event.changelog-master"


    @Entity
    @Table(name = "EVENT_DETAIL")
    class PersistentEvent(
            @Column(name = "event_id")
            @Type(type = "uuid-char")
            val linearId: UUID,
            @Column(name = "EventType")
            val type: EventType,
//            @OneToMany(cascade = [CascadeType.PERSIST])
//            @JoinColumn(name = "DigitalTwins", referencedColumnName = "DT_id")
//            val digitalTwins: MutableList<PersistentDigitalTwinUUID>,
            @Column(name = "Time")
            val time: Date,
            @OneToOne(cascade = [CascadeType.PERSIST])
            @JoinColumn(name = "location_id", referencedColumnName = "location_id")
            val location: PersistentLocation
    ) : PersistentState(), Serializable {
        constructor() : this(
                UUID.randomUUID(),
                EventType.OTHER,
//                mutableListOf<PersistentDigitalTwinUUID>(),
                Date(),
                PersistentLocation()
        )
    }

    @Entity
    @CordaSerializable
    @Table(name = "DIGITAL_TWINS_UUID")
    class PersistentDigitalTwinUUID(
            @Id @Column(name = "DT_id")
            @Type(type = "uuid-char")
            val id: UUID,

            @Column(name = "digitalTwinUUID")
            @Type(type = "uuid-char")
            val digitalTwinUUID: UUID,

            @ManyToOne(targetEntity = PersistentEvent::class)
            var parent: EventSchemaV1.PersistentEvent?

    ) {
        constructor() : this(
                UUID.randomUUID(),
                UniqueIdentifier().id,
                null
        )
    }

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
}