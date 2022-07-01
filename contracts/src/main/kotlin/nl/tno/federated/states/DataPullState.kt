package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable
import nl.tno.federated.contracts.DataPullContract
import nl.tno.federated.contracts.EventContract
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(DataPullContract::class)
data class DataPullState(
    val sparqlQuery: String,
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState