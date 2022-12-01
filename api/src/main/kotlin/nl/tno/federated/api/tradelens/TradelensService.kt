package nl.tno.federated.api.tradelens

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.util.*


class TradelensException(message: String) : Exception(message)

@Service
class TradelensService(
    private val ibmIdentityTokenRestTemplate: RestTemplate,
    private val tradelensRestTemplate: RestTemplate,
    @Value("\${tradelens.apikey}") private val apikey: String,
    @Value("\${tradelens.orgId}") private val orgId: String
) {

    private val log = LoggerFactory.getLogger(TradelensService::class.java)

    private fun getIBMIdentityToken(): String {
        log.info("Retrieving IBM identity token...")
        val urlParameters = "grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=$apikey"
        return ibmIdentityTokenRestTemplate.exchange("/identity/token", HttpMethod.POST, HttpEntity(urlParameters), String::class.java).body ?: throw TradelensException("Unable to retrieve IBM identity token, response is empty.")
    }

    private fun getSolutionToken(): String {
        log.info("Retrieving Tradelens solution token...")
        val request = getIBMIdentityToken()
        val response = tradelensRestTemplate.exchange("/sa/api/v1/auth/exchange_token/organizations/$orgId", HttpMethod.POST, HttpEntity(request), String::class.java).body ?: ""
        return extractSolutionToken(response)
    }

    private fun extractSolutionToken(unprocessedJsonString: String): String {
        return if (unprocessedJsonString.contains("solution_token")) {
            unprocessedJsonString.split(":", "{", "}")[2].replace("\"", "")
        } else {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Solution token is malformed")
        }
    }

    fun getTransportEquipmentData(consignmentId: String, twinId: UUID): String? {
        val solutionToken = getSolutionToken()
        val headers = HttpHeaders().apply { set(HttpHeaders.AUTHORIZATION, "Bearer $solutionToken") }
        log.info("Retrieving Tradelens transport equipment data for consignmentId: {} and twinId: {}", consignmentId, twinId)

        return try {
            tradelensRestTemplate.exchange("/api/v1/transportEquipment/currentProgress/consignmentId/$consignmentId/transportEquipmentId/$twinId", HttpMethod.GET, HttpEntity(null, headers), String::class.java).body
        } catch(e: HttpClientErrorException) {
            log.warn("Failed to retrieve transportEquipment data from Tradelens for consignmentId: {} and twinId: {}, statusCode: {}", consignmentId, twinId, e.statusCode)

            if(e.statusCode == HttpStatus.NOT_FOUND) {
                throw TradelensException("transportEquipment data not found at Tradelens for consignmentId: $consignmentId and twinId: $twinId")
            }
            throw e
        }
    }
}