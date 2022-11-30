package nl.tno.federated.ishare.utils

import nl.tno.federated.ishare.config.ISHAREConfig
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyStoreException
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*
import java.util.regex.Pattern

class ISHAREPemReaderUtil(private val ishareConfig: ISHAREConfig) {

    private val urlDecoder = Base64.getUrlDecoder()
    private val mimeDecoder = Base64.getMimeDecoder()

    @Throws(GeneralSecurityException::class, IOException::class)
    fun getPrivateKey(): RSAPrivateKey {
        val key = String(urlDecoder.decode(ishareConfig.key), StandardCharsets.US_ASCII)
        val matcher = KEY_PATTERN.matcher(key)
        if (!matcher.find()) {
            throw KeyStoreException("no private key found in $key")
        }
        val encodedKey = mimeDecoder.decode(matcher.group(1))
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encodedKey)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    fun getPublicKeyAsX5C(): List<String> {
        val publicKey = ishareConfig.cert
        val content = String(urlDecoder.decode(publicKey), StandardCharsets.US_ASCII)
        val matcher = CERT_PATTERN.matcher(content)
        val x5c: MutableList<String> = ArrayList()
        var start = 0
        while (matcher.find(start)) {
            x5c.add(matcher.group(1))
            start = matcher.end()
        }
        return x5c
    }

    fun toPemPubKey(pubKey: String): String {
        return """
               |-----BEGIN CERTIFICATE-----
               |$pubKey
               |-----END CERTIFICATE-----
               |""".trimMargin()
    }

    companion object {

        private val CERT_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" +  // Header
                "([a-z0-9+/=\\r\\n]+)" +  // Base64 text
                "-+END\\s+.*CERTIFICATE[^-]*-+",  // Footer
            Pattern.CASE_INSENSITIVE
        )

        private val KEY_PATTERN = Pattern.compile(
            ("-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" +  // Header
                "([a-z0-9+/=\\r\\n]+)" +  // Base64 text
                "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+"),  // Footer
            Pattern.CASE_INSENSITIVE
        )
    }
}