package nl.tno.federated.api.event.type


import jakarta.validation.constraints.NotNull

data class EventType(@NotNull val eventType: String, @NotNull val rml: String, val shacl: String?, val schemaDefinition: String? = null)