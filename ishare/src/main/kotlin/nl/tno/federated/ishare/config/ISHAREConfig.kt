package nl.tno.federated.ishare.config

import java.io.InputStream
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
    return Thread.currentThread().contextClassLoader.getResourceAsStream(filename)
}