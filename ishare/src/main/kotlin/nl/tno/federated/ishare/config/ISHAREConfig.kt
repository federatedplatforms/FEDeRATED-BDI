package nl.tno.federated.ishare.config

import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class ISHAREConfig(
    val EORI: String,
    val key: String,
    val cert: String,
    val pass: String,
    val schemeURL: String,
    val schemeID: String,
    val enabled: Boolean = false
) {
    companion object {
        fun loadProperties(fileName: String): ISHAREConfig {
            getInputStreamFromClassPathResource(fileName).use {
                val properties = Properties().apply {
                    load(it)
                }

                // System properties overrides
                with(System.getProperties()) {
                    getProperty("ishare.EORI")?.let { properties.setProperty("ishare.EORI", it) }
                    getProperty("ishare.key")?.let { properties.setProperty("ishare.key", it) }
                    getProperty("ishare.cert")?.let { properties.setProperty("ishare.cert", it) }
                    getProperty("ishare.pass")?.let { properties.setProperty("ishare.pass", it) }
                    getProperty("ishare.schemeURL")?.let { properties.setProperty("ishare.schemeURL", it) }
                    getProperty("ishare.schemeID")?.let { properties.setProperty("ishare.schemeID", it) }
                    getProperty("ishare.enabled")?.let { properties.setProperty("ishare.enabled", it) }
                }

                return with(properties) {
                    ISHAREConfig(
                        EORI = getProperty("ishare.EORI"),
                        key = getProperty("ishare.key"),
                        cert = getProperty("ishare.cert"),
                        pass = getProperty("ishare.pass"),
                        schemeURL = getProperty("ishare.schemeURL"),
                        schemeID = getProperty("ishare.schemeID"),
                        enabled = getProperty("ishare.enabled")?.toBoolean() ?: false
                    )
                }
            }
        }


    }
}

private fun getInputStreamFromClassPathResource(filename: String): InputStream? {
    val file = Paths.get(filename)
    if(Files.exists(file)) {
        return Files.newInputStream(file)
    }
    return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
}