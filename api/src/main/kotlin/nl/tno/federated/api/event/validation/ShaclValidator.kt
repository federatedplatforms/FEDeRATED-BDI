package nl.tno.federated.api.event.validation

import nl.tno.federated.api.event.mapper.EventType
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
import java.io.StringReader

/**
 * https://rdf4j.org/documentation/programming/shacl/
 *
 * TODO write test for ShaclValidator
 */
class ShaclValidator {

    fun validate(rdf: String, eventType: EventType) {
        if(eventType.shacl == null) return

        val shaclSail = ShaclSail(MemoryStore())
        val sailRepository = SailRepository(shaclSail)
        sailRepository.init()
        sailRepository.connection.use { connection ->
            connection.begin()
            connection.add(shaclRulesForEventType(eventType), "", RDFFormat.TURTLE, RDF4J.SHACL_SHAPE_GRAPH)
            connection.commit()

            connection.begin()
            connection.add(StringReader(rdf), "", RDFFormat.TURTLE)
            try {
                connection.commit()
            } catch (exception: RepositoryException) {
                val cause: Throwable = exception
                if (cause is ValidationException) {
                    val validationReportModel: Model = (cause as ValidationException).validationReportAsModel()
                    val writerConfig: WriterConfig = WriterConfig()
                        .set(BasicWriterSettings.INLINE_BLANK_NODES, true)
                        .set(BasicWriterSettings.XSD_STRING_TO_PLAIN_LITERAL, true)
                        .set(BasicWriterSettings.PRETTY_PRINT, true)
                    Rio.write(validationReportModel, System.out, RDFFormat.TURTLE, writerConfig)
                }
                throw exception
            }
        }
    }

    fun shaclRulesForEventType(eventType: EventType) = eventType.shacl!!.reader()
}