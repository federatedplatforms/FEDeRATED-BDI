package nl.tno.federated.webserver.dtos

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import nl.tno.federated.states.Location
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import java.util.*

data class DigitalTwinDTO(
    val type: DigitalTwinType,
    val identifier: UniqueIdentifier,
    val location: Location
    )
{
    constructor(state: CargoState) : this(state.type, state.digitalTwins, state.time, state.location)
    constructor(state: LocationState) : this(state.type, state.digitalTwins, state.time, state.location)
    constructor(state: ProductState) : this(state.type, state.digitalTwins, state.time, state.location)
}


enum class DigitalTwinType {
    CARGO, LOCATION, PRODUCT
}