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
    val type: MilestoneType,
    val digitalTwins: List<UniqueIdentifier>,
    val time: Date,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState

@CordaSerializable
enum class MilestoneType {
    ARRIVE, DEPART, LOAD, DISCHARGE, POSITION
}