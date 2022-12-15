package nl.tno.federated.api.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import nl.tno.federated.api.corda.NodeRPCConnection
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Wrapper that exposes only the relevant node information that we want to expose.
 */
data class NodeInfoData(val names: List<CordaX500Name>, val addresses: List<NetworkHostAndPort> = emptyList())

/**
 * Endpoints to find out more about the Corda network.
 * These endpoints are for debugging and proof of concept purposes only.
 * Do not expose these endpoints to the outside world without implementing security.
 */
@RestController
@RequestMapping("/node", produces = [MediaType.APPLICATION_JSON_VALUE])
@Api(value = "NodeController", tags = ["Corda details"])
class NodeController(val rpc: NodeRPCConnection) {

    @ApiOperation(value = "How the node identifies itself to the network")
    @GetMapping(value = ["/identities"])
    fun identities() = rpc.client().nodeInfo().let { NodeInfoData(names = it.legalIdentities.map { it.name }, addresses = it.addresses) }

    @ApiOperation(value = "Who the node knows")
    @GetMapping(value = ["/peers"])
    fun peers() = rpc.client().networkMapSnapshot().map { NodeInfoData(names = it.legalIdentities.map { it.name }, addresses = it.addresses) }

    @ApiOperation(value = "What notaries are known")
    @GetMapping(value = ["/notaries"])
    fun notaries() = NodeInfoData(names = rpc.client().notaryIdentities().map { it.name })

    @ApiOperation(value = "What operations / 'smart contracts' / flows the node supports")
    @GetMapping(value = ["/flows"])
    fun flows() = rpc.client().registeredFlows()
}