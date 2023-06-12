package nl.tno.federated.states

import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import nl.tno.federated.contracts.EventContract
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(EventContract::class)
data class EventState(
    val event: String, // Contains the event in RDF Turtle format
    val eventType: String, // Contains the event in RDF Turtle format
    override val participants: List<AbstractParty> = listOf(),
    override val linearId: UniqueIdentifier = UniqueIdentifier(),
    var accessTokens: MutableMap<AbstractParty,String> = mutableMapOf()
) : LinearState