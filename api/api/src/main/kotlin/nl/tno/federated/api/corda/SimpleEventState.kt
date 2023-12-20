package nl.tno.federated.api.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.shared.states.EventState

// omits the public key part from the participants list.
data class SimpleEventState(val event: String, val eventType: String, val participants: List<CordaX500Name?>)

fun EventState.toSimpleEventState() = SimpleEventState(this.event, this.eventType, this.participants.map { it.nameOrNull() })
