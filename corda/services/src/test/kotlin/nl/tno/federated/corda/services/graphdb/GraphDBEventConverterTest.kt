package nl.tno.federated.corda.services.graphdb

import org.junit.Test
import kotlin.test.assertEquals

class GraphDBEventConverterTest {

    private val eventConverter = GraphDBEventConverter

    @Test
    fun `parse country`() {
        val testRdfEvent = """
        @base <http://example.com/base/> . 
        @prefix owl: <http://www.w3.org/2002/07/owl#> . 
        @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix data: <http://example.com/base#> .
        @prefix ex: <http://example.com/base#> . 
        @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        @prefix time: <http://www.w3.org/2006/time#> . 
        @prefix data: <http://example.com/base#> .
        @prefix ex: <http://example.com/base#> . 
        @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . 
        @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .
        @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> .
        @prefix dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .
        @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> .
        
                ex:LegalPerson-Qjvtcz a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;
                  businessService:actorName "Qjvtcz" .
                    
                ex:Equipment-3d623de9-aa2c-4f76-b4e8-6685a66d63e3 a dt:Equipment, owl:NamedIndividual;
                  rdfs:label "TNO-test092022" .
                    
                ex:businessTransaction-dace89b9-b771-4de6-86ec-4d0dc702a348 a businessService:Consignment, owl:NamedIndividual;
                  businessService:consignmentCreationTime "2022-01-01T00:01:00"^^xsd:dateTime;
                  businessService:involvedActor ex:LegalPerson-Qjvtcz .
                    
                ex:PhysicalInfrastructure-ZULZW a pi:Location, owl:NamedIndividual;
                    pi:cityName "Maastricht" ;
                    pi:countryName "NL" .
                 
                ex:dt-cc145613-3825-4de2-84d4-475ac295f498 a dt:TransportMeans, owl:NamedIndividual, dt:Vessel;
                  rdfs:label "Vessel";
                  dt:hasVIN "1879236";
                  dt:hasTransportMeansID "1879236" .
                     
                ex:Event-a2f4c4fd-f9ac-40f1-ac9e-cd08e1b352fb a Event:Event, owl:NamedIndividual;
                  Event:hasTimestamp "2018-07-28T00:08:04Z"^^xsd:dateTime;
                  Event:hasDateTimeType Event:Planned;
                  Event:involvesDigitalTwin ex:dt-cc145613-3825-4de2-84d4-475ac295f498, ex:Equipment-3d623de9-aa2c-4f76-b4e8-6685a66d63e3;
                  Event:involvesBusinessTransaction ex:businessTransaction-dace89b9-b771-4de6-86ec-4d0dc702a348;
                  Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-ZULZW;
                  Event:hasMilestone Event:Start;
                  Event:hasSubmissionTimestamp "2018-06-28T00:08:04Z"^^xsd:dateTime .
        """.trimIndent()

        val parsedCountry = eventConverter.getCountryCodesFromRDFEventData(testRdfEvent)

        assertEquals("NL", parsedCountry.single())
    }
    @Test
    fun `parse event IDs`() {
        val testRdfEvent = """
        @base <http://example.com/base/> . 
        @prefix owl: <http://www.w3.org/2002/07/owl#> . 
        @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
        @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
        @prefix data: <http://example.com/base#> .
        @prefix ex: <http://example.com/base#> . 
        @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
        @prefix time: <http://www.w3.org/2006/time#> . 
        @prefix data: <http://example.com/base#> .
        @prefix ex: <http://example.com/base#> . 
        @prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . 
        @prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .
        @prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> .
        @prefix dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .
        @prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> .
        
                ex:LegalPerson-Qjvtcz a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise;
                  businessService:actorName "Qjvtcz" .
                    
                ex:Equipment-3d623de9-aa2c-4f76-b4e8-6685a66d63e3 a dt:Equipment, owl:NamedIndividual;
                  rdfs:label "TNO-test092022" .
                    
                ex:businessTransaction-dace89b9-b771-4de6-86ec-4d0dc702a348 a businessService:Consignment, owl:NamedIndividual;
                  businessService:consignmentCreationTime "2022-01-01T00:01:00"^^xsd:dateTime;
                  businessService:involvedActor ex:LegalPerson-Qjvtcz .
                    
                ex:PhysicalInfrastructure-ZULZW a pi:Location, owl:NamedIndividual;
                    pi:cityName "Maastricht" ;
                    pi:countryName "NL" .
                 
                ex:dt-cc145613-3825-4de2-84d4-475ac295f498 a dt:TransportMeans, owl:NamedIndividual, dt:Vessel;
                  rdfs:label "Vessel";
                  dt:hasVIN "1879236";
                  dt:hasTransportMeansID "1879236" .
                     
                ex:Event-a2f4c4fd-f9ac-40f1-ac9e-cd08e1b352fb a Event:Event, owl:NamedIndividual;
                  Event:hasTimestamp "2018-07-28T00:08:04Z"^^xsd:dateTime;
                  Event:hasDateTimeType Event:Planned;
                  Event:involvesDigitalTwin ex:dt-cc145613-3825-4de2-84d4-475ac295f498, ex:Equipment-3d623de9-aa2c-4f76-b4e8-6685a66d63e3;
                  Event:involvesBusinessTransaction ex:businessTransaction-dace89b9-b771-4de6-86ec-4d0dc702a348;
                  Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-ZULZW;
                  Event:hasMilestone Event:Start;
                  Event:hasSubmissionTimestamp "2018-06-28T00:08:04Z"^^xsd:dateTime .
        """.trimIndent()

        val parsedIDs = eventConverter.parseRDFToEventIDs(testRdfEvent)

        assertEquals("a2f4c4fd-f9ac-40f1-ac9e-cd08e1b352fb", parsedIDs.single())
    }
}