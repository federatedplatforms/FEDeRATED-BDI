package nl.tno.federated.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType

// ************
// * Contract *
// ************
class EventContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.EventContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val inputStates = tx.inputStates
        val outputStates = tx.outputStates
        requireThat {
            "A single EventState output must be passed" using (outputStates.filterIsInstance<EventState>().size == 1)
        }

        val eventState = outputStates.filterIsInstance<EventState>().single()

        // Building the list of ID of Digital Twins passed as input states
        val digitalTwinIds = inputStates.filterIsInstance<DigitalTwinState>()
            .map { it.linearId }

        requireThat {
            "Digital twins must be linked" using (eventState.digitalTwins.isNotEmpty())
            "Digital twins must exist" using (digitalTwinIds.containsAll(eventState.digitalTwins))
        }

        when(command.value) {
            is Commands.Load -> {
                requireThat {
                    "Load is the first step in a process. No event input state may be passed." using (inputStates.filterIsInstance<EventState>()
                        .isEmpty())
                    "A load output state must be passed." using (eventState.type == EventType.LOAD)
                    "A counterparty must exist, sender shouldn't transact with itself alone." using (eventState.participants.count() > 1)
                }
            }
            else -> throw IllegalArgumentException("An unknown command was passed.")
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Load : Commands
        class Departure : Commands
        class Discharge : Commands
        class Arrive : Commands
    }
}