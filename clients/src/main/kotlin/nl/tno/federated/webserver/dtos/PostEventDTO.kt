package nl.tno.federated.webserver.dtos

import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.flows.DigitalTwinPair
import nl.tno.federated.states.Milestone
import java.util.*

@CordaSerializable
data class PostEventDTO(
        val digitalTwins: List<DigitalTwinPair>,
        val ecmruri: String,
        val milestone: Milestone,
        val time: Date = Date(),
        val id: String = "",
        val uniqueId: Boolean = true,
        val fullEvent: String,
        val countriesInvolved: Set<String>
)
