package nl.tno.federated.api.event.distribution.rules

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import org.eclipse.rdf4j.model.vocabulary.FOAF
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SparqlEventDistributionRuleTest {

    private val rdfEventString = """
        @prefix ex: <http://example.org/> .
        @prefix foaf: <http://xmlns.com/foaf/0.1/> .
        
        ex:Stephan a ex:Developer ;
                   foaf:firstName "Stephan" ;
                   foaf:surname "Oudmaijer";
                   ex:homeAddress _:node1 .
        
        _:node1 ex:street "Tempeltuinlaan 8" ;
                ex:city "Vleuten" ;
                ex:country "The Netherlands" .
    """.trimIndent()

    private val sparql = """
        PREFIX foaf: <${FOAF.NAMESPACE}>
   
        ASK  { ?x foaf:surname  "Oudmaijer" }
    """.trimIndent()

    private val destinations = listOf(CordaEventDestination(CordaX500Name("TNO", "Den Haag", "NL")))

    private val rule = SparqlEventDistributionRule(sparql = sparql, destinations = destinations)


    @Test
    fun getDestinations() {
        assertEquals(destinations, rule.getDestinations())
    }

    @Test
    fun appliesTo() {
        assertTrue { rule.appliesTo(rdfEventString) }
    }
}