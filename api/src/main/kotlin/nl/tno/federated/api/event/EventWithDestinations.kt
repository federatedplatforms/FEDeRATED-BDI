package nl.tno.federated.api.event

class NewEvent(val event: String, val eventType: String, val eventDestinations: Set<String>?)