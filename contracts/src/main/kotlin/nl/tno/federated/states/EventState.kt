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
    override val goods: Set<UUID>,
    override val transportMean: Set<UUID>,
    override val location: Set<String>,
    override val otherDigitalTwins: Set<UUID>,
    override val timestamps: Set<Timestamp>,
    override val ecmruri: String,
    override val milestone: Milestone,
    override val fullEvent: String,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    override val labels: Set<String> = emptySet()
) : LinearState, Event(goods, transportMean, location, otherDigitalTwins, timestamps, ecmruri, milestone, fullEvent, labels), QueryableState {

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        if (schema is EventSchemaV1) {

            val pGoods = goods.map {
                it
            }.toMutableList()

            val pTransportMean = transportMean.map {
                it
            }.toMutableList()

            val pLocation = location.map {
                it
            }.toMutableList()

            val pOtherDigitalTwins = otherDigitalTwins.map {
                it
            }.toMutableList()

            return EventSchemaV1.PersistentEvent(
                    linearId.externalId ?: linearId.id.toString(),
                    pGoods.toList(),
                    pTransportMean.toList(),
                    pLocation,
                    pOtherDigitalTwins.toList(),
                    ecmruri,
                    milestone
            )
        } else
            throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(EventSchemaV1)

    /**
     * Returns true if everything besides times, participants and linearId are the same
     */
    fun equalsExceptTimesAndParticipants(other: EventState): Boolean {
        return super.goods == other.goods &&
                super.transportMean == other.transportMean &&
                super.location == other.location &&
                super.otherDigitalTwins == other.otherDigitalTwins &&
                super.ecmruri == other.ecmruri &&
                super.milestone == other.milestone
    }

    /**
     * Returns true if digital twins are the same
     */
    fun hasSameDigitalTwins(other: EventState): Boolean {
        return super.goods == other.goods &&
                super.transportMean == other.transportMean &&
                super.location == other.location &&
                super.otherDigitalTwins == other.otherDigitalTwins
    }
}

@CordaSerializable
enum class Milestone {
    START, STOP
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

open class Event(
    open val goods: Set<UUID>,
    open val transportMean: Set<UUID>,
    open val location: Set<String>,
    open val otherDigitalTwins: Set<UUID>,
    open val timestamps: Set<Timestamp>,
    open val ecmruri: String,
    open val milestone: Milestone,
    open val fullEvent: String,
    open val labels: Set<String> = emptySet()
)

@CordaSerializable
enum class PhysicalObject {
    CARGO, TRANSPORTMEAN, OTHER, GOOD, LOCATION
}