package nl.tno.federated.corda.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import nl.tno.federated.corda.contracts.DataPullContract

// *********
// * State *
// *********
@BelongsToContract(DataPullContract::class)
data class DataPullState(
    val sparql: String,
    val results: String?, // Contains the results of the SPARQL in RDF Turtle format
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState