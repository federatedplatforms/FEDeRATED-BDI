package nl.tno.federated.states

import com.sun.org.apache.xpath.internal.operations.Bool
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty

// *********
// * State *
// *********
//@BelongsToContract(DigitalTwinContract::class)
data class TruckState(
    override val licensePlate: String,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState, Truck(licensePlate)

open class Truck(
    open val licensePlate : String
) : PhysicalObject()
