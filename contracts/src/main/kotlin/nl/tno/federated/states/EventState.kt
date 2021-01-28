package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
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
) : LinearState, Event(type, digitalTwins, time, location)

@CordaSerializable
enum class EventType {
    ARRIVE, DEPART, LOAD, DISCHARGE, POSITION
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