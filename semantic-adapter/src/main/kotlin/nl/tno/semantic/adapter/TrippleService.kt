package nl.tno.semantic.adapter

import absolutePathString
import be.ugent.rml.Executor
import be.ugent.rml.Utils
import be.ugent.rml.records.RecordsFactory
import be.ugent.rml.store.QuadStoreFactory
import be.ugent.rml.store.RDF4JStore
import be.ugent.rml.term.NamedNode
import deleteIfExists
import inputStream
import nl.tno.semantic.adapter.DataType.TRADELENS_CONTAINERS
import nl.tno.semantic.adapter.DataType.TRADELENS_EVENTS
import nl.tno.semantic.adapter.TradelensMapper.createPreMappingContainers
import nl.tno.semantic.adapter.TradelensMapper.createPreMappingEvents
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import pathString
import java.io.File
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files

enum class DataType {
    DEFAULT, TRADELENS_EVENTS, TRADELENS_CONTAINERS
}

@Service
class TrippleService {

    private val log = LoggerFactory.getLogger(TrippleService::class.java)
    private val defaultRules = ClassPathResource("default_rules.ttl").file
    private val containersRules = ClassPathResource("container_rules.ttl").file

    fun createTripples(jsonData: String, type: DataType, baseUri: String?): String {
        // For the tradelens data we need to create mappings based on the type.
        val data = mapInputData(jsonData, type)

        // For Tradelens containers use specific rules.
        val rules = getRulesForType(type)

        // Replace namespace in rules file if one is provided.
        val ttl = replaceNamespaceUrl(rules, baseUri)

        // Map the data with the provided rules
        val result = mapRml(data, ttl)
        log.trace("Result: $result")
        return result
    }

    private fun getRulesForType(type: DataType) = if (type == TRADELENS_CONTAINERS) containersRules else defaultRules

    /**
     * For the tradelens data we need to create mappings based on the type.
     */
    private fun mapInputData(jsonData: String, type: DataType) = when (type) {
        TRADELENS_EVENTS -> createPreMappingEvents(jsonData)
        TRADELENS_CONTAINERS -> createPreMappingContainers(jsonData)
        else -> jsonData
    }

    /**
     * Run rml mapper for the given json data and provided ttl mapping.
     */
    private fun mapRml(jsonData: String, mappingTtl: String): String {

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
                val rmlStore = QuadStoreFactory.read(it);

                // Set up the basepath for the records factory, i.e., the basepath for the (local file) data sources
                val factory = RecordsFactory(tempDir.pathString)

                // Set up the functions used during the mapping
                // val functionAgent = AgentFactory.createFromFnO("fno/functions_idlab.ttl", "fno/functions_idlab_test_classes_java_mapping.ttl");

                // Set up the outputstore (needed when you want to output something else than nquads
                val outputStore = RDF4JStore()

                // Create the Executor
                val executor = Executor(rmlStore, factory, outputStore, Utils.getBaseDirectiveTurtle(it), null)

                // Execute the mapping
                val result = executor.execute(null).get(NamedNode("rmlmapper://default.store"))

                // Output the result
                val out = StringWriter()

                result?.write(out, "turtle")
                out.close()
                out.toString()
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
    private fun replaceNamespaceUrl(file: File, baseUri: String?): String {
        val readAllLines = Files.readAllLines(file.toPath())
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
                line.contains(oldUrl) -> line.replace(oldUrl, baseUri!!)
                else -> line
            }
            stringBuilder.append(l)
        }

        return stringBuilder.toString()
    }
}