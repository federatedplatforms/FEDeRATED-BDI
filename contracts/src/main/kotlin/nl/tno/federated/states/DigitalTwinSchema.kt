package nl.tno.federated.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import nl.tno.federated.states.PhysicalObject.OTHER
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
    mappedTypes = listOf(PersistentDigitalTwin::class.java, PersistentCargo::class.java, PersistentTruck::class.java)) {

    override val migrationResource: String
        get() = "digitaltwin.changelog-master"


    @Entity
    @Table(name = "TRUCK_DETAIL")
    class PersistentTruck(
        @Id @Column(name = "truck_id")
        @Type(type = "uuid-char")
        val id: UUID,
        @Column(name = "license_plate")
        val licensePlate: String
    ) {
        // Default constructor required by hibernate.
        constructor() : this(
            UUID.randomUUID(),
            ""
        )
    }

    @Entity
    @Table(name = "CARGO_DETAIL")
    class PersistentCargo(
        @Id @Column(name = "cargo_id")
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
        @Column(name = "minimumSize")
        val minimumSize : Int,
        @Column(name = "minimumTemperature")
        val minimumTemperature : String,
        @Column(name = "minimumVolume")
        val minimumVolume : Int,
        @Column(name = "minimumWeight")
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
    @Table(name = "DIGITALTWIN_DETAIL")
    class PersistentDigitalTwin(
        @Column(name = "linear_id")
        @Type(type = "uuid-char")
        val linearId: UUID,
        @Column(name = "physicalObject")
        val PhysicalObject: PhysicalObject,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumn(name = "cargo_id", referencedColumnName = "cargo_id")
        val cargo: PersistentCargo?,
        @OneToOne(cascade = [CascadeType.PERSIST])
        @JoinColumn(name = "truck_id", referencedColumnName = "truck_id")
        val truck: PersistentTruck?
    ) : PersistentState(), Serializable {
        constructor() : this(
            UUID.randomUUID(),
            OTHER,
            PersistentCargo(),
            PersistentTruck()
        )
    }
}