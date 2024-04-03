package nl.tno.federated.api.rml

import be.ugent.idlab.knows.functions.agent.AgentFactory
import be.ugent.rml.Executor
import be.ugent.rml.Utils
import be.ugent.rml.records.RecordsFactory
import be.ugent.rml.store.QuadStore
import be.ugent.rml.store.RDF4JStore
import be.ugent.rml.term.NamedNode
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class RMLMapper {

    private val log = LoggerFactory.getLogger(RMLMapper::class.java)

    fun createTriples(data: String, rml: String): String? {
        // Map the data with the provided rules
        val result = mapRml(data, rml)
        log.trace("Result: $result")
        return result
    }

    /**
     * Run rml mapper for the given json data and provided ttl mapping.
     */
    private fun mapRml(jsonData: String, mappingTtl: String): String? {
        // RmlMapper requires an unique folder per request, it read the data from data.json
        val tempDir = Files.createTempDirectory("semantic-adapter")
        log.debug("Created temp dir: ${tempDir.absolutePathString()}")

        val dataFile = Files.createFile(tempDir.resolve("data.json"))
        log.debug("Created data file: ${dataFile.absolutePathString()}")

        val ttlFile = Files.createFile(tempDir.resolve("rules.ttl"))
        log.debug("Created ttl file: ${ttlFile.absolutePathString()}")

        Files.write(dataFile, jsonData.toByteArray(StandardCharsets.UTF_8))
        Files.write(ttlFile, mappingTtl.toByteArray(StandardCharsets.UTF_8))

        // Get the mapping string stream
        try {
            ttlFile.inputStream().use {

                // Load the mapping in a QuadStore
                val rmlStore = RDF4JStore().apply {
                    read(it, null, RDFFormat.TURTLE)
                }

                // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
                val factory = RecordsFactory(tempDir.pathString)

                // Set up the outputstore (needed when you want to output something else than nquads
                val outputStore = RDF4JStore()

                // Create the Executor
                val executor = Executor(rmlStore, factory, outputStore, Utils.getBaseDirectiveTurtle(it), functionAgent)

                // Execute the mapping
                val targets = executor.execute(null)

                if (targets != null) {
                    val result = targets[NamedNode("rmlmapper://default.store")]
                    if (result != null) {
                        result.copyNameSpaces(rmlStore)
                        return writeToString(result)
                    }
                }
                return null
            }
        } finally {
            // Cleanup
            dataFile.deleteIfExists()
            log.debug("Deleted data file: ${dataFile.absolutePathString()}")
            ttlFile.deleteIfExists()
            log.debug("Deleted ttl file: ${ttlFile.absolutePathString()}")
            tempDir.deleteIfExists()
            log.debug("Deleted temp dir: ${tempDir.absolutePathString()}")
        }
    }

    @Throws(Exception::class)
    private fun writeToString(store: QuadStore, format: String = "turtle"): String {
        // Default target is exported separately for backwards compatibility reasons
        log.debug("Exporting to default Target")

        if (store.size() > 1) {
            log.info("{} quads were generated for default Target", store.size())
        } else {
            log.info("{} quad was generated for default Target", store.size())
        }

        //if output file provided, write to triples output file
        val str = StringWriter()

        BufferedWriter(str).use {
            store.write(it, format)
        }

        return str.toString()
    }

    companion object {
        val functionAgent = AgentFactory.createFromFnO("fno/functions_idlab.ttl", "fno/functions_idlab_classes_java_mapping.ttl", "functions_grel.ttl", "grel_java_mapping.ttl");
    }
}