package nl.tno.federated.api.event

class EventWithDestinations(val event: String, val eventType: String, val eventDestinations: Set<String>)