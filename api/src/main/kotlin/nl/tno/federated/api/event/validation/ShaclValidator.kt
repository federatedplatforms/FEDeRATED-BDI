package nl.tno.federated.api.event.validation

import net.corda.core.internal.rootCause
import org.eclipse.rdf4j.exceptions.ValidationException
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.vocabulary.RDF4J
import org.eclipse.rdf4j.repository.RepositoryException
import org.eclipse.rdf4j.repository.sail.SailRepository
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import org.eclipse.rdf4j.rio.WriterConfig
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings
import org.eclipse.rdf4j.sail.memory.MemoryStore
import org.eclipse.rdf4j.sail.shacl.ShaclSail
import org.eclipse.rdf4j.sail.shacl.ShaclSailValidationException
import org.slf4j.LoggerFactory

import java.io.StringReader
import java.io.StringWriter


class ShaclValidationException(msg: String?) : Exception(msg)

/**
 * https://rdf4j.org/documentation/programming/shacl/
 */
class ShaclValidator(private val shapes: List<String>) {

    private val log = LoggerFactory.getLogger(ShaclValidator::class.java)

    /**
     * Add all the shapes to the SailRepository.
     * Do this only once, not per validate action.
     */
    private fun addShapes(sailRepository: SailRepository) {
        shapes.forEach {
            sailRepository.connection.use { connection ->
                connection.begin()
                connection.add(StringReader(it), "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH)
                connection.commit()
            }
        }
    }

    private fun initRepository(): SailRepository {
        val shaclSail = ShaclSail(MemoryStore())
        val sailRepository = SailRepository(shaclSail)
        sailRepository.init()
        addShapes(sailRepository)
        return sailRepository
    }

    fun validate(rdf: String) {
        val sailRepository = initRepository()

        try {
            sailRepository.connection.use { connection ->
                connection.begin()
                connection.add(StringReader(rdf), "", RDFFormat.TURTLE)
                try {
                    connection.commit()
                } catch (exception: RepositoryException) {
                    val cause: Throwable = exception.rootCause
                    if (cause is ShaclSailValidationException) {
                        val validationReportModel: Model = cause.validationReportAsModel()
                        val writerConfig: WriterConfig = WriterConfig()
                            .set(BasicWriterSettings.INLINE_BLANK_NODES, true)
                            .set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)
                            .set(BasicWriterSettings.PRETTY_PRINT, true)
                        val sw = StringWriter()
                        Rio.write(validationReportModel, sw, RDFFormat.TURTLE, writerConfig)
                        log.debug("SHACL validation failed, validation report:\n {}", sw.toString())
                        throw ShaclValidationException(cause.message)
                    }
                    throw exception
                }
            }
        } finally {
            sailRepository.shutDown()
        }
    }
}