package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.DigitalTwinContract

@BelongsToContract(DigitalTwinContract::class)
data class DigitalTwinState(
    val physicalObject : PhysicalObject = PhysicalObject.OTHER,
    val cargo : Cargo? = null,
    val truck : Truck? = null,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is DigitalTwinSchemaV1) {
            val pCargo = cargo?.let {
                DigitalTwinSchemaV1.PersistentCargo(
                    UniqueIdentifier().id,
                    cargo.dangerous,
                    cargo.dryBulk,
                    cargo.excise,
                    cargo.liquidBulk,
                    cargo.maximumSize,
                    cargo.maximumTemperature,
                    cargo.maximumVolume,
                    cargo.minimumSize,
                    cargo.minimumTemperature,
                    cargo.minimumVolume,
                    cargo.minimumWeight,
                    cargo.natureOfCargo,
                    cargo.numberOfTEU,
                    cargo.properties,
                    cargo.reefer,
                    cargo.tarWeight,
                    cargo.temperature,
                    cargo.type,
                    cargo.waste
                )
            }

            val pTruck = truck?.let {
                DigitalTwinSchemaV1.PersistentTruck(
                    UniqueIdentifier().id,
                    truck.licensePlate
                )
            }

            return DigitalTwinSchemaV1.PersistentDigitalTwin(
                linearId.id,
                physicalObject,
                pCargo,
                pTruck
            )
        } else
        throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(DigitalTwinSchemaV1)
}

@CordaSerializable
enum class PhysicalObject {
    CARGO, TRANSPORTMEAN, OTHER
}

@CordaSerializable
data class Cargo(
    val dangerous : Boolean,
    val dryBulk : Boolean,
    val excise : Boolean,
    val liquidBulk : Boolean,
    val maximumSize : Int,
    val maximumTemperature : String,
    val maximumVolume : Int,
    val minimumSize : Int,
    val minimumTemperature : String,
    val minimumVolume : Int,
    val minimumWeight : Double,
    val natureOfCargo : String,
    val numberOfTEU : Int,
    val properties : String,
    val reefer : Boolean,
    val tarWeight : Double,
    val temperature: String,
    val type : String,
    val waste : Boolean
)

@CordaSerializable
data class Truck(
    val licensePlate : String
)