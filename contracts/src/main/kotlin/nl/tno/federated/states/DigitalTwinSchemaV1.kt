package nl.tno.federated.states

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import org.hibernate.annotations.Type
import java.io.Serializable
import java.util.*
import javax.persistence.*

// Family of schemas
object DigitalTwinSchema


// first version of the schema
object DigitalTwinSchemaV1 : MappedSchema(
    schemaFamily = DigitalTwinSchema.javaClass,
    version = 1,
    mappedTypes = listOf(PersistentDigitalTwin::class.java, PersistentCargo::class.java)) {

    override val migrationResource: String
        get() = "digitaltwin.changelog-master";

    @Entity
    @Table(name = "CARGO_DETAIL")
    class PersistentCargo(
        @Id @Column(name = "id")
        @Type(type = "uuid-char")
        val id: UUID,
        @Column(name = "dangerous")
        val dangerous : Boolean,
        @Column(name = "dryBulk")
        val dryBulk : Boolean,
        @Column(name = "excise")
        val excise : Boolean,
        @Column(name = "liquidBulk")
        val liquidBulk : Boolean,
        @Column(name = "maximumSize")
        val maximumSize : Int,
        @Column(name = "maximumTemperature")
        val maximumTemperature : String,
        @Column(name = "maximumVolume")
        val maximumVolume : Int,
        @Column(name = "maximumSize")
        val minimumSize : Int,
        @Column(name = "maximumTemperature")
        val minimumTemperature : String,
        @Column(name = "minimumVolume")
        val minimumVolume : Int,
        @Column(name = "minumimWeight")
        val minimumWeight : Double,
        @Column(name = "natureOfCargo")
        val natureOfCargo : String,
        @Column(name = "numberOfTEU")
        val numberOfTEU : Int,
        @Column(name = "properties")
        val properties : String,
        @Column(name = "reefer")
        val reefer : Boolean,
        @Column(name = "tarWeight")
        val tarWeight : Double,
        @Column(name = "temperature")
        val temperature: String,
        @Column(name = "type")
        val type : String,
        @Column(name = "waste")
        val waste : Boolean
    ) {
        // Default constructor required by hibernate.
        constructor() : this(
            UUID.randomUUID(),
            false,
            false,
            false,
            false,
            0,
            "",
            0,
            0,
            "",
            0,
            0.0,
            "",
            0,
            "",
            false,
            0.0,
            "",
            "",
            false
        )
    }
    @Entity
        @Table(name = "TRUCK_DETAIL")
        class PersistentTruck(
            @Id @Column(name = "id")
            @Type(type = "uuid-char")
            val id: UUID,
            @Column(name = "dangerous")
            val licensePlate : String
        ) {
            // Default constructor required by hibernate.
            constructor() : this(
                UUID.randomUUID(),
                ""
            )
        }

    @Entity
    @Table(name = "DIGITALTWIN_DETAIL")
    class PersistentDigitalTwin(
        @Column(name = "id")
        val linearId: UniqueIdentifier,
        @Column(name = "physicalobject")
        val PhysicalObject: PhysicalObject,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumns(JoinColumn(name = "id", referencedColumnName = "id"))
        val cargo: PersistentCargo?,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumns(JoinColumn(name = "id", referencedColumnName = "id"))
        val truck: PersistentTruck?
    ) : PersistentState(), Serializable {
        constructor() : this(
            UniqueIdentifier(),
            nl.tno.federated.states.PhysicalObject.OTHER,
            null,
            null
        )
    }
}