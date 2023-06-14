package nl.tno.federated.corda.services.data.fetcher

import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import java.io.StringWriter

class SPARQLDataFetcher : DataFetcher {

    private val url = "http://localhost:7200/repositories/default"
    private val repository = SPARQLRepository(url)

    override fun fetch(input: String): String? {
        return executeSPARQL(input)
    }

    private fun executeSPARQL(sparql: String): String? {
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