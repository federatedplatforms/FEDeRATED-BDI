package nl.tno.federated.ishare.utils

import org.apache.commons.codec.binary.Base64
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.KeyStoreException
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.util.regex.Pattern

object PemReaderUtil {

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

    @Throws(GeneralSecurityException::class, IOException::class)
    fun readPrivateKey(key: String): RSAPrivateKey {
        val matcher = KEY_PATTERN.matcher(key)
        if (!matcher.find()) {
            throw KeyStoreException("no private key found in $key")
        }
        val encodedKey = Base64.decodeBase64(matcher.group(1))
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encodedKey)
        return keyFactory.generatePrivate(keySpec) as RSAPrivateKey
    }

    fun toPemPubKey(pubKey: String): String {
        return """
               |-----BEGIN CERTIFICATE-----
               |$pubKey
               |-----END CERTIFICATE-----
               |""".trimMargin()
    }

}