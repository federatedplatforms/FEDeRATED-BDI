package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TransactionState
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.nodeapi.internal.KeyOwningIdentity.UnmappedIdentity.uuid
import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.states.DataPullState
import nl.tno.federated.states.EventState
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Wrapper that exposes only the relevant node information that we want to expose.
 */
data class CordaNodeInfo(val names: List<CordaX500Name>, val addresses: List<NetworkHostAndPort> = emptyList())

/**
 * Endpoints to find out more about the Corda network.
 * These endpoints are for debugging and proof of concept purposes only.
 * Do not expose these endpoints to the outside world without implementing security.
 */
@RestController
@RequestMapping("/corda", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "CordaNodeController", description = "Corda network and node details")
class CordaNodeController(val rpc: NodeRPCConnection) {

    @Operation(summary = "How the node identifies itself to the network")
    @GetMapping(value = ["/identities"])
    fun identities() = rpc.client().nodeInfo().let { CordaNodeInfo(names = it.legalIdentities.map { it.name }, addresses = it.addresses) }

    @Operation(summary = "Who the node knows")
    @GetMapping(value = ["/peers"])
    fun peers() = rpc.client().networkMapSnapshot().map { CordaNodeInfo(names = it.legalIdentities.map { it.name }, addresses = it.addresses) }

    @Operation(summary = "What notaries are known")
    @GetMapping(value = ["/notaries"])
    fun notaries() = CordaNodeInfo(names = rpc.client().notaryIdentities().map { it.name })

    @Operation(summary = "What operations / 'smart contracts' / flows the node supports")
    @GetMapping(value = ["/flows"])
    fun flows() = rpc.client().registeredFlows()

    @Operation(summary = "Return all the EventStates from the Vault. This endpoint supports paging.")
    @GetMapping(value = ["/vault/EventState"])
    fun vaultEventStates(@RequestParam("pageSize", defaultValue = "10") pageSize: Int = 10): List<EventState> {
        val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        val vaultQueryBy = rpc.client().vaultQueryBy<EventState>(criteria, PageSpecification(1, pageSize))
        return vaultQueryBy.states.map { it.state.data }
    }

    @Operation(summary = "Return all the DataPullState from the Vault. This endpoint supports paging.")
    @GetMapping(value = ["/vault/DataPullState"])
    fun vaultDataPullStates(@RequestParam("pageSize", defaultValue = "10") pageSize: Int = 10): List<DataPullState> {
        val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL)
        return rpc.client().vaultQueryBy<DataPullState>(criteria, PageSpecification(1, pageSize)).states.map { it.state.data }
    }
}