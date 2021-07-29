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
        override val goods: List<UUID>,
        override val transportMean: List<UUID>,
        override val location: List<UUID>,
        override val otherDigitalTwins: List<UUID>,
        override val eventCreationtime: Date,
        override val timestamps: List<TimeAndType>,
        override val ecmruri: String,
        override val milestone: Milestone,
        override val participants: List<AbstractParty> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, Event(goods, transportMean, location, otherDigitalTwins, eventCreationtime, timestamps, ecmruri, milestone), QueryableState {

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
                    linearId.id,
                    pGoods,
                    pTransportMean,
                    pLocation,
                    pOtherDigitalTwins,
                    eventCreationtime,
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
    fun equals(other: EventState): Boolean {
        if(
            super.goods == other.goods &&
            super.transportMean == other.transportMean &&
            super.location == other.location &&
            super.otherDigitalTwins == other.otherDigitalTwins &&
            super.ecmruri == other.ecmruri &&
            super.milestone == other.milestone
        ) return true
        else return false
    }

    /**
     * Returns true if digital twins are the same
     */
    fun hasSameDigitalTwins(other: EventState): Boolean {
        if(
            super.goods == other.goods &&
            super.transportMean == other.transportMean &&
            super.location == other.location &&
            super.otherDigitalTwins == other.otherDigitalTwins
        ) return true
        else return false
    }
}

@CordaSerializable
enum class Milestone {
    START, STOP
}

@CordaSerializable
enum class TimeType {
    PLANNED, ESTIMATED, ACTUAL
}

@CordaSerializable
data class TimeAndType (
    val time : Date,
    val type : TimeType
)


open class Event(
        open val goods: List<UUID>,
        open val transportMean: List<UUID>,
        open val location: List<UUID>,
        open val otherDigitalTwins: List<UUID>,
        open val eventCreationtime: Date,
        open val timestamps: List<TimeAndType>,
        open val ecmruri: String,
        open val milestone: Milestone
)