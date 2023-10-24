package nl.tno.federated.api.event

import nl.tno.federated.api.event.mapper.EventType
import java.util.*

/**
 * Event class containing the original event json, the event type, generated UUID and event RDF.
 */
data class EnrichedEvent(val eventJson: String, val eventType: EventType, val eventUUID: UUID, val eventRDF: String)