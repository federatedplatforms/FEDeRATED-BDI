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
    val fullEvent: String,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    var accessTokens: MutableMap<AbstractParty,String> = mutableMapOf()
) : LinearState
@CordaSerializable
enum class Milestone {
    START, END
}

@CordaSerializable
enum class EventType {
    PLANNED, ESTIMATED, ACTUAL
}

@CordaSerializable
data class Timestamp (
        val id: String,
        val time: Date,
        val type: EventType
        )

@CordaSerializable
data class Event(
    val goods: Set<UUID>,
    val transportMean: Set<UUID>,
    val location: Set<String>,
    val otherDigitalTwins: Set<UUID>,
    val timestamps: Set<Timestamp>,
    val ecmruri: String,
    val milestone: Milestone,
    val businessTransaction: String,
    val fullEvent: String,
    val labels: Set<String> = emptySet()
) {
    fun allEvents() = setOf(this.goods, this.transportMean, this.otherDigitalTwins)
    fun allEventsAndLocations() = allEvents() + location

}

@CordaSerializable
enum class PhysicalObject {
    CARGO, TRANSPORTMEAN, OTHER, GOOD, LOCATION
}