package nl.tno.federated.states

import nl.tno.federated.contracts.MilestoneContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import java.sql.Timestamp
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(MilestoneContract::class)
data class MilestoneState(
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val type: MilestoneType,
    override val digitalTwins: List<UniqueIdentifier>,
    override val time: Date,
    override val location: Location
) : LinearState, MilestoneDTO(type, digitalTwins, time, location)

@CordaSerializable
enum class MilestoneType {
    ARRIVE, DEPART, LOAD, DISCHARGE, POSITION
}
@CordaSerializable
data class Location (
    val country: String,
    val city: String
        )

open class MilestoneDTO(
    open val type: MilestoneType,
    open val digitalTwins: List<UniqueIdentifier>,
    open val time: Date,
    open val location: Location
)