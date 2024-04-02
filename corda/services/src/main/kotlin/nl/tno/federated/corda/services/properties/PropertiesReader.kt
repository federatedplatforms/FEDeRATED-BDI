package nl.tno.federated.corda.services.properties

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class PropertiesReader {
    private val log = LoggerFactory.getLogger(PropertiesReader::class.java)

    fun readProperties(filename: String): Properties {
        getInputStreamFromClassPathResource(filename).use {
            val properties = Properties()

            if (it == null) {
                log.warn("database.properties could not be found!")
                return properties
            }

            properties.load(it)

            with(System.getProperties()) {
                getProperty("triplestore.protocol")?.run {
                    log.info("Overriding database.properties with System properties: triplestore.protocol: {}", this)
                    properties.setProperty("triplestore.protocol", this)
                }
                getProperty("triplestore.host")?.run {
                    log.info("Overriding database.properties with System properties: triplestore.host: {}", this)
                    properties.setProperty("triplestore.host", this)
                }
                getProperty("triplestore.port")?.let {
                    log.info("Overriding database.properties with System properties: triplestore.port: {}", this)
                    properties.setProperty("triplestore.port", it)
                }
            }

            log.info("Loaded database.properties: triplestore.protocol: {}, triplestore.host: {}, triplestore.port: {}", properties.get("triplestore.protocol"), properties.get("triplestore.host"), properties.get("triplestore.port"))
            return properties
        }
    }

    private fun getInputStreamFromClassPathResource(filename: String): InputStream? {
        val file = Paths.get(filename)
        if (Files.exists(file)) {
            log.info("Using file: {}", file.toAbsolutePath())
            return Files.newInputStream(file)
        }
        log.info("Using classpath resource: {}", filename)
        return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    }

}