package nl.tno.federated.services

import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.apache.commons.lang3.RandomUtils
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

data class GeneratedTTL(val constructedTTL: String,
                        val eventIdentifiers: List<String>,
                        val legalPerson: String,
                        val businessTransaction: String,
                        val equipment: String,
                        val digitalTwinTransportMeans:List<String>)

class TTLRandomGenerator {

    val prefixes = """
        @base <http://example.com/base/> . 
        @prefix : <https://ontology.tno.nl/logistics/federated/Event#> .
        @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> . 
        @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> . 
        @prefix dcterms: <http://purl.org/dc/terms/> . 
        @prefix LogisticsRoles: <https://ontology.tno.nl/logistics/federated/LogisticsRoles#> . 
        @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . 
        @prefix owl: <http://www.w3.org/2002/07/owl#> . 
        @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . 
        @prefix ReusableTags: <https://ontology.tno.nl/logistics/federated/ReusableTags#> .
        @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> . 
        @prefix DigitalTwin: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> . 
        @prefix skos: <http://www.w3.org/2004/02/skos/core#> . 
        @prefix xsd: <http://www.w3.org/2001/XMLSchema#> . 
        @prefix ex: <http://example.com/base#> . 
        @prefix time: <http://www.w3.org/2006/time#> . 
        @prefix dc: <http://purl.org/dc/elements/1.1/> . 
        @prefix era: <http://era.europa.eu/ns#> .  
    """

    fun generateRandomEvents(numberEvents: Int = 4): GeneratedTTL {

        val (generatedLegalPerson, generatedLegalPersonEntry) = generateLegalPerson()

        val (generatedEquipment, generatedEquipmentEntry) = generateEquipment()

        val (generatedBusinessTransaction, generatedBusinessTransactionEntry) =
                generateBusinessTransaction(generatedLegalPerson)

        var constructedTTL = prefixes + generatedLegalPersonEntry + generatedEquipmentEntry + generatedBusinessTransactionEntry

        val eventIdentifiers = arrayListOf<String>()

        val digitalTwinTransportMeansIdentifiers = mutableListOf<String>()
        val digitalTwinTransportMeansEntry = mutableListOf<String>()
        val generatedEventsEntries = mutableListOf<String>()

        val transportMeansIdentifier = RandomUtils.nextInt(0, 9999999)

        for (i in 0 until numberEvents) {
            val (digitalTwinTransportMeanIdentifier, digitalTwinTransportMeanEntry) = generateDigitalTwinTransportMeans(transportMeansIdentifier)
            digitalTwinTransportMeansIdentifiers.add(digitalTwinTransportMeanIdentifier)
            digitalTwinTransportMeansEntry.add(digitalTwinTransportMeanEntry)
        }

        for (i in 0 until numberEvents) {
            constructedTTL = "$constructedTTL ${digitalTwinTransportMeansEntry[i]}"
            val (eventIdentifier, generatedEventEntry) = generateEvent(digitalTwinTransportMeansIdentifiers[i],
                    generatedBusinessTransaction, generatedEquipment, i%2)
            eventIdentifiers.add(eventIdentifier)
            generatedEventsEntries.add(generatedEventEntry)
            constructedTTL = "$constructedTTL ${generatedEventsEntries[i]}"
        }

        return GeneratedTTL(
                constructedTTL = constructedTTL,
                eventIdentifiers = eventIdentifiers,
                legalPerson = generatedLegalPerson,
                businessTransaction = "businessTransaction-$generatedBusinessTransaction",
                equipment = "Equipment-$generatedEquipment",
                digitalTwinTransportMeans = digitalTwinTransportMeansIdentifiers
        )
    }

    private fun generateDigitalTwinTransportMeans(transportMeansIdentifier: Int): Pair<String, String> {
        val digitalTwinIdentifier = UUID.randomUUID().toString()
        return Pair("DigitalTwin-$digitalTwinIdentifier", """
        ex:DigitalTwin-$digitalTwinIdentifier a DigitalTwin:TransportMeans, owl:NamedIndividual, DigitalTwin:Vessel;
          rdfs:label "Vessel";
          DigitalTwin:hasVIN "$transportMeansIdentifier";
          DigitalTwin:hasTransportMeansID "$transportMeansIdentifier" .
            """)
    }

    private fun CharRange.random(numberOfChars: Int): String {
        var randomString = ""
        for (index in 0 until numberOfChars){
            val firstCharChoice = RandomUtils.nextInt(0, this.count())
            randomString += this.elementAt(firstCharChoice)
        }

        return randomString
    }

     private fun generateLegalPerson(): Pair<String, String> {

        // generate random 6-chars
        val firstChar = ('A'..'Z')
        val nextChars = ('a'..'z')
        val legalPerson = firstChar.random(1) + nextChars.random(5)
        return Pair("LegalPerson-$legalPerson", """
        ex:LegalPerson-$legalPerson a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;
          businessService:actorName "$legalPerson" .
            """)
    }

    private fun generateEquipment(): Pair<String, String> {
        val equipmentIdentifier = UUID.randomUUID().toString()
        return Pair(equipmentIdentifier, """
        ex:Equipment-$equipmentIdentifier a DigitalTwin:Equipment, owl:NamedIndividual;
          rdfs:label "TNO-test092022" .
            """)
    }

    private fun generateBusinessTransaction(involvedActor: String): Pair<String, String> {
        val businessTransactionIdentifier = UUID.randomUUID().toString()
        return Pair(businessTransactionIdentifier, """
        ex:businessTransaction-$businessTransactionIdentifier a businessService:Consignment, owl:NamedIndividual;
          businessService:consignmentCreationTime "2022-01-01T00:01:00"^^xsd:dateTime;
          businessService:involvedActor ex:$involvedActor .
            """)
    }

    // first string: hasTimestamp, second string: hasSubmissionTimestamp
    private fun generateTimestamps(): Pair<String, String> {
        // generate integers
        val year: Int = RandomUtils.nextInt(2018, 2022)
        val month = RandomUtils.nextInt(1, 13)
        val day = RandomUtils.nextInt(1, 29)
        val hour = RandomUtils.nextInt(0, 24)
        val minute = RandomUtils.nextInt(0, 60)
        val second = RandomUtils.nextInt(0, 60)

        val localDate = LocalDateTime.of(year, month, day, hour, minute, second)
        val offsetDate = OffsetDateTime.of(localDate, ZoneOffset.UTC)

        val stringDate = offsetDate.format(DateTimeFormatter.ISO_DATE_TIME)

        val monthSubmission = if (month > 1) month - 1 else 12
        val yearSubmission = if (month > 1) year else year - 1

        val localDateSubmission = LocalDateTime.of(yearSubmission, monthSubmission, day, hour, minute, second)
        val offsetDateSubmission = OffsetDateTime.of(localDateSubmission, ZoneOffset.UTC)

        val stringDateSubmission = offsetDateSubmission.format(DateTimeFormatter.ISO_DATE_TIME)

        val hasTimestamp = "Event:hasTimestamp \"$stringDate\"^^xsd:dateTime;"
        val hasSubmissionTimestamp = "Event:hasSubmissionTimestamp \"$stringDateSubmission\"^^xsd:dateTime ."

        return Pair(hasTimestamp, hasSubmissionTimestamp)
    }

    private fun String.ReCapitalize(): String {
        return this.toLowerCase().capitalize()
    }

    private fun generateDateTimeType(): String {
        val hasDateTimeType = EventType.values()
        val randomEventType = RandomUtils.nextInt(0, 3)
        return hasDateTimeType[randomEventType].toString().ReCapitalize()
    }

    private fun generateHasMilestone(startStop: Int): String {
        val hasMilestone = Milestone.values()
        return hasMilestone[startStop].toString().ReCapitalize()
    }

    // string 1: identifier, string 2: entry for TTL
    private fun generateEvent(digitalTwinIdentifier: String,
                              generatedBusinessTransaction: String, generatedEquipment: String,
                              milestonePosition: Int): Pair<String, String> {
        val generatedEventIdentifier = UUID.randomUUID().toString()
        val (hasTimestamp, hasSubmissionTimestamp) = generateTimestamps()
        val generatedEventEntry = """
        ex:Event-$generatedEventIdentifier a Event:Event, owl:NamedIndividual;
          $hasTimestamp
          Event:hasDateTimeType Event:${generateDateTimeType()};
          Event:involvesDigitalTwin ex:$digitalTwinIdentifier, ex:Equipment-$generatedEquipment;
          Event:involvesBusinessTransaction ex:businessTransaction-$generatedBusinessTransaction;
          Event:hasMilestone Event:${generateHasMilestone(milestonePosition)};
          $hasSubmissionTimestamp
            """
        return Pair(generatedEventIdentifier, generatedEventEntry)
    }

}