package nl.tno.federated.semantic.adapter.tradelens

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

data class Event(
    val id: String = "",
    val eventType: String,
    val dateTimeType: String,
    val milestone: String,
    val timestamp: String = "",
    val submissionTimestamp: String = "",
    val digitalTwin: String = "",
    val digitalTwin2: String = "",
    val infrastructure: String = "",
    val businessService: String = ""
)

data class Container(
    val id: String = "",
    val containerType: List<String>,
    val billOfLading: List<String>,
    val equipmentNumber: String?,
    val verifiedGrossMass: String?,
    val harmonizedSystemCode: List<String>
)

class TradelensMapperException(message: String) : Exception(message)

@Component
class TradelensMapper(private val objectMapper: ObjectMapper) {

    /**
     * Map the incoming Tradelens Event JSON to the JSON format expected by the RML mapper.
     */
    fun createPreMappingEvents(jsonData: String): Pair<String, List<Event>> {
        val root = objectMapper.readTree(jsonData)
        val events = if (root.has("events")) root.get("events") else root

        if (!events.iterator().hasNext()) throw TradelensMapperException("No events found in provided input!")

        val result = events.iterator().asSequence().map { item ->
            with(item) {
                // 1. get the OPTIONAL event type
                val jsonEventType = listOf("Arrival", "Departure", "Load", "Discharge").filter { it ->
                    getAsText("eventType")!!.contains(it, ignoreCase = true)
                }
                val extractedEventType = if (jsonEventType.isNotEmpty()) jsonEventType[0] else ""

                // 2. get the OPTIONAL datetime type
                val jsonDateTimeType = listOf("Actual", "Estimated", "Expected", "Planned", "Requested").filter { it ->
                    getAsText("eventType")!!.contains(it, ignoreCase = true)
                }
                val extractedDatetimeType = if (jsonDateTimeType.isNotEmpty()) jsonDateTimeType[0] else ""

                // 3. get the OPTIONAL milestone
                val extractedMilestone = if (listOf("Departure", "Discharge").any { it ->
                        getAsText("eventType")!!.contains(it, ignoreCase = true)
                    }) "End" else "Start"

                Event(
                    id = getAsText("eventTransactionId") ?: "", // OPTIONAL
                    eventType = extractedEventType, // OPTIONAL
                    dateTimeType = extractedDatetimeType, // OPTIONAL
                    milestone = extractedMilestone, // OPTIONAL
                    timestamp = getAsText("eventOccurrenceTime8601") ?: "", // OPTIONAL
                    submissionTimestamp = getAsText("eventSubmissionTime8601")!!, // MANDATORY
                    digitalTwin = getAsText("transportEquipmentId") ?: "", // OPTIONAL
                    infrastructure = get("location")?.getAsText("unlocode") ?: "", // OPTIONAL
                    businessService = getAsText("consignmentId") ?: "" // OPTIONAL
                )
            }
        }.toList()
        return Pair(objectMapper.writeValueAsString(result), result)
    }

    /**
     * Map the incoming Tradelens Container JSON to the JSON format expected by the RML mapper.
     */
    fun createPreMappingContainers(jsonData: String): Pair<String, List<Container>> {
        val root = objectMapper.readTree(jsonData)
        val input = if (root.get("transportEquipmentList") != null) root.get("transportEquipmentList") else root
        val items = if(input.isArray) input.iterator().asSequence().toList() else listOf(input)

        if (items.isEmpty()) throw TradelensMapperException("No container details found in provided input.")

        val result = items.map { it ->

            val summary = it.get("transportEquipmentSummary") ?: it
            val equipmentTypes = mutableListOf<String>()

            if (summary.get("transportEquipmentTypes") != null) {
                for (equipmentType in summary["transportEquipmentTypes"].elements()) {
                    equipmentTypes.add(equipmentType["code"].asText())
                }
            }

            val billsOfLading = mutableListOf<String>()

            if (summary.get("billsOfLading") != null) {
                for (billOfLading in summary["billsOfLading"].elements()) {
                    billsOfLading.add(billOfLading.asText())
                }
            }

            val harmonizedSystemCodes = mutableListOf<String>()

            if (summary.get("cargoTypes") != null) {
                for (cargoType in summary["cargoTypes"].elements()) {
                    harmonizedSystemCodes.add(cargoType["code"].asText())
                }
            }

            Container(
                id = summary.getAsText("transportEquipmentId") ?: "", // OPTIONAL
                containerType = equipmentTypes, // OPTIONAL
                billOfLading = billsOfLading, // OPTIONAL
                equipmentNumber = summary.getAsText("equipmentNumber") ?: "",
                verifiedGrossMass = summary.getAsText("verifiedGrossMass") ?: "", // OPTIONAL
                harmonizedSystemCode = listOf() //harmonizedSystemCodes // MANDATORY
            )

        }.toList()
        return Pair(objectMapper.writeValueAsString(result), result)
    }
}

private fun JsonNode.getAsText(name: String) = get(name)?.asText()