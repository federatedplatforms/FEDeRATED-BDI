package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.EventContract
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(EventContract::class)
data class EventState(
    override val type: EventType,
    override val digitalTwins: List<UniqueIdentifier>,
    override val time: Date,
    override val location: Location,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, Event(type, digitalTwins, time, location), QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is EventSchemaV1) {
            val pLocation =
                EventSchemaV1.PersistentLocation(
                        UniqueIdentifier().id,
                        location.country,
                        location.city
                )


            val pDigitalTwins = digitalTwins.map {
                                        it.id
            }.toMutableList()

            return EventSchemaV1.PersistentEvent(
                    linearId.id,
                    type,
                    pDigitalTwins,
                    time,
                    pLocation
            )
        } else
            throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(EventSchemaV1)
}

@CordaSerializable
enum class EventType {
    LOAD, DEPART, ARRIVE, DISCHARGE, OTHER
}
@CordaSerializable
data class Location (
    val country: String,
    val city: String
        )

open class Event(
    open val type: EventType,
    open val digitalTwins: List<UniqueIdentifier>,
    open val time: Date,
    open val location: Location
)