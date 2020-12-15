package nl.tno.federated.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.Requirements.using
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType

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
            "An single output state must be passed." using (outputStates.size == 1)
            "The output must be a milestone state" using (outputStates.single() is MilestoneState)
        }
        val milestoneState = outputStates.single() as MilestoneState
        require(milestoneState.digitalTwins.isNotEmpty()) { "Digital twins must be linked." }

        when(command.value) {
            is Commands.Arrive -> {
                requireThat {
                    "Arrival is the first step in a process. No input state may be passed." using (inputStates.isEmpty())
                    "An arrival output state must be passed." using (milestoneState.type == MilestoneType.ARRIVE)
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