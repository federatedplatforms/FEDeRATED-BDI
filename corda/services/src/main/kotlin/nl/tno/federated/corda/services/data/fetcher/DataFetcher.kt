package nl.tno.federated.corda.services.data.fetcher

import org.slf4j.LoggerFactory
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

interface DataFetcher {

    companion object {
        val log = LoggerFactory.getLogger(DataFetcher::class.java)
    }

    fun fetch(): String

    fun getInputStreamFromClassPathResource(filename: String): InputStream? {
        val file = Paths.get(filename)
        if (Files.exists(file)) {
            log.info("Using file: {}", file.toAbsolutePath())
            return Files.newInputStream(file)
        }
        log.info("Using classpath resource: {}", filename)
        return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
    }

    fun fetch(input: String): String
}