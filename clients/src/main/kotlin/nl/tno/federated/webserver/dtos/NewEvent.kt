package nl.tno.federated.webserver.dtos

import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.flows.DigitalTwinPair
import nl.tno.federated.states.Milestone
import java.util.*

@CordaSerializable
data class NewEvent(
    val digitalTwins: List<DigitalTwinPair>,
    val eCMRuri: String,
    val milestone: Milestone,
    val time: Date = Date(),
    val id: String = "",
    val uniqueId: Boolean = true
)
