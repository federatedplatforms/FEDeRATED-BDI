package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import nl.tno.federated.contracts.DataPullContract

// *********
// * State *
// *********
@BelongsToContract(DataPullContract::class)
data class DataPullState(
    val sparqlQuery: String,
    val result: List<String>,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState