package nl.tno.federated.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.*
import java.util.*

// ************
// * Contract *
// ************
class EventNewContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.EventNewContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {

        val command = tx.commands.requireSingleCommand<Commands>()

        val outputStates = tx.outputStates

        val eventState = outputStates.filterIsInstance<EventNewState>().single()

        requireThat{
            "Goods and locations cannot be linked together" using (!(eventState.goods.isNotEmpty() && eventState.location.isNotEmpty()))
            "There can be one location only" using (eventState.location.size <= 1)
            "There can be one good only" using (eventState.goods.size <= 1)
            "There can be one transport mean only" using (eventState.transportMean.size <= 1)
            // there can be one "other" only?
            "There must be at least a connection" using (eventState.goods.size + eventState.location.size + eventState.transportMean.size + eventState.otherDigitalTwins.size >= 2)
            // requirements about eCMR?
        }

    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Other : Commands
    }
}