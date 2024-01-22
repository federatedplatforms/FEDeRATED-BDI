package nl.tno.federated.corda.services.data.fetcher

import org.eclipse.rdf4j.query.TupleQuery
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class HTTPDataFetcher : DataFetcher {

    private val log = LoggerFactory.getLogger(SPARQLDataFetcher::class.java)

    override fun fetch(input: String): String {
        // for codgnotto => call their GET endpoint (HTTP data fetcher)
        // reverse RML => interpreter
        // configure what data fether to use
    }

}