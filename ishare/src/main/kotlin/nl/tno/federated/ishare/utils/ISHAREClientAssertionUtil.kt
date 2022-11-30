package nl.tno.federated.ishare.utils

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nl.tno.federated.ishare.config.ISHAREConfig
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.interfaces.RSAPrivateKey
import java.time.Instant
import java.util.*
import java.util.regex.Pattern

class ISHAREClientAssertionUtil(private val config: ISHAREConfig) {

    private val x5c: List<String> = publicKeyToX5C(config.cert)
    private val key: RSAPrivateKey =
        PemReaderUtil.readPrivateKey(String(Base64.getMimeDecoder().decode(config.key), StandardCharsets.US_ASCII))

    fun generate(aud: String): String {
        return Jwts.builder()
            .setIssuer(config.EORI)
            .setSubject(config.EORI)
            .setExpiration(Date.from(Instant.now().plusSeconds(30)))
            .setIssuedAt(Date.from(Instant.now()))
            .setAudience(aud) // change to recipient
            .setId(UUID.randomUUID().toString())
            .setHeaderParam("x5c", x5c)
            .setHeaderParam("alg", "RS256")
            .setHeaderParam("typ", "JWT")
            .signWith(key,SignatureAlgorithm.RS256)
            .compact()
    }

    companion object {
        private val CERT_PATTERN = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" +  // Header
                    "([a-z0-9+/=\\r\\n]+)" +  // Base64 text
                    "-+END\\s+.*CERTIFICATE[^-]*-+",  // Footer
            Pattern.CASE_INSENSITIVE)

        private fun publicKeyToX5C(publicKey: String?): List<String> {
            val content = String(Base64.getMimeDecoder().decode(publicKey), StandardCharsets.US_ASCII)
            val matcher = CERT_PATTERN.matcher(content)
            val x5c: MutableList<String> = ArrayList()
            var start = 0
            while (matcher.find(start)) {
                x5c.add(matcher.group(1))
                start = matcher.end()
            }
            return x5c
        }
        val logger = LoggerFactory.getLogger(ISHAREClientAssertionUtil::class.java)
    }

}
