package nl.tno.federated.api.webhook.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*
import kotlin.text.Charsets.UTF_8

object JwtHelper {
    /**
     * Enum class representing JWT algorithms with their corresponding values.
     */
    enum class JwtAlgorithm(val value: String) {
        ALGORITHM_HS256("HS256"), ALGORITHM_HS384("HS384"), ALGORITHM_HS512("HS512"), ALGORITHM_RS256("RS256")
    }

    /**
     * Enum class representing HMAC signature algorithms with their corresponding values.
     */
    private enum class SignatureAlgorithm(val value: String) {
        ALGORITHM_HS256("HmacSHA256"), ALGORITHM_HS384("HmacSHA384"), ALGORITHM_HS512("HmacSHA512"), ALGORITHM_RS256("RS256")
    }

    // Secret key for JWT generation and verification
    private var SECRET_KEY: String = ""
    // Default expiration time for JWT tokens (365 days)
    private const val EXPIRATION_TIME_MS = 31536000000 // 365 days - Customizable

    // Default JWT algorithm and signature algorithm
    private var jwtAlgorithm: String = JwtAlgorithm.ALGORITHM_RS256.value
    private var signatureAlgorithm: String = SignatureAlgorithm.ALGORITHM_RS256.value

    /**
     * Initializes the JWT Helper with the secret key and algorithm.
     *
     * @param key Secret key for JWT token generation and verification.
     * @param algorithm JWT algorithm to be used (default is ALGORITHM_HS512).
     */
    fun init() {

    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token JWT token to be checked.
     * @return True if the token has expired, false otherwise.
     */
    fun isTokenExpired(token: String): Boolean {
        val payloadMap = extractPayload(token) ?: return true
        val expiration = payloadMap["exp"] as? Long ?: return true
        return System.currentTimeMillis() > expiration
    }

    /**
     * Decodes a base64 URL-encoded string.
     *
     * @param input Base64 URL-encoded string to decode.
     * @return Decoded string.
     */
    private fun decodeBase64URL(input: String): String {
        val decodedBytes = Base64.getUrlDecoder().decode(input)
        return String(decodedBytes, UTF_8)
    }

    /**
     * Extracts and decodes the payload from a JWT token.
     *
     * @param token JWT token from which to extract the payload.
     * @return Decoded payload as a Map or null if the token format is invalid.
     */
    fun extractPayload(token: String): Map<String, Any>? {
        val parts = token.split("\\.".toRegex())
        if (parts.size != 3) {
            return null
        }
        val payloadBase64 = parts[1]
        val payloadJson = String(Base64.getUrlDecoder().decode(payloadBase64), UTF_8)
        return ObjectMapper().readValue(payloadJson, Map::class.java) as Map<String, Any>
    }

    /**
     * Encodes a byte array into a base64 URL-encoded string and removes padding.
     *
     * @param input Byte array to encode.
     * @return Base64 URL-encoded string without padding.
     */
    private fun encodeBase64URL(input: ByteArray): String {
        val encoded = Base64.getUrlEncoder().encodeToString(input)
        return encoded.replace("=", "")
    }

    /**
     * Serializes a Map of key-value pairs into a JSON-formatted string.
     *
     * @param data Map of key-value pairs to be serialized.
     * @return JSON-formatted string representing the serialized data.
     */
    private fun serializeToJson(data: Map<String, Any>): String {
        val entries = data.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        return "{$entries}"
    }

}