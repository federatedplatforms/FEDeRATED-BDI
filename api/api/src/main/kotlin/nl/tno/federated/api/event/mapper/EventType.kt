package nl.tno.federated.api.event.mapper


import jakarta.validation.constraints.NotNull

class EventType(@NotNull val eventType: String, @NotNull val rml: String, val shacl: String?)