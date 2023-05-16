package nl.tno.federated.api.event.mapper

enum class EventType(val rmlFile: String) {
    LoadEvent("rml/LoadEvent RML Mapping.ttl"),
    ArrivalEvent("rml/ArrivalEvent RML Mapping.ttl")
}