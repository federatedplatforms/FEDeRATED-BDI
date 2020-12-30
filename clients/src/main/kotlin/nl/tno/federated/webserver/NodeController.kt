package nl.tno.federated.webserver

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Endpoints to find out more about the Corda network. These endpoints are for debugging and proof of concept purposes
 * only. Do not expose these endpoints to the outside world.
 */
@RestController
@RequestMapping("/node")
@Api(value = "NodeController", tags = ["Corda details"])
class NodeController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @ApiOperation(value = "How the node identifies itself to the network")
    @GetMapping(value = ["/identities"], produces = ["text/plain"])
    private fun identities() = proxy.nodeInfo().legalIdentities.toString()

    @ApiOperation(value = "Who the node knows")
    @GetMapping(value = ["/peers"], produces = ["text/plain"])
    private fun peers() = proxy.networkMapSnapshot().flatMap { it.legalIdentities }.toString()

    @ApiOperation(value = "What notaries are known")
    @GetMapping(value = ["/notaries"], produces = ["text/plain"])
    private fun notaries() = proxy.notaryIdentities().toString()

    @ApiOperation(value = "What operations / 'smart contracts' / flows the node supports")
    @GetMapping(value = ["/flows"], produces = ["text/plain"])
    private fun flows() = proxy.registeredFlows().toString()
}