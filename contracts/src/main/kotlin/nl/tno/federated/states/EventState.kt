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
        override val time: Date,
        override val ecmruri: String,
        override val milestone: Milestone,
        override val participants: List<AbstractParty> = listOf(),
        override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, Event(goods, transportMean, location, otherDigitalTwins, time, ecmruri, milestone), QueryableState {

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
                    time,
                    ecmruri,
                    milestone
            )
        } else
            throw IllegalArgumentException("Unsupported Schema")
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(EventSchemaV1)
}

@CordaSerializable
enum class Milestone {
    START, STOP
}


open class Event(
        open val goods: List<UUID>,
        open val transportMean: List<UUID>,
        open val location: List<UUID>,
        open val otherDigitalTwins: List<UUID>,
        open val time: Date,
        open val ecmruri: String,
        open val milestone: Milestone
)