package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.EventNewContract
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(EventNewContract::class)
data class EventNewState(
        val goods: List<UUID>,
        val transportMean: List<UUID>,
        val location: List<UUID>,
        val otherDigitalTwins: List<UUID>,
        val time: Date,
        val ecmruri: String,
        val milestone: MilestoneNew,
        override val participants: List<AbstractParty> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState

@CordaSerializable
enum class MilestoneNew {
    START, STOP
}