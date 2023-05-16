package nl.tno.federated.api.util

import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.InputStream
import java.io.StringWriter

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class InvalidRDFException(message: String?, e: Exception) : Exception(message, e)

object RDFUtils {

    val log = LoggerFactory.getLogger(RDFUtils::class.java)

    fun isValidRDF(input: String, format: RDFFormat): Boolean {
        return isValidRDF(input.byteInputStream(), format)
    }

    fun isValidRDF(input: InputStream, format: RDFFormat): Boolean {
        return try {
            input.use {
                val model = Rio.parse(input, format)
                // assert a certain event here?
                log.debug(model.toString())
            }
            true
        } catch (e: Exception) {
            throw InvalidRDFException(e.message, e)
        }
    }

    fun convert(input: String, inputFormat: RDFFormat, outputFormat: RDFFormat): String {
        input.byteInputStream().use {
            return with(StringWriter()) {
                val writer = Rio.createWriter(outputFormat, this)
                val model = Rio.parse(it, inputFormat)
                Rio.write(model, writer)
                toString()
            }
        }
    }
}

fun main() {
    val event = """@base <http://example.com/base/> .
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

ex:dt-42f17294-712e-460d-818e-7a01866055f8 a dt:Goods, owl:NamedIndividual .
ex:LegalPerson-Phzche a businessService:LegalPerson, owl:NamedIndividual, businessService:PrivateEnterprise .
ex:PhysicalInfrastructure-TVODL a pi:Location, owl:NamedIndividual ;
                                pi:countryName "DE".
ex:businessTransaction-c448f2b2-9954-49f4-85f3-9a2731516764 a businessService:Consignment, owl:NamedIndividual .
ex:dt-5c92dc09-535c-4060-af77-58422000a7b3 a dt:TransportMeans, owl:NamedIndividual, dt:Vessel .
ex:Event-1d59ee2c-eb75-4556-89ff-6c01c4962e4a a Event:LoadEvent, owl:NamedIndividual;
                                              Event:hasTimestamp "2022-12-21T09:01:09Z"^^xsd:dateTime;
                                              Event:hasDateTimeType Event:Actual;
                                              Event:hasMilestone Event:Start;
                                              Event:hasSubmissionTimestamp "2022-12-21T09:01:09Z"^^xsd:dateTime;
                                              Event:involvesPhysicalInfrastructure ex:PhysicalInfrastructure-TVODL;
                                              Event:involvesDigitalTwin ex:dt-5c92dc09-535c-4060-af77-58422000a7b3, ex:dt-42f17294-712e-460d-818e-7a01866055f8."""

    RDFUtils.isValidRDF(event, RDFFormat.TURTLE)
}