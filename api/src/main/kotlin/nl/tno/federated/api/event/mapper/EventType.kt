package nl.tno.federated.api.event.mapper

enum class EventType(val rmlFile: String) {
    LoadEvent("rml/EventMapping.ttl"),
    ArrivalEvent("rml/EventMapping.ttl")
}