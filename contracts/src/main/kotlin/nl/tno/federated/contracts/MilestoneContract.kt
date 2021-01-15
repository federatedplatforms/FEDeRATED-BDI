package nl.tno.federated.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import nl.tno.federated.states.DigitalTwinState

// ************
// * Contract *
// ************
class MilestoneContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.MilestoneContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        val inputStates = tx.inputStates
        val outputStates = tx.outputStates
        requireThat {
            "An single MilestoneState output must be passed." using (outputStates.filter { it is MilestoneState }.size == 1)
        }

        val milestoneState = outputStates.filter { it is MilestoneState }.single() as MilestoneState

        // Building the list of ID of Digital Twins passed as input states
        val idOfDTinput : MutableList<UniqueIdentifier> = mutableListOf()

        for(state in inputStates){
            when(state) {
                is DigitalTwinState -> {
                    val dtState = state as DigitalTwinState
                    idOfDTinput.add(dtState.linearId)
                }
            }
        }
        requireThat {
            "Digital twins must be linked" using (milestoneState.digitalTwins.isNotEmpty())
            "Digital twins must exist" using (idOfDTinput.containsAll(milestoneState.digitalTwins))
        }

        when(command.value) {
            is Commands.Arrive -> {
                requireThat {
                    "Arrival is the first step in a process. No milestone input state may be passed." using (inputStates.filter { it is MilestoneState }.isEmpty())
                    "An arrival output state must be passed." using (milestoneState.type == MilestoneType.ARRIVE)
                    "A counterparty must exist, sender shouldn't transact with itself alone." using (milestoneState.participants.count() > 1)
                }
            }
            else -> throw IllegalArgumentException("An unknown command was passed.")
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Arrive : Commands
    }
}