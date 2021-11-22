package nl.tno.federated.webserver.dtos

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class NewEvent(
        /*val digitalTwins: List<DigitalTwinPair>,
        val ecmruri: String,
        val milestone: Milestone,
        val time: Date = Date(),
        val id: String = "",
        val uniqueId: Boolean = true,*/
        val fullEvent: String,
        val countriesInvolved : Set<String>
)
