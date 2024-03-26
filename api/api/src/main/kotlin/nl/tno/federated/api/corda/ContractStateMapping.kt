package nl.tno.federated.api.corda

import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault
import nl.tno.federated.corda.states.DataPullState
import nl.tno.federated.corda.states.EventState
import java.time.Instant

// Omits the public key part from the participants list.
data class SimpleDataPullState(val query: String, val results: String?, val participants: List<CordaX500Name?>, val recordedTime: Instant)

fun DataPullState.toSimpleDataPullState(meta: Vault.StateMetadata) = SimpleDataPullState(this.query, this.results, this.participants.map { it.nameOrNull() }, meta.recordedTime)

// Omits the public key part from the participants list.
data class SimpleEventState(val eventUUID: String, val eventType: String, val eventData: String, val recordedTime: Instant)

fun EventState.toSimpleEventState(meta: Vault.StateMetadata) = SimpleEventState(this.linearId.externalId.toString(), this.eventType, this.event, meta.recordedTime) // TODO fix

data class StatePlusMeta(val id: String, val recordedTime: Instant, val participants: List<CordaX500Name?>, val notary: CordaX500Name, val status: Vault.StateStatus, val data: Any)

fun ContractState.mapContractState(meta: Vault.StateMetadata): Any {
    return when (this.javaClass) {
        EventState::class.java -> (this as EventState).toSimpleEventState(meta)
        DataPullState::class.java -> (this as DataPullState).toSimpleDataPullState(meta)
        else -> throw IllegalArgumentException("Unknown state_type specified: ${this.javaClass}")
    }
}

fun Vault.Page<*>.mapToStatePlusMeta(): List<StatePlusMeta> {
    val results = mutableListOf<StatePlusMeta>()
    this.states.forEach {
        val meta = this.statesMetadata.find { meta -> meta.ref == it.ref }!!
        results.add(StatePlusMeta(it.ref.txhash.toHexString(), meta.recordedTime, it.state.data.participants.map { it.nameOrNull() }, it.state.notary.name, meta.status, it.state.data.mapContractState(meta)))
    }
    return results
}