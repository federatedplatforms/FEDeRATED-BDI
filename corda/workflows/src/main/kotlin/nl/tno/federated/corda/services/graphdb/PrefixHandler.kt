package nl.tno.federated.services

object PrefixHandlerTTLGenerator {
    private val basePrefix = "@base <http://example.com/base/> . \n"

    private val examplePrefix = "@prefix data: <http://example.com/base#> .\n" +
        "@prefix ex: <http://example.com/base#> . \n"

    private val semanticElementsPrefixes = "@prefix owl: <http://www.w3.org/2002/07/owl#> . \n" +
        "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .\n" +
        "@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n"

    private val ttlGeneratorPrefixes = basePrefix + semanticElementsPrefixes + examplePrefix +
        "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n" +
        "@prefix time: <http://www.w3.org/2006/time#> . \n"

    private val prefixOntologyObjectsMap = mapOf(
        OntologyObjects.PhysicalInfrastructure to "@prefix pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> .\n",
        OntologyObjects.Event to "@prefix Event: <https://ontology.tno.nl/logistics/federated/Event#> . \n",
        OntologyObjects.BusinessService to "@prefix businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> .\n",
        OntologyObjects.DigitalTwin to "@prefix dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> .\n",
        OntologyObjects.Classifications to "@prefix classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> .\n"
    )

    fun getPrefixesTTLGenerator(): String {
        return ttlGeneratorPrefixes + examplePrefix +
            prefixOntologyObjectsMap[OntologyObjects.Event] +
            prefixOntologyObjectsMap[OntologyObjects.PhysicalInfrastructure] +
            prefixOntologyObjectsMap[OntologyObjects.BusinessService] +
            prefixOntologyObjectsMap[OntologyObjects.DigitalTwin] +
            prefixOntologyObjectsMap[OntologyObjects.Classifications]
    }
}

object PrefixHandlerQueries {
    private val semanticElementsPrefixes = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "

    private val prefixOntologyObjectsMap = mapOf(
        OntologyObjects.PhysicalInfrastructure to "PREFIX pi: <https://ontology.tno.nl/logistics/federated/PhysicalInfrastructure#> ",
        OntologyObjects.Event to "PREFIX Event: <https://ontology.tno.nl/logistics/federated/Event#> ",
        OntologyObjects.BusinessService to "PREFIX businessService: <https://ontology.tno.nl/logistics/federated/BusinessService#> ",
        OntologyObjects.DigitalTwin to "PREFIX dt: <https://ontology.tno.nl/logistics/federated/DigitalTwin#> ",
        OntologyObjects.Classifications to "PREFIX classifications: <https://ontology.tno.nl/logistics/federated/Classifications#> "
    )

    fun getPrefixesEvent(): String {
        return prefixOntologyObjectsMap[OntologyObjects.Event]!!
    }

    fun getPrefixesDigitalTwin(): String {
        return prefixOntologyObjectsMap[OntologyObjects.DigitalTwin]!!
    }

    fun getPrefixesSemanticElements(): String {
        return semanticElementsPrefixes
    }
}

enum class OntologyObjects {
    PhysicalInfrastructure,
    Event,
    BusinessService,
    DigitalTwin,
    Classifications
}