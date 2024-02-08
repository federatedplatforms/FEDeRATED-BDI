package nl.tno.federated.corda.services.data.fetcher

import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.util.*

class SPARQLDataFetcher : DataFetcher {

    private val repository by lazy { SPARQLRepository(properties.getProperty("graphdb.sparql.url")) }
    private val logSPARQLDataFetcher = LoggerFactory.getLogger(SPARQLDataFetcher::class.java)
    private val propertiesFileName = "database.properties"

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
    
    private val properties: Properties by lazy {
        getInputStreamFromClassPathResource(propertiesFileName).use {
            if (it == null) logSPARQLDataFetcher.warn("${propertiesFileName} could not be found!")
            val properties = Properties()
            properties.load(it)

            with(System.getProperties()) {
                getProperty("graphdb.sparql.url")?.run {
                    logSPARQLDataFetcher.info("Overriding ${propertiesFileName} with System properties: graphdb.sparql.url: {}", this)
                    properties.setProperty("graphdb.sparql.url", this)
                }
            }

            logSPARQLDataFetcher.info("Loaded ${propertiesFileName}: graphdb.sparql.url: {}", properties.get("graphdb.sparql.url"))
            properties
        }
    }
}