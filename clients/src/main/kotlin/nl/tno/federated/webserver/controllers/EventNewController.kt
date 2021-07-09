package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.DigitalTwinPair
import nl.tno.federated.flows.ExecuteEventFlow
import nl.tno.federated.flows.NewEventFlow
import nl.tno.federated.flows.NewEventNewFlow
import nl.tno.federated.states.*
import nl.tno.federated.webserver.NodeRPCConnection
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Create and query events.
 */
@RestController
@RequestMapping("/eventsNew")
@Api(value = "EventController", tags = ["Event details"])
class EventNewController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "Create a new event")
    @PostMapping(value = ["/"])
    private fun newEvent(@RequestBody digitalTwins: List<DigitalTwinPair>, eCMRuri: String, milestone: MilestoneNew): ResponseEntity<String> {
        return if (false)
            ResponseEntity("Error" /*write the type of error according to condition*/, HttpStatus.BAD_REQUEST)
        else {
            try {
                val newEventTx = proxy.startFlowDynamic(
                        NewEventNewFlow::class.java,
                        digitalTwins,
                        eCMRuri,
                        milestone
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as EventNewState).linearId.id
                ResponseEntity("Event created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
