package nl.tno.federated.states

import nl.tno.federated.contracts.DigitalTwinContract
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
@BelongsToContract(DigitalTwinContract::class)
data class DigitalTwinState(
    val type: DigitalTwinType,
    val plate: String,
    val owner: String,
    var goods: List<UniqueIdentifier>?,
    var lastMilestone: UniqueIdentifier?,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState


@CordaSerializable
enum class DigitalTwinType {
    TRUCK, CARGO, OTHER
}