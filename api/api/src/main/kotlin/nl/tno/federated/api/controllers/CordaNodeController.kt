package nl.tno.federated.api.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.PageSpecification
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
import java.time.Instant

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

    @Operation(summary = "Return all the states from the Vault for the specified state_type (e.g.: EventState,DataPullState). This endpoint supports paging and sorts based on recorded time.")
    @GetMapping(value = ["/vault/{state_type}"])
    fun vaultEventStates(@PathVariable("state_type") stateType: String, @RequestParam("page", defaultValue = "1") page: Int = 10, @RequestParam("size", defaultValue = "10") size: Int = 10): List<StatePlusMeta> {
        val result = when (stateType) {
            "EventState" -> rpc.client().vaultQueryPagedAndSortedByRecordedTime<EventState>(PageSpecification(page, size)).mapToStatePlusMeta()
            "DataPullState" -> rpc.client().vaultQueryPagedAndSortedByRecordedTime<DataPullState>(PageSpecification(page, size)).mapToStatePlusMeta()
            else -> throw IllegalArgumentException("Unknown state_type specified: $stateType")
        }
        return result
    }

    private fun Vault.Page<*>.mapToStatePlusMeta(): List<StatePlusMeta> {
        val results = mutableListOf<StatePlusMeta>()
        this.states.forEach {
            val meta = this.statesMetadata.find { meta -> meta.ref == it.ref }!!
            results.add(StatePlusMeta(it.ref.txhash.toHexString(), meta.recordedTime, it.state.data.participants.map { it.nameOrNull() }, it.state.notary.name, meta.status, it.state.data.mapContractState()))
        }
        return results
    }

    private fun ContractState.mapContractState(): Any {
        return when(this.javaClass) {
            EventState::class.java -> (this as EventState).toSimpleEventState()
            DataPullState::class.java -> (this as DataPullState).toSimpleDataPullState()
            else -> throw IllegalArgumentException("Unknown state_type specified: ${this.javaClass}")
        }
    }
}

data class StatePlusMeta(val txId: String, val recordedTime: Instant, val participants: List<CordaX500Name?>, val notary: CordaX500Name, val status: Vault.StateStatus, val data: Any)
