package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import nl.tno.federated.webserver.NodeRPCConnection
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints to find out more about the Corda network. These endpoints are for debugging and proof of concept purposes
 * only. Do not expose these endpoints to the outside world.
 */
@RestController
@RequestMapping("/node", produces = [MediaType.APPLICATION_JSON_VALUE])
@Api(value = "NodeController", tags = ["Corda details"])
class NodeController(val rpc: NodeRPCConnection) {

    @ApiOperation(value = "How the node identifies itself to the network")
    @GetMapping(value = ["/identities"])
    fun identities() = rpc.client().nodeInfo().legalIdentities

    @ApiOperation(value = "Who the node knows")
    @GetMapping(value = ["/peers"])
    fun peers() = rpc.client().networkMapSnapshot().flatMap { it.legalIdentities }

    @ApiOperation(value = "What notaries are known")
    @GetMapping(value = ["/notaries"])
    fun notaries() = rpc.client().notaryIdentities()

    @ApiOperation(value = "What operations / 'smart contracts' / flows the node supports")
    @GetMapping(value = ["/flows"])
    fun flows() = rpc.client().registeredFlows()
}