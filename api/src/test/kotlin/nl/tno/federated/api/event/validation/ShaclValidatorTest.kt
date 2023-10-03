package nl.tno.federated.api.event.validation

import org.junit.Test

class ShaclValidatorTest {

    @Test
    fun validate() {
        val shape = """
            @prefix ex: <http://example.org#> .
            @prefix dash: <http://datashapes.org/dash#> .
            @prefix sh: <http://www.w3.org/ns/shacl#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            
            ex:PersonShape a sh:NodeShape ;
                sh:targetClass foaf:Person ;
                sh:property [
                   sh:path foaf:birthday ;
                   sh:datatype xsd:date ;
                ] .
        """.trimIndent()

        val rdf = """
            @prefix laureate: <http://data.nobelprize.org/resource/laureate/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            
            laureate:935
                a                foaf:Person ;
                foaf:birthday    "1948-10-09"^^xsd:date ;
                foaf:familyName  "Hart" ;
                foaf:givenName   "Oliver" ;
                foaf:name        "Oliver Hart" ;
                foaf:gender      "male" .     
        """.trimIndent()

        val validator = ShaclValidator(listOf(shape))
        validator.validate(rdf)
    }

    @Test(expected = ShaclValidationException::class)
    fun validateExceptionThrown() {
        val shape = """
            @prefix ex: <http://example.org#> .
            @prefix dash: <http://datashapes.org/dash#> .
            @prefix sh: <http://www.w3.org/ns/shacl#> .
            @prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            
            ex:PersonShape a sh:NodeShape ;
                sh:targetClass foaf:Person ;
                sh:property [
                   sh:path foaf:birthday ;
                   sh:datatype xsd:string ;
                ] .
        """.trimIndent()

        val rdf = """
            @prefix laureate: <http://data.nobelprize.org/resource/laureate/> .
            @prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .
            @prefix foaf:  <http://xmlns.com/foaf/0.1/> .
            
            laureate:935
                a                foaf:Person ;
                foaf:birthday    "1948-10-09"^^xsd:date ;
                foaf:familyName  "Hart" ;
                foaf:givenName   "Oliver" ;
                foaf:name        "Oliver Hart" ;
                foaf:gender      "male" .     
        """.trimIndent()

        val validator = ShaclValidator(listOf(shape))
        validator.validate(rdf)
    }

}