package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.GeneralSPARQLqueryFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.QueryGraphDBbyIdFlow
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.Event
import nl.tno.federated.states.EventState
import nl.tno.federated.webserver.L1Services
import nl.tno.federated.webserver.L1Services.extractAccessTokenFromHeader
import nl.tno.federated.webserver.L1Services.retrieveUrlBody
import nl.tno.federated.webserver.L1Services.semanticAdapterURL
import nl.tno.federated.webserver.L1Services.userIsAuthorized
import nl.tno.federated.webserver.NodeRPCConnection
import nl.tno.federated.webserver.dtos.NewEvent
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URL
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

    @ApiOperation(value = "Create new event after passing it through the semantic adapter")
    @PostMapping(value = ["/newUnprocessed"])
    private fun newUnprocessedEvent(@RequestBody event: String): ResponseEntity<String> {
        // TODO can we authenticate this in the case of a webhook?

        val convertedEvent = convertData(event)
        retrieveAndStoreExtraData(convertedEvent)
        return newEvent(NewEvent(convertedEvent, emptySet()), "Bearer doitanyway") //TODO who to share with?
    }

    private fun retrieveAndStoreExtraData(event: String): Boolean {
        val digitalTwinIdsAndConsignmentIds = parseDTIdsAndBusinessTransactionIds(event)

        val solutionToken = L1Services.getSolutionToken()

        digitalTwinIdsAndConsignmentIds.forEach {
            val consignmentId = it.value
            it.key.forEach {twinId ->
                val url = URL("https://platform-sandbox.tradelens.com/api/v1/transportEquipment/currentProgress/consignmentId/$consignmentId/transportEquipmentId/$twinId")
                val dataFromApi = retrieveUrlBody(
                    url,
                    L1Services.RequestMethod.GET,
                    headers = hashMapOf(Pair("Authorization", "Bearer $solutionToken"))
                )//TODO handle api errors
                val convertedData = this.convertData(dataFromApi)
                insertDataIntoGraphDB(convertedData)
            }
        }
        return true
    }

    private fun convertData(dataFromApi: String) =
        retrieveUrlBody(
            semanticAdapterURL(),
            L1Services.RequestMethod.POST,
            dataFromApi
        )

    private fun insertDataIntoGraphDB(dataFromApi: String): Boolean {
        return GraphDBService.insertEvent(dataFromApi, true)
    }

    private fun parseDTIdsAndBusinessTransactionIds(event: String): Map<List<UUID>, String> {
        val parsedEvent = GraphDBService.parseRDFToEvents(event)
        return parsedEvent.associate { it.allEvents().flatten() to it.businessTransaction }
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
                "",
                it.fullEvent
            )
        }
}
