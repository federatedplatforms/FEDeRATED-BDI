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
import kotlin.collections.LinkedHashMap

// *********
// * State *
// *********
@BelongsToContract(EventContract::class)
data class EventState(
    override val goods: List<UUID>,
    override val transportMean: List<UUID>,
    override val location: List<UUID>,
    override val otherDigitalTwins: List<UUID>,
    override val timestamps: LinkedHashMap<EventType, Date>,
    override val ecmruri: String,
    override val milestone: Milestone,
    override val fullEvent: String,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, Event(goods, transportMean, location, otherDigitalTwins, timestamps, ecmruri, milestone, fullEvent, linearId.externalId ?: linearId.id.toString()), QueryableState {

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
                    pGoods,
                    pTransportMean,
                    pLocation,
                    pOtherDigitalTwins,
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

open class Event(
    open val goods: List<UUID>,
    open val transportMean: List<UUID>,
    open val location: List<UUID>,
    open val otherDigitalTwins: List<UUID>,
    open val timestamps: LinkedHashMap<EventType, Date>,
    open val ecmruri: String,
    open val milestone: Milestone,
    open val fullEvent: String,
    open val id: String
)

fun <K, V> LinkedHashMap<K, V>.last(): Pair<K, V> {
    if (this.isEmpty()) throw NoSuchElementException("Map is empty.")
    val lastKey = this.keys.last()
    return Pair(lastKey, this[lastKey]!!)
}

fun <K, V> LinkedHashMap<K, V>.single(): Pair<K,V> {
    return when (size) {
        0 -> throw NoSuchElementException("Map is empty.")
        1 -> this.first()
        else -> throw IllegalArgumentException("Map has more than one element.")
    }
}

fun <K, V> LinkedHashMap<K, V>.first(): Pair<K, V> {
    if (this.isEmpty()) throw NoSuchElementException("Map is empty.")
    return Pair(this.keys.iterator().next(), this.values.iterator().next())
}


fun <K, V> java.util.LinkedHashMap<K, V>.numberOfDifferingEntries(other: java.util.LinkedHashMap<K, V>): Int {
    var differing = 0
    other.forEach { (k, v) -> if (!this.containsKey(k) || this[k] != v) differing++ }
    return differing
}

fun <K, V> LinkedHashMap<K, V>.removeLast(): LinkedHashMap<K, V> {
    this.remove(this.last().first)
    return this
}