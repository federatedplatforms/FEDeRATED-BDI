package nl.tno.federated.corda.services

import nl.tno.federated.services.PrefixHandlerTTLGenerator
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
                        val digitalTwinTransportMeans: List<String>)

data class TripTTL(val constructedTTL: String,
                   val eventsIdentifiers: List<String>,
                   val eventsAtLocations: Map<String, String>,
                   val transportMeansIdentifiers: List<String>,
                   val eventsAtCities: Map<String, String>,
                   val eventsAtCountries: Map<String, String>,
                   val eventsEntries: List<String>,
                   val helperItems: TripHelperItems)

data class TripHelperItems(val helperItemsTTL: String,
                           val generatedGoodsIdentifier: String,
                           val generatedBusinessTransaction: String,
                           val generatedPILocations: List<PILocation>,
                           val transportMeansIdentifier: Int)

data class PILocation(val generatedPIname: String, val generatedPIentry: String, val generatedPIcity: String, val generatedPIcountry: String)

class TTLRandomGenerator {

    private val prefixes = PrefixHandlerTTLGenerator.getPrefixesTTLGenerator()

    fun generateRandomEvents(numberEvents: Int = 4, countryCode: String = "NL"): GeneratedTTL {

        val (generatedLegalPerson, generatedLegalPersonEntry) = generateLegalPerson()

        val (generatedEquipment, generatedEquipmentEntry) = generateEquipment()

        val generatedLocation = generateLocation(countryCode)
        val generatedPIname = generatedLocation.second.first
        val generatedPIentry = generatedLocation.second.second

        val (generatedBusinessTransaction, generatedBusinessTransactionEntry) = generateBusinessTransaction(generatedLegalPerson)

        var constructedTTL = prefixes + generatedLegalPersonEntry + generatedEquipmentEntry + generatedBusinessTransactionEntry + generatedPIentry

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
                generatedBusinessTransaction, generatedEquipment, generatedPIname, i % 2)
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

    fun generateTripEvents(): TripTTL {
        val eventEntries = mutableListOf<String>()
        val helperItems = generateEventHelperItems()
        var constructedTTL = helperItems.helperItemsTTL

        val eventsAtLocation = mutableMapOf<String, String>()
        val eventsAtCities = mutableMapOf<String, String>()
        val eventsAtCountries = mutableMapOf<String, String>()

        // unpack helper items: transportMeansCode
        val transportMeansCode = helperItems.transportMeansIdentifier

        // unpack helper items: goods
        val generatedGoodsIdentifier = helperItems.generatedGoodsIdentifier

        // unpack helper items: business transaction
        val generatedBusinessTransaction = helperItems.generatedBusinessTransaction

        // unpack helper items: locations - departure (NL)
        val generatedPInameDeparture = helperItems.generatedPILocations[0].generatedPIname
        val generatedDepartureCity = helperItems.generatedPILocations[0].generatedPIcity
        val generatedDepartureCountry = helperItems.generatedPILocations[0].generatedPIcountry

        // unpack helper items: locations - arrival (DE)
        val generatedPInameArrival = helperItems.generatedPILocations[1].generatedPIname
        val generatedArrivalCity = helperItems.generatedPILocations[1].generatedPIcity
        val generatedArrivalCountry = helperItems.generatedPILocations[1].generatedPIcountry

        // unpack helper items: locations - border (NL)
        val generatedPInameBorderNetherlands = helperItems.generatedPILocations[2].generatedPIname
        val generatedBorderCountryNL = helperItems.generatedPILocations[2].generatedPIcountry

        // unpack helper items: locations - border (DE)
        val generatedPInameBorderGermany = helperItems.generatedPILocations[3].generatedPIname
        val generatedBorderCountryDE = helperItems.generatedPILocations[3].generatedPIcountry


        // unpack helper items: locations - border (DE)

        // 1. generateLoadEvent()
        val (digitalTwinTransportMeanIdentifier, digitalTwinTransportMeanEntry) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (loadEventIdentifier, loadEventEntry) = generateSpecificEvent(
            TripEvents.LoadEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameDeparture,
            Milestone.START.toString().ReCapitalize()
        )
        eventsAtLocation[loadEventIdentifier] = generatedPInameDeparture
        eventsAtCities[loadEventIdentifier] = generatedDepartureCity
        eventsAtCountries[loadEventIdentifier] = generatedDepartureCountry
        constructedTTL += digitalTwinTransportMeanEntry + loadEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry + loadEventEntry)

        //  2. generateDepartureEvent(Planned)
        val (digitalTwinTransportMeanIdentifier2, digitalTwinTransportMeanEntry2) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (plannedDepartureEventIdentifier, plannedDepartureEventEntry) = generateSpecificEvent(
            TripEvents.DepartureEvent,
            EventType.PLANNED.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier2,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameDeparture,
            Milestone.END.toString().ReCapitalize()
        )
        eventsAtLocation[plannedDepartureEventIdentifier] = generatedPInameDeparture
        eventsAtCities[plannedDepartureEventIdentifier] = generatedDepartureCity
        eventsAtCountries[plannedDepartureEventIdentifier] = generatedDepartureCountry
        constructedTTL += digitalTwinTransportMeanEntry2 + plannedDepartureEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry2 + plannedDepartureEventEntry)

        //  3. generateArrivalEvent(Planned)
        val (digitalTwinTransportMeanIdentifier3, digitalTwinTransportMeanEntry3) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (plannedArrivalEventIdentifier, plannedArrivalEventEntry) = generateSpecificEvent(
            TripEvents.ArrivalEvent,
            EventType.PLANNED.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier3,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameArrival,
            Milestone.START.toString().ReCapitalize()
        )
        eventsAtLocation[plannedArrivalEventIdentifier] = generatedPInameArrival
        eventsAtCities[plannedArrivalEventIdentifier] = generatedArrivalCity
        eventsAtCountries[plannedArrivalEventIdentifier] = generatedArrivalCountry
        constructedTTL += digitalTwinTransportMeanEntry3 + plannedArrivalEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry3 + plannedArrivalEventEntry)

        //  4. generateDepartureEvent(Actual)
        val (digitalTwinTransportMeanIdentifier4, digitalTwinTransportMeanEntry4) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (actualDepartureEventIdentifier, actualDepartureEventEntry) = generateSpecificEvent(
            TripEvents.DepartureEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier4,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameDeparture,
            Milestone.END.toString().ReCapitalize()
        )
        eventsAtLocation[actualDepartureEventIdentifier] = generatedPInameDeparture
        eventsAtCities[actualDepartureEventIdentifier] = generatedDepartureCity
        eventsAtCountries[actualDepartureEventIdentifier] = generatedDepartureCountry
        constructedTTL += digitalTwinTransportMeanEntry4 + actualDepartureEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry4 + actualDepartureEventEntry)

        //  5. generateBorderCrossingEvents()
        //  5.0. generate timestamp for both events
        val borderCrossingTimestamps = generateTimestamps()
        //  5.1. arrival at border - NETHERLANDS
        val (digitalTwinTransportMeanIdentifier5, digitalTwinTransportMeanEntry5) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (arrivalBorderEventIdentifier, arrivalBorderEventEntry) = generateSpecificEvent(
            TripEvents.ArrivalEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier5,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameBorderNetherlands,
            Milestone.START.toString().ReCapitalize(),
            borderCrossingTimestamps
        )
        eventsAtLocation[arrivalBorderEventIdentifier] = generatedPInameBorderNetherlands
        eventsAtCities[arrivalBorderEventIdentifier] = generatedPInameBorderNetherlands
        eventsAtCountries[arrivalBorderEventIdentifier] = generatedBorderCountryNL
        constructedTTL += digitalTwinTransportMeanEntry5 + arrivalBorderEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry5 + arrivalBorderEventEntry)

        //  5.2. departure from border - DEUTSCHLAND
        val (digitalTwinTransportMeanIdentifier6, digitalTwinTransportMeanEntry6) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (departureBorderEventIdentifier, departureBorderEventEntry) = generateSpecificEvent(
            TripEvents.DepartureEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier6,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameBorderGermany,
            Milestone.END.toString().ReCapitalize(),
            borderCrossingTimestamps
        )
        eventsAtLocation[departureBorderEventIdentifier] = generatedPInameBorderGermany
        eventsAtCities[departureBorderEventIdentifier] = generatedPInameBorderGermany
        eventsAtCountries[departureBorderEventIdentifier] = generatedBorderCountryDE
        constructedTTL += digitalTwinTransportMeanEntry6 + departureBorderEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry6 + departureBorderEventEntry)

        //  6. generateArrivalEvent(Actual)
        val (digitalTwinTransportMeanIdentifier7, digitalTwinTransportMeanEntry7) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (actualArrivalEventIdentifier, actualArrivalEventEntry) = generateSpecificEvent(
            TripEvents.ArrivalEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier7,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameArrival,
            Milestone.START.toString().ReCapitalize()
        )
        eventsAtLocation[actualArrivalEventIdentifier] = generatedPInameArrival
        eventsAtCities[actualArrivalEventIdentifier] = generatedArrivalCity
        eventsAtCountries[actualArrivalEventIdentifier] = generatedArrivalCountry
        constructedTTL += digitalTwinTransportMeanEntry7 + actualArrivalEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry7 + actualArrivalEventEntry)

        //  7. generateDischargeEvent()
        val (digitalTwinTransportMeanIdentifier8, digitalTwinTransportMeanEntry8) = generateDigitalTwinTransportMeans(transportMeansCode)
        val (dischargeEventIdentifier, dischargeEventEntry) = generateSpecificEvent(
            TripEvents.DischargeEvent,
            EventType.ACTUAL.toString().ReCapitalize(),
            digitalTwinTransportMeanIdentifier8,
            generatedGoodsIdentifier,
            generatedBusinessTransaction,
            generatedPInameArrival,
            Milestone.END.toString().ReCapitalize()
        )
        eventsAtLocation[dischargeEventIdentifier] = generatedPInameArrival
        eventsAtCities[dischargeEventIdentifier] = generatedArrivalCity
        eventsAtCountries[dischargeEventIdentifier] = generatedArrivalCountry
        constructedTTL += digitalTwinTransportMeanEntry8 + dischargeEventEntry
        eventEntries.add(prefixes + digitalTwinTransportMeanEntry8 + dischargeEventEntry)

        val transportMeansIdentifiers = listOf(digitalTwinTransportMeanEntry, digitalTwinTransportMeanEntry2,
            digitalTwinTransportMeanEntry3, digitalTwinTransportMeanEntry4,
            digitalTwinTransportMeanEntry5, digitalTwinTransportMeanEntry6,
            digitalTwinTransportMeanEntry7, digitalTwinTransportMeanEntry8)

        val eventsIdentifiers = listOf(loadEventIdentifier, plannedDepartureEventIdentifier,
            plannedArrivalEventIdentifier, actualDepartureEventIdentifier, arrivalBorderEventIdentifier,
            departureBorderEventIdentifier, actualArrivalEventIdentifier, dischargeEventIdentifier)

        return TripTTL(constructedTTL, eventsIdentifiers, eventsAtLocation, transportMeansIdentifiers, eventsAtCities, eventsAtCountries, eventEntries, helperItems)
    }

    private fun generateEventHelperItems(): TripHelperItems {
        // I.generate goods
        val goodsCode = RandomUtils.nextInt(0, 9999999)
        val goodsWeight = RandomUtils.nextInt(0, 9999999)
        val (generatedGoodsIdentifier, generatedGoodsEntry) = generateGoods(goodsCode, goodsWeight)

        val tripLocations = mutableListOf<PILocation>()

        // II.(NL) generate Departure location - unpack it
        val generatedLocationDeparture = generateLocation("NL")
        val generatedDepartureCity = generatedLocationDeparture.first.first
        val generatedDepartureCountry = generatedLocationDeparture.first.second
        val generatedPInameDeparture = generatedLocationDeparture.second.first
        val generatedPIentryDeparture = generatedLocationDeparture.second.second

        val departurePILocation = PILocation(generatedPInameDeparture, generatedPIentryDeparture, generatedDepartureCity, generatedDepartureCountry)
        tripLocations.add(departurePILocation)

        // III.(DE) generate Arrival location - unpack it
        val generatedLocationArrival = generateLocation("DE")
        val generatedArrivalCity = generatedLocationArrival.first.first
        val generatedArrivalCountry = generatedLocationArrival.first.second
        val generatedPInameArrival = generatedLocationArrival.second.first
        val generatedPIentryArrival = generatedLocationArrival.second.second

        val arrivalPILocation = PILocation(generatedPInameArrival, generatedPIentryArrival, generatedArrivalCity, generatedArrivalCountry)
        tripLocations.add(arrivalPILocation)

        // IV. generate the border location Veldhuizen

        val generatedPInameBorderNetherlands = "VeldhuizenNL"
        val generatedPInameBorderGermany = "VeldhuizenDE"

        // IV.1.NETHERLANDS
        val generatedLocationBorderNetherlands = generateLocation("NL", generatedPInameBorderNetherlands, generatedPInameBorderNetherlands)
        val generatedBorderCityNL = generatedLocationBorderNetherlands.first.first
        val generatedBorderCountryNL = generatedLocationBorderNetherlands.first.second
        val generatedPIentryBorderNL = generatedLocationBorderNetherlands.second.second

        val borderPILocationNL = PILocation(generatedPInameBorderNetherlands, generatedPIentryBorderNL, generatedBorderCityNL, generatedBorderCountryNL)
        tripLocations.add(borderPILocationNL)

        // generate the border location Veldhuizen - DEUTSCHLAND
        val generatedLocationBorderGermany = generateLocation("DE", generatedPInameBorderGermany, generatedPInameBorderGermany)
        val generatedBorderCityDE = generatedLocationBorderGermany.first.first
        val generatedBorderCountryDE = generatedLocationBorderGermany.first.second
        val generatedPIentryBorderDE = generatedLocationBorderGermany.second.second

        val borderPILocationDE = PILocation(generatedPInameBorderGermany, generatedPIentryBorderDE, generatedBorderCityDE, generatedBorderCountryDE)
        tripLocations.add(borderPILocationDE)

        // generate legal person and business transaction
        val (generatedLegalPerson, generatedLegalPersonEntry) = generateLegalPerson()
        val (generatedBusinessTransaction, generatedBusinessTransactionEntry) =
            generateBusinessTransaction(generatedLegalPerson)

        val helperItemsTTL = prefixes +
            generatedGoodsEntry +
            generatedLegalPersonEntry +
            generatedPIentryDeparture +
            generatedPIentryArrival +
            generatedPIentryBorderNL +
            generatedPIentryBorderDE +
            generatedBusinessTransactionEntry

        val transportMeansCode = RandomUtils.nextInt(0, 9999999)

        return TripHelperItems(helperItemsTTL, generatedGoodsIdentifier, generatedBusinessTransaction, tripLocations, transportMeansCode)
    }

    /**
     * Generate an event ttl entry, return a pair of the event identifier and the ttl entry
     * @param digitalTwinIdentifierTransportMeans The UUID of the digital twin of the transport means
     * @param generatedBusinessTransaction The UUID of the business transaction
     * @param generatedEquipment The UUID of the equipment
     * @param locationName The name of the location
     *
     */
    private fun generateEvent(digitalTwinIdentifierTransportMeans: String,
                              generatedBusinessTransaction: String, generatedEquipment: String,
                              locationName: String, milestonePosition: Int): Pair<String, String> {
        val generatedEventIdentifier = UUID.randomUUID().toString()
        val (hasTimestamp, hasSubmissionTimestamp) = generateTimestamps()
        val generatedEventEntry = """
        ex:Event-$generatedEventIdentifier a Event:Event, owl:NamedIndividual;
          Event:hasTimestamp ${hasTimestamp}^^xsd:dateTime;
          Event:hasDateTimeType Event:${generateDateTimeType()};
          Event:involvesDigitalTwin ex:dt-$digitalTwinIdentifierTransportMeans, ex:Equipment-$generatedEquipment;
          Event:involvesBusinessTransaction ex:businessTransaction-$generatedBusinessTransaction;
          Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-$locationName;
          Event:hasMilestone Event:${generateHasMilestone(milestonePosition)};
          Event:hasSubmissionTimestamp ${hasSubmissionTimestamp}^^xsd:dateTime .
            """
        return Pair(generatedEventIdentifier, generatedEventEntry)
    }

    /**
     * Generate an event ttl entry, return a pair of the event identifier and the ttl entry
     * @param eventType can be one of: LoadEvent, DepartureEvent, BorderCrossingEvent, ArrivalEvent, DischargeEvent
     * @param eventDateTimeType can be either Planned or Actual (we don't deal with estimate yet)
     * @param transportMeansIdentifier every event has its own transport means with a distinct
     * @param generatedBusinessTransaction The UUID of the business transaction
     * @param goodsIdentifier the UUID of the goods
     * @param locationName The name of the location
     * @param milestone can be either Start or End
     * @param timestamps if provided the event will be specified with the timestamps, first hasTimestamp, second hasSubmissionTimestamp
     *
     */
    private fun generateSpecificEvent(eventType: TripEvents,
                                      eventDateTimeType: String,
                                      transportMeansIdentifier: String,
                                      goodsIdentifier: String, generatedBusinessTransaction: String,
                                      locationName: String,
                                      milestone: String,
                                      timestamps: Pair<String, String> = Pair("", "")): Pair<String, String> {
        val generatedEventIdentifier = UUID.randomUUID().toString()
        val (hasTimestamp, hasSubmissionTimestamp) = if (timestamps.first == "") generateTimestamps() else timestamps
        val genericEventEntry = """
        ex:Event-$generatedEventIdentifier a Event:$eventType, owl:NamedIndividual;
          Event:hasTimestamp ${hasTimestamp}^^xsd:dateTime;
          Event:hasDateTimeType Event:$eventDateTimeType;
          Event:involvesBusinessTransaction ex:businessTransaction-$generatedBusinessTransaction;
          Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-$locationName;
          Event:hasMilestone Event:$milestone;
          Event:hasSubmissionTimestamp ${hasSubmissionTimestamp}^^xsd:dateTime;"""
        val specificEventEntry = when (eventType) {
            in listOf(TripEvents.LoadEvent, TripEvents.DischargeEvent) -> """
          Event:involvesDigitalTwin ex:dt-$transportMeansIdentifier, ex:Goods-$goodsIdentifier.
        """

            else -> """
          Event:involvesDigitalTwin ex:dt-$transportMeansIdentifier.
        """
        }
        val generatedEventEntry = genericEventEntry + specificEventEntry
        return Pair(generatedEventIdentifier, generatedEventEntry)
    }

    private fun generateDigitalTwinTransportMeans(transportMeansIdentifier: Int): Pair<String, String> {
        val digitalTwinIdentifier = UUID.randomUUID().toString()
        return Pair(digitalTwinIdentifier, """
        ex:dt-$digitalTwinIdentifier a dt:TransportMeans, owl:NamedIndividual, dt:Vessel;
          rdfs:label "Vessel";
          dt:hasVIN "$transportMeansIdentifier";
          dt:hasTransportMeansID "$transportMeansIdentifier" .
            """)
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

    private fun generateLocation(countryCode: String, name: String = "", cityName: String = ""): Pair<Pair<String, String>, Pair<String, String>> {
        // generate random 5-char location name
        val charPool = ('A'..'Z')
        val piName = if (name.isBlank()) charPool.random(5) else name

        // both countries have equal # cities => we use 1 indexer
        val cityIndex = RandomUtils.nextInt(0, NetherlandsCities.values().count())

        val piCity = if (countryCode == "NL" && cityName.isBlank()) NetherlandsCities.values()[cityIndex].toString()
        else if (countryCode == "DE" && cityName.isBlank()) GermanCities.values()[cityIndex].toString()
        else cityName

        return Pair(Pair(piCity, countryCode), Pair(piName, """
        ex:PhysicalInfrastructure-$piName a pi:Location, owl:NamedIndividual;
            pi:cityName "$piCity" ;
            pi:countryName "$countryCode" .
        """))
    }


    private fun generateEquipment(): Pair<String, String> {
        val equipmentIdentifier = UUID.randomUUID().toString()
        return Pair(equipmentIdentifier, """
        ex:Equipment-$equipmentIdentifier a dt:Equipment, owl:NamedIndividual;
          rdfs:label "TNO-test092022" .
            """)
    }

    private fun generateGoods(goodsCode: Int, goodsWeight: Int): Pair<String, String> {
        val goodsIdentifier = UUID.randomUUID().toString()
        return Pair(goodsIdentifier, """
        ex:Goods-$goodsIdentifier a dt:Goods, owl:NamedIndividual;
          dt:goodsDescription "a container";
          dt:GoodsTypeCode "$goodsCode";
          dt:goodsWeight $goodsWeight;
          dt:grossMass ${goodsWeight + 500} .
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

        val hasTimestamp = "\"$stringDate\""
        val hasSubmissionTimestamp = "\"$stringDateSubmission\""

        return Pair(hasTimestamp, hasSubmissionTimestamp)
    }


    // HELPER FUNCTIONS BELOW
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

    private fun CharRange.random(numberOfChars: Int): String {
        var randomString = ""
        for (index in 0 until numberOfChars) {
            val firstCharChoice = RandomUtils.nextInt(0, this.count())
            randomString += this.elementAt(firstCharChoice)
        }

        return randomString
    }
}

enum class NetherlandsCities {
    TheHague,
    Amsterdam,
    Utrecht,
    Rotterdam,
    Eindhoven,
    Almere,
    Groningen,
    Breda,
    Apeldoorn,
    Haarlem,
    Zaanstad,
    Arnhem,
    DenBosch,
    Leeuwarden,
    Maastricht,
}

enum class GermanCities {
    Berlin,
    Hamburg,
    Munich,
    Cologne,
    Frankfurt,
    Bremen,
    Dusseldorf,
    Stuttgart,
    Leipzig,
    Dortmund,
    Essen,
    Dresden,
    Hannover,
    Nuremberg,
    Duisburg
}

enum class TripEvents {
    LoadEvent,
    DepartureEvent,
    ArrivalEvent,
    DischargeEvent
}