package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.vault.PageSpecification
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.utilities.NetworkHostAndPort
import nl.tno.federated.api.corda.NodeRPCConnection
import nl.tno.federated.api.corda.SimpleDataPullState
import nl.tno.federated.api.corda.SimpleEventState
import nl.tno.federated.api.corda.toSimpleDataPullState
import nl.tno.federated.api.corda.toSimpleEventState
import nl.tno.federated.api.corda.vaultQueryPagedAndSortedByRecordedTime
import nl.tno.federated.corda.states.DataPullState
import nl.tno.federated.corda.states.EventState
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

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
class CordaNodeController(private val rpc: NodeRPCConnection) {

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

    @Operation(summary = "What operations / 'smart contracts' / flows the node supports")
    @GetMapping(value = ["/flows/{run_id}"])
    fun flowStatus(@PathVariable("run_id") runId: String) {
        val criteria = QueryCriteria.VaultQueryCriteria().withExternalIds(listOf(UUID.fromString(runId)))
        //val states = rpc.client().vaultQuery()vaultQueryBy<EventState>(criteria, PageSpecification(), Sort(emptySet()))
        println("bla")
    }

    @Operation(summary = "Return all the EventStates from the Vault. This endpoint supports paging and sorts based on recorded time.")
    @GetMapping(value = ["/vault/EventState"])
    fun vaultEventStates(@RequestParam("page", defaultValue = "1") page: Int = 10, @RequestParam("size", defaultValue = "10") size: Int = 10): List<SimpleEventState> {
        return rpc.client().vaultQueryPagedAndSortedByRecordedTime<EventState>(PageSpecification(page, size)).map { it.toSimpleEventState() }
    }

    @Operation(summary = "Return all the DataPullState from the Vault. This endpoint supports paging and sorts based on recorded time.")
    @GetMapping(value = ["/vault/DataPullState"])
    fun vaultDataPullStates(@RequestParam("page", defaultValue = "1") page: Int = 10, @RequestParam("size", defaultValue = "10") size: Int = 10): List<SimpleDataPullState> {
        return rpc.client().vaultQueryPagedAndSortedByRecordedTime<DataPullState>(PageSpecification(page, size)).map { it.toSimpleDataPullState() }
    }
}

