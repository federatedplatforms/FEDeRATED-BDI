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
        override val goods: List<UUID>,
        override val transportMean: List<UUID>,
        override val location: List<UUID>,
        override val otherDigitalTwins: List<UUID>,
        override val time: Date,
        override val ecmruri: String,
        override val milestone: MilestoneNew,
        override val participants: List<AbstractParty> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, EventNew(goods, transportMean, location, otherDigitalTwins, time, ecmruri, milestone)

@CordaSerializable
enum class MilestoneNew {
    START, STOP
}


open class EventNew(
        open val goods: List<UUID>,
        open val transportMean: List<UUID>,
        open val location: List<UUID>,
        open val otherDigitalTwins: List<UUID>,
        open val time: Date,
        open val ecmruri: String,
        open val milestone: MilestoneNew
)