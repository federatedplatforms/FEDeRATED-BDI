package nl.tno.federated.api.graphdb

import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import java.io.StringWriter

class GraphDBSPARQLClient(url: String) {

    private val repository = SPARQLRepository(url)

    fun executeSPARQL(sparql: String): String {
        return repository.connection.use {
            val query: TupleQuery = it.prepareTupleQuery(sparql)
            val sw = StringWriter()
            sw.use {
                query.evaluate(SPARQLResultsJSONWriter(it))
            }
            sw.toString()
        }
    }
}