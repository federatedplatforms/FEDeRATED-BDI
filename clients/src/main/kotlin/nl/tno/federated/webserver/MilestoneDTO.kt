package nl.tno.federated.webserver

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import nl.tno.federated.states.Location
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import java.util.*

data class MilestoneDTO(
    val type: MilestoneType,
    val digitalTwins: List<UniqueIdentifier>,
    val time: Date,
    val location: Location
//  val counterparty: String
    )
{
    constructor(state: MilestoneState) : this(state.type, state.digitalTwins, state.time, state.location)
}
