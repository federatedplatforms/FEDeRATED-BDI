package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.DigitalTwinContract

// *********
// * State *
// *********
@BelongsToContract(DigitalTwinContract::class)
data class DigitalTwinState(
    val type: DigitalTwinType,
    val plate: String,
    val owner: String,
    var goods: List<UniqueIdentifier>,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState


@CordaSerializable
enum class DigitalTwinType {
    TRUCK, CARGO, OTHER
}