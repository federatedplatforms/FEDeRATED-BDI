package nl.tno.federated.api.event


//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@RunWith(SpringRunner::class)
class EventMapperTest {

//    @Autowired
//    private lateinit var eventMapper: EventMapper
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper

//    val loadEvent = LoadEvent(
//        hasSubmissionTimestamp = OffsetDateTime.now(),
//        hasTimestamp = OffsetDateTime.now(),
//        hasMilestone = true,
//        hasDateTimeType = true,
//        involvesDigitalTwin = listOf(
//            LoadEventInvolvesDigitalTwin(
//                goodsDescription = "goods",
//                goodsWeight = 12,
//                goodsTypeCode = listOf(),
//                hasGrossMass = listOf()
//            )
//        )
//    )
//
//    @Test
//    fun rdfToJsonLD() {
//        val rdfTurtle = eventMapper.toRDFTurtle(loadEvent)
//
//        println(rdfTurtle)
//
//
//
//
//        val ttl = """
//            @base <http://example.com/base/> .
//            @prefix ns0: <https://ontology.tno.nl/logistics/federated/Event#> .
//
//            _:0 a ns0:LoadEvent;
//              ns0:hasDateTimeType "true";
//              ns0:hasMilestone "true";
//              ns0:hasSubmissionTimestamp "2023-05-02T10:21:25.779+02:00";
//              ns0:hasTimestamp "2023-05-02T10:21:25.787+02:00" .
//        """.trimIndent()
//
//        val jsonLd = convert(rdfTurtle, RDFFormat.TURTLE, RDFFormat.JSONLD)
//
//        println(jsonLd)
//
//        val options = JsonLdOptions()
//        options.base = "https://ontology.tno.nl/logistics/federated/Event"
//        options.compactArrays = true
//
//        val compacted = jsonLd.byteInputStream().use {
//            // https://github.com/jsonld-java/jsonld-java
//            val compact = JsonLdProcessor.compact(JsonUtils.fromInputStream(jsonLd.byteInputStream()), HashMap<Any, Any>(), options)
//            JsonUtils.toPrettyString(compact)
//        }
//
//        println(compacted)
//    }
//
//    @Test
//    fun `test LoadEvent mapping`() {
//
//        val test = """
//            {
//    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" : [
//      {
//        "value" : "https://ontology.tno.nl/logistics/federated/Event#LoadEvent",
//        "type" : "uri"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/Event#hasDateTimeType" : [
//      {
//        "value" : "true",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/Event#hasMilestone" : [
//      {
//        "value" : "true",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/Event#hasSubmissionTimestamp" : [
//      {
//        "value" : "2023-05-01T15:22:33.471+02:00",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/Event#hasTimestamp" : [
//      {
//        "value" : "2023-05-01T15:22:33.477+02:00",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/Event#involvesDigitalTwin" : [
//      {
//        "value" : "_:genid-ac0c9aa627f74f7fbb833131b76f31cc-1",
//        "type" : "bnode"
//      }
//    ]
//  },
//  "_:genid-ac0c9aa627f74f7fbb833131b76f31cc-1" : {
//    "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" : [
//      {
//        "value" : "https://ontology.tno.nl/logistics/federated/DigitalTwin#Goods",
//        "type" : "uri"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/DigitalTwin#goodsDescription" : [
//      {
//        "value" : "goods",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ],
//    "https://ontology.tno.nl/logistics/federated/DigitalTwin#goodsWeight" : [
//      {
//        "value" : "12",
//        "type" : "literal",
//        "datatype" : "http://www.w3.org/2001/XMLSchema#string"
//      }
//    ]
//  }
//
//        """.trimIndent()
//
////        val readValue = objectMapper.readValue<LoadEvent>(test)
////
//
//
//
//
////            ),
////            involvesBusinessTransaction = listOf(
////                LoadEventInvolvesBusinessTransaction(
////                    partOfTransportMovementType = listOf(),
////                    involvedActor = listOf()
////                )
////            ),
////            involvesPhysicalInfrastructure = listOf(
////                element = LoadEventInvolvesPhysicalInfrastructure(
////                    cityLoCode = listOf(),
////                    postalCode = listOf(),
////                    lat = listOf(),
////                    long = listOf()
////                )
////            )
//
//
//        val rdfTurtle = eventMapper.toRDFTurtle(loadEvent)
//        val rdfNQuad = convert(rdfTurtle, RDFFormat.TURTLE, RDFFormat.NQUADS)
//        val jsonLd = convert(rdfTurtle, RDFFormat.TURTLE, RDFFormat.JSONLD)
//
//        val options = JsonLdOptions()
//        options.base = "https://ontology.tno.nl/logistics/federated/Event"
//        options.compactArrays = true
//
//        val newContexts: MutableList<String> = mutableListOf<String>()
////        newContexts.add("http://schema.org/")
//        val compacted = JsonLdProcessor.compact(jsonLd, newContexts, options)
//        JsonLdProcessor.compact(jsonLd, HashMap<Any, Any>(), JsonLdOptions())
//
////        JsonDocument.of()
////
////        JsonLd.compact("https://ontology.tno.nl/logistics/federated/Event", ).get()
//
//        println(rdfTurtle)
//
//        val of = RdfDocument.of(rdfNQuad.byteInputStream())
//
//        val fromRdf = JsonLd.fromRdf(of)
//
//
////        eventMapper.fromRDFTurtle(rdfTurtle, LoadEvent::class.java)
//
//        val convert = convert(rdfTurtle, RDFFormat.TURTLE, RDFFormat.JSONLD)
//        val flattenJsonLD = flattenJsonLD(convert)
//        val compactJsonLD = compactJsonLD(flattenJsonLD)
//        val convertedLoadEvent = eventMapper.fromRDFTurtle(rdfTurtle, LoadEvent::class.java)
//        println(convertedLoadEvent)
//    }
}