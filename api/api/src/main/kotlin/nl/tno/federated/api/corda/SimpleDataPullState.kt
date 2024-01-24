package nl.tno.federated.api.corda

import net.corda.core.identity.CordaX500Name
import nl.tno.federated.corda.states.DataPullState

// omits the public key part from the participants list.
data class SimpleDataPullState(val query: String, val results: String?, val participants: List<CordaX500Name?>)

fun DataPullState.toSimpleDataPullState() = SimpleDataPullState(this.query, this.results, this.participants.map { it.nameOrNull() })
