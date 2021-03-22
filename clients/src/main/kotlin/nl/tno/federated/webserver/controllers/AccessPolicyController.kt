package nl.tno.federated.webserver.controllers

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.Builder.equal
import net.corda.core.node.services.vault.QueryCriteria
import nl.tno.federated.flows.CreateAccessPolicyFlow
import nl.tno.federated.states.AccessPolicySchemaV1
import nl.tno.federated.states.AccessPolicyState
import nl.tno.federated.webserver.DTOs.AccessPolicy
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
@RequestMapping("/accesspolicies")
@Api(value = "EventController", tags = ["Access policies details"])
class AccessPolicyController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    val proxy = rpc.proxy

    @ApiOperation(value = "Create a new access policy")
    @PostMapping(value = ["/"])
    private fun newAccessPolicy (@RequestBody accessPolicy : AccessPolicy) : ResponseEntity<String> {
        return try {
                val newEventTx = proxy.startFlowDynamic(
                    CreateAccessPolicyFlow::class.java,
                    accessPolicy.context,
                    accessPolicy.type,
                    accessPolicy.id,
                    accessPolicy.idsProvider,
                    accessPolicy.idsConsumer,
                    accessPolicy.idsPermission,
                    accessPolicy.idsTarget
                ).returnValue.get()
                val createdEventId = (newEventTx.coreTransaction.getOutput(0) as AccessPolicyState).linearId.id
                ResponseEntity("Access policy created: $createdEventId", HttpStatus.CREATED)
            } catch (e: Exception) {
                return ResponseEntity("Something went wrong: $e", HttpStatus.INTERNAL_SERVER_ERROR)
            }
    }

    @ApiOperation(value = "Return all known access policies")
    @GetMapping(value = ["/"])
    private fun accessPolicies() : Map<UUID, AccessPolicy> {
        val accessPolicyStates = proxy.vaultQuery(AccessPolicyState::class.java).states.map { it.state.data }

        return accessPolicyStates.map { it.linearId.id to
                AccessPolicy(
                    it.context,
                    it.type,
                    it.id,
                    it.idsProvider,
                    it.idsConsumer,
                    it.idsPermission,
                    it.idsTarget
                ) }.toMap()
    }

    @ApiOperation(value = "Return an access policy")
    @GetMapping(value = ["/{id}"])
    private fun accessPolicy(@PathVariable id: UUID): Map<UUID, AccessPolicy> {
        val criteria = QueryCriteria.LinearStateQueryCriteria(uuid = listOf(id))
        val state = proxy.vaultQueryBy<AccessPolicyState>(criteria).states.map { it.state.data }
        return state.map { it.linearId.id to AccessPolicy(
            it.context,
            it.type,
            it.id,
            it.idsProvider,
            it.idsConsumer,
            it.idsPermission,
            it.idsTarget
        ) }.toMap()
    }

    @ApiOperation(value = "Return access policy by ids:consumer")
    @GetMapping(value = ["/consumer/{consumer}"])
    private fun accessPolicyByConsumer(@PathVariable consumer: String): Map<UUID, AccessPolicy> {
        val matchesConsumer = QueryCriteria.VaultCustomQueryCriteria(AccessPolicySchemaV1.PersistentAccessPolicy::idsConsumer.equal(consumer))
        val accessPolicyStates = proxy.vaultQueryBy<AccessPolicyState>(matchesConsumer).states.map { it.state.data }

        return accessPolicyStates.map { it.linearId.id to AccessPolicy(it.context, it.type, it.id, it.idsProvider, it.idsConsumer, it.idsPermission, it.idsTarget) }.toMap()
    }

}