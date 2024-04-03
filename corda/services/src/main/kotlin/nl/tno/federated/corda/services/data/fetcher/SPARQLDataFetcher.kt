package nl.tno.federated.corda.services.data.fetcher

import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import java.io.StringWriter
import java.util.*

class SPARQLDataFetcher(private val properties: Properties) : DataFetcher {

    private val repository by lazy { SPARQLRepository(properties.getProperty("graphdb.sparql.url")) }

    override fun fetch(input: String): String {
        return executeSPARQL(input)
    }

    private fun executeSPARQL(sparql: String): String {
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