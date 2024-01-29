package nl.tno.federated.api.corda

import nl.tno.federated.corda.states.EventState

// omits the public key part from the participants list.
data class SimpleEventState(val eventUUID: String, val eventType: String, val eventData: String)

fun EventState.toSimpleEventState() = SimpleEventState(this.linearId.externalId.toString(), this.eventType, this.event)
