package nl.tno.federated.ishare

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import nl.tno.federated.ishare.config.ISHAREConfig
import nl.tno.federated.ishare.model.party.PartiesResponse
import nl.tno.federated.ishare.model.party.PartiesToken
import nl.tno.federated.ishare.model.token.AccessTokenBody
import nl.tno.federated.ishare.model.token.AccessTokenHeader
import nl.tno.federated.ishare.model.token.Body
import nl.tno.federated.ishare.model.token.ClientAssertion
import nl.tno.federated.ishare.model.token.ConsumerAccessToken
import nl.tno.federated.ishare.model.token.Header
import nl.tno.federated.ishare.model.token.ISHAREAccessToken
import nl.tno.federated.ishare.model.token.ISHARETokenRequest
import nl.tno.federated.ishare.model.token.ISHARETokenResponse
import nl.tno.federated.ishare.utils.ISHAREHTTPClient
import nl.tno.federated.ishare.utils.ISHAREPemReaderUtil
import org.apache.commons.io.IOUtils
import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.message.BasicNameValuePair
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.IOException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ISHAREClient(ishareConfigFileName: String = "ishare.properties") {

    private val logger = LoggerFactory.getLogger(ISHAREClient::class.java)
    private val ishareConfig = ISHAREConfig.loadProperties(ishareConfigFileName)
    private val isharePemReaderUtil = ISHAREPemReaderUtil(ishareConfig)
    private val ishareHTTPClient = ISHAREHTTPClient(ishareConfig)
    private val mapper = ObjectMapper().registerModule(KotlinModule())
    private val decoder: Base64.Decoder = Base64.getUrlDecoder()
    private val accessTokens: MutableMap<String, ISHAREAccessToken> = ConcurrentHashMap() // This should be stored somewhere, but not in memory

    /**
     * Check the consumer status with the ishare scheme, Must be active to be allowed to connect
     **/
    fun checkPartyWithScheme(partyEORI: String): Boolean {
        try {
            val response = ishareHTTPClient.sendRequest(HttpGet("${ishareConfig.schemeURL}/parties?eori=${partyEORI}"), getToken(ishareConfig.schemeURL, ishareConfig.schemeID))

            if (response.statusLine.statusCode == 200) {
                val partiesResponse = mapper.readValue(String(IOUtils.toByteArray(response.entity.content)), PartiesResponse::class.java)
                val decodedToken = decodeJWTToken(partiesResponse.parties_token.replace("/\\s/g", ""))
                val partiesToken = mapper.readValue(decodedToken.second, PartiesToken::class.java)
                if (partiesToken.parties_info.data.isNotEmpty()) return "ACTIVE" == partiesToken.parties_info.data[0].adherence.status.toUpperCase()
                logger.warn("Party with id $partyEORI is NOT active in the scheme information: ${partiesToken.parties_info.data[0]}")
                return false
            } else {
                throw ISHAREException("Could not request delegation evidence from ${ishareConfig.schemeURL}, HTTP returncode ${response.statusLine.statusCode}")
            }
        } catch (e: IOException) {
            throw ISHAREException("Could not request party information from ${ishareConfig.schemeURL}", e)
        }
    }

    /**
     * Checks an accesstoken
     * - Token must have issuer : this service
     * - signed with service private ishare key
     * - subject : the consumer , which must be part of the scheme (in case he was removed during lifetime of token)
     * - must not be expired
     * - audience must be this service
     */
    fun checkAccessToken(token: String): Pair<Boolean, String> {
        logger.info("Verifying iSHARE Access Token")
        // first decode the token to check the fields
        val decodedToken = decodeJWTToken(token)
        val accessToken = ConsumerAccessToken(mapper.readValue(decodedToken.first, AccessTokenHeader::class.java), mapper.readValue(decodedToken.second, AccessTokenBody::class.java), decodedToken.third)

        //check header fields
        when {
            accessToken.header.alg.substring(0, 2) != "RS" -> return Pair(false, "Incorrect algorithm in header field 'alg'")
            accessToken.header.alg.substring(2, 5).toInt() < 256 -> return Pair(false, "alg field has lower value than RS256 (i.e. 128, 64 etc)")
            accessToken.header.typ != "JWT" -> return Pair(false, "typ field does not contain the mandatory value 'JWT'")
            //check body fields
            accessToken.body.iss != ishareConfig.EORI -> return Pair(false, "This accessToken is not issued by us !! ")
            accessToken.body.aud != ishareConfig.EORI -> return Pair(false, "This accessToken is not meant for us !!")
            // 5 is added here because of time sync issues
            accessToken.body.iat > ((System.currentTimeMillis() / 1000) + 5) -> return Pair(false, "Access token has expired")
            else -> return try {
                if (Jwts.parserBuilder().setSigningKey(isharePemReaderUtil.getPrivateKey()).build().parseClaimsJws(token).getBody().issuer != ishareConfig.EORI) return Pair(
                    false,
                    "Access token is not issued by us"
                )
                Pair(true, "Verified")
            } catch (e: JwtException) {
                Pair(false, "Invalid Signature (Access token might not be signed by us)")
            }
        }
    }

    /**
     * check if the client assertion is conform the ISHARE specification and signed correctly
     * The issuer myst be the same as the client_id in the request and the audience must be the ishare ID of this service
     */
    fun checkTokenRequest(tokenRequest: ISHARETokenRequest): Pair<Boolean, String> {
        when {
            tokenRequest.grant_type != "client_credentials" -> return Pair(false, "Incorrect grant_type")
            tokenRequest.scope != "iSHARE" -> return Pair(false, "Incorrect scope")
            tokenRequest.client_assertion_type != "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" -> return Pair(false, "Incorrect client_assertion_type")
            tokenRequest.client_id != parseIssuer(tokenRequest.client_assertion) -> return Pair(false, "Issuer (client_assertion) does not match the client_id")
        }

        logger.debug("client id: {} assertion to check: {}", tokenRequest.client_id, tokenRequest.client_assertion)
        val decodedca = decodeJWTToken(tokenRequest.client_assertion.replace("/\\s/g", ""))
        logger.debug("decoded client assertion: header = {}, body = {}", decodedca.first, decodedca.second)

        val clientAssertion: ClientAssertion = try {
            ClientAssertion(mapper.readValue(decodedca.first, Header::class.java), mapper.readValue(decodedca.second, Body::class.java), decodedca.third)
        } catch (e: Exception) {
            return Pair(false, "Error reading the client assertion : $e")
        }

        when {
            clientAssertion.header.alg.substring(0, 2) != "RS" -> return Pair(false, "Incorrect algorithm in header field 'alg'")
            clientAssertion.header.alg.substring(2, 5).toInt() < 256 -> return Pair(false, "alg field has lower value than RS256 (i.e. 128, 64 etc)")
            clientAssertion.header.typ != "JWT" -> return Pair(false, "typ field does not contain the mandatory value 'JWT'")
            clientAssertion.body.iss != tokenRequest.client_id -> return Pair(false, "Client assertion JWT payload 'sub' field has a different value the client_id in the request")
            clientAssertion.body.iss != clientAssertion.body.sub -> return Pair(false, "Client assertion JWT payload 'sub' field has a different value than 'iss' field")
            clientAssertion.body.aud != ishareConfig.EORI -> return Pair(false, "Client assertion JWT payload 'aud' field is different value than the server iSHARE client id")
            clientAssertion.body.exp != clientAssertion.body.iat + 30 -> return Pair(false, "Client assertion JWT payload 'exp' field has a different value the iat field + 30 seconds")
            // 5 is added here because of time sync issues
            clientAssertion.body.iat > ((System.currentTimeMillis() / 1000) + 5) -> return Pair(false, "Client assertion JWT payload 'iat' field is after current time")
            else -> return try {
                val fact: CertificateFactory = CertificateFactory.getInstance("X.509")
                val cer: X509Certificate = fact.generateCertificate(ByteArrayInputStream(isharePemReaderUtil.toPemPubKey(clientAssertion.header.x5c[0]).toByteArray())) as X509Certificate
                if (Jwts.parserBuilder().setSigningKey(cer.publicKey as RSAPublicKey).build().parseClaimsJws(tokenRequest.client_assertion).getBody().issuer != ishareConfig.EORI) return Pair(false, "Access token is not issued by us")
                Pair(true, "Verified")
            } catch (e: JwtException) {
                Pair(false, "Failed to verify signature")
            }
        }
    }

    fun getTokenRequest(): ISHARETokenRequest {
        return ISHARETokenRequest("client_credentials", "iSHARE", ishareConfig.EORI, "urn:ietf:params:oauth:client-assertion-type:jwt-bearer", generate(ishareConfig.EORI))
    }

    fun createTokenResponse(client_id: String): ISHARETokenResponse {
        return ISHARETokenResponse(createAccessToken(client_id))
    }

    fun ishareEnabled(): Boolean = ishareConfig.enabled

    private fun decodeJWTToken(token: String): Triple<String, String, String> {
        logger.debug("Decoding: {}", token)
        val chunks = token.split("\\.".toRegex()).toTypedArray()
        if (chunks.size != 3) {
            throw ISHAREException("Invalid Token format")
        }

        val part1 = String(decoder.decode(chunks[0]))
        val part2 = String(decoder.decode(chunks[1]))
        val part3 = String(decoder.decode(chunks[2]))

        logger.debug("Result header: {}, body: {}, signature: {}", part1, part2, part3)
        return Triple(part1, part2, part3)
    }

    private fun parseIssuer(token: String): String {
        val jwtConsumerWithoutVerification = JwtConsumerBuilder().setSkipSignatureVerification().setSkipDefaultAudienceValidation().build()
        val jwtClaims = jwtConsumerWithoutVerification.processToClaims(token)
        return jwtClaims.issuer
    }

    /**
     * Create an AccessToken for a consumer.
     * The Token consists of a JWT , containing this service as issuer, and signed
     * with the private ISHRE key of this service
     */
    private fun createAccessToken(participant: String): String {
        val jwtb = Jwts.builder().setIssuer(ishareConfig.EORI).setSubject(participant).setExpiration(Date.from(Instant.now().plusSeconds(3600))).setIssuedAt(Date.from(Instant.now())).setAudience(ishareConfig.EORI)
            .setId(UUID.randomUUID().toString())
            .setHeaderParam("alg", "RS256").setHeaderParam("typ", "JWT")
        val jwt = jwtb.signWith(isharePemReaderUtil.getPrivateKey(), SignatureAlgorithm.RS256).compact()
        logger.debug("created accessToken: {}", jwt)
        return jwt
    }

    private fun generate(aud: String): String {
        return Jwts.builder()
            .setIssuer(ishareConfig.EORI)
            .setSubject(ishareConfig.EORI)
            .setExpiration(Date.from(Instant.now().plusSeconds(30)))
            .setIssuedAt(Date.from(Instant.now()))
            .setAudience(aud) // change to recipient
            .setId(UUID.randomUUID().toString())
            .setHeaderParam("x5c", isharePemReaderUtil.getPublicKeyAsX5C())
            .setHeaderParam("alg", "RS256")
            .setHeaderParam("typ", "JWT")
            .signWith(isharePemReaderUtil.getPrivateKey(), SignatureAlgorithm.RS256)
            .compact()
    }

    private fun renewToken(url: String, aud: String): ISHAREAccessToken {
        val form: MutableList<NameValuePair> = mutableListOf<NameValuePair>().apply {
            add(BasicNameValuePair("grant_type", "client_credentials"))
            add(BasicNameValuePair("scope", "iSHARE"))
            add(BasicNameValuePair("client_id", ishareConfig.EORI))
            add(BasicNameValuePair("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"))
            add(BasicNameValuePair("client_assertion", generate(aud)))
        }

        val httpPost = HttpPost("$url/connect/token").apply {
            this.entity = UrlEncodedFormEntity(form)
            setHeader("Content-Type", "application/x-www-form-urlencoded")
        }

        try {
            logger.info("sending accessToken request to : {} with parameters: {}", url, form)
            val response = ishareHTTPClient.sendRequest(httpPost)
            val responseString = String(IOUtils.toByteArray(response.entity.content))
            if (response.statusLine.statusCode == 200) {
                accessTokens[url] = mapper.readValue(responseString, ISHAREAccessToken::class.java)
                logger.info("Access token successfully renewed/acquired for $url")
                return accessTokens[url]!!
            } else {
                throw ISHAREException("Could not request token from Scheme: ${response.statusLine.statusCode} : ${response.entity.content}")
            }
        } catch (e: IOException) {
            throw ISHAREException("Could not request token from AR", e)
        }
    }

    private fun getToken(url: String, aud: String): String {
        if (!accessTokens.containsKey(url) || accessTokens[url]!!.hasExpired()) {
            renewToken(url, aud)
        }
        return accessTokens[url]!!.access_token
    }
}