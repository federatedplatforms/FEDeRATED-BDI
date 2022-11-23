package nl.tno.federated.semantic.adapter.core

import be.ugent.idlab.knows.functions.agent.AgentFactory
import be.ugent.rml.Executor
import be.ugent.rml.Utils
import be.ugent.rml.records.RecordsFactory
import be.ugent.rml.store.QuadStore
import be.ugent.rml.store.RDF4JStore
import be.ugent.rml.term.NamedNode
import be.ugent.rml.term.Term
import org.eclipse.rdf4j.rio.RDFFormat
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files

abstract class TripleService {

    private val log = LoggerFactory.getLogger(TripleService::class.java)

    fun createTriples(data: String, rules: ClassPathResource, baseUri: String? = null): String? {
        // Replace namespace in rules file if one is provided.
        val ttl = replaceNamespaceUrl(rules, baseUri)

        // Map the data with the provided rules
        val result = mapRml(data, ttl)
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
        return try {
            ttlFile.inputStream().use {

                // Load the mapping in a QuadStore
                val rmlStore = RDF4JStore().apply {
                    read(it, null, RDFFormat.TURTLE)
                }

                // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
                val factory = RecordsFactory(tempDir.pathString)

                // Set up the outputstore (needed when you want to output something else than nquads
                val outputStore = RDF4JStore()

                val functionAgent = AgentFactory.createFromFnO("fno/functions_idlab.ttl", "fno/functions_idlab_classes_java_mapping.ttl", "functions_grel.ttl", "grel_java_mapping.ttl");

                // Create the Executor
                val executor = Executor(rmlStore, factory, outputStore, Utils.getBaseDirectiveTurtle(it), functionAgent)

                // Execute the mapping
                var result = executor.execute(null).get(NamedNode("rmlmapper://default.store"))

                val targets = executor.targets
                if (targets != null) {
                    result = targets[NamedNode("rmlmapper://default.store")]!!
                    result.copyNameSpaces(rmlStore)
                    return writeOutputTargets(targets)
                } else {
                    return null
                }
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

    /**
     * Replaces the namespace url ("https://ontology.tno.nl/logistics/federated/tradelens") in the provided file
     * with the provided baseUri. Skips if baseUri is null or blank.
     */
    private fun replaceNamespaceUrl(file: ClassPathResource, baseUri: String?): String {
        val readAllLines = file.inputStream.use {
            val reader = BufferedReader(InputStreamReader(it))
            val lines = mutableListOf<String>()
            while (reader.ready()) {
                lines.add(reader.readLine())
            }
            lines
        }
        val stringBuilder = StringBuilder()

        if (baseUri.isNullOrBlank()) {
            log.debug("Skip replacing namespaceUrl with baseUri: $baseUri")
            readAllLines.forEach { stringBuilder.append(it) }
            return stringBuilder.toString()
        }

        log.debug("Replacing namespaceUrl with baseUri: $baseUri")

        val oldUrl = "https://ontology.tno.nl/logistics/federated/tradelens"

        for (line in readAllLines) {
            val l = when {
                line.contains(oldUrl) -> line.replace(oldUrl, baseUri)
                else -> line
            }
            stringBuilder.append(l)
        }

        return stringBuilder.toString()
    }

    @Throws(Exception::class)
    private fun writeOutputTargets(targets: HashMap<Term, QuadStore>): String? {
        var hasNoResults = true
        log.debug("Writing to Targets: {}", targets.keys)

        // Go over each term and export to the Target if needed
        for ((term, store) in targets) {
            if (store.size() > 0) {
                hasNoResults = false
                log.info("Target: {} has {} results", term, store.size())
            }

            // Default target is exported separately for backwards compatibility reasons
            if (term.value == "rmlmapper://default.store") {
                log.debug("Exporting to default Target")

                if (store.size() > 1) {
                    log.info("{} quads were generated for default Target", store.size())
                } else {
                    log.info("{} quad was generated for default Target", store.size())
                }

                //if output file provided, write to triples output file
                val str = StringWriter()

                BufferedWriter(str).use {
                    store.write(it, "turtle")
                }

                return str.toString()
            }
        }
        if (hasNoResults) {
            log.info("No results!")
        }
        return null
    }
}