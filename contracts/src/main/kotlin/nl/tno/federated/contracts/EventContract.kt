package nl.tno.federated.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.PhysicalObject

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
        val referenceStates = tx.referenceStates
        val outputStates = tx.outputStates
        val inputStates = tx.inputStates
        requireThat {
            "A single EventState output must be passed" using (outputStates.filterIsInstance<EventState>().size == 1)
        }

        // Retrieving output Event state
        val eventOutputState = outputStates.filterIsInstance<EventState>().single()
        val eventInputStates = inputStates.filterIsInstance<EventState>()

        // Building the list of ID of Digital Twins passed as input states
        val digitalTwinStates = referenceStates.filterIsInstance<DigitalTwinState>()
        val digitalTwinIds = digitalTwinStates.map { it.linearId }

        requireThat {
            // General requirements about DT
            "Digital twins must be linked" using (eventOutputState.digitalTwins.isNotEmpty())
            "Digital twins must exist" using (digitalTwinIds.containsAll(eventOutputState.digitalTwins))
            "The number of DT reference states must be equal to the number of DT UUID in the event state" using (digitalTwinIds.size == eventOutputState.digitalTwins.size)

            // General requirements about Event
            "A counterparty must exist, sender shouldn't transact with itself alone" using (eventOutputState.participants.count() > 1)
            "An eCMR URI must be passed" using (eventOutputState.eCMRuri.isNotBlank())
        }

        when(command.value) {
            is Commands.Load -> {
                requireThat {
                    "No input state may be passed" using (inputStates.isEmpty())
                    "A load output state must be passed" using (eventOutputState.type == EventType.LOAD)
                    "Every LOAD event must be linked to exactly one cargo object" using (digitalTwinStates.filter { it.physicalObject == PhysicalObject.CARGO }.size == 1)
                }
            }
            is Commands.Departure -> {
                requireThat {
                    "No input state may be passed" using (inputStates.isEmpty())
                    "A departure output state must be passed" using (eventOutputState.type == EventType.DEPART)
                }
            }
            is Commands.Discharge -> {
                requireThat {
                    "A discharge output state must be passed" using (eventOutputState.type == EventType.DISCHARGE)
                    "At least one event input state must be passed" using (eventInputStates.isNotEmpty())
                    "Every input state must be of type LOAD" using (eventInputStates.all { it.type == EventType.LOAD })
                    "Every DT in DISCHARGE event must be in a previous LOAD event" using (
                            eventOutputState.digitalTwins.all { DT ->
                                eventInputStates.any { it.digitalTwins.contains(DT) }
                            }
                    )
                }
            }
            is Commands.Arrive -> {
                requireThat {
                    "No input state may be passed" using (inputStates.isEmpty())
                    "An arrive output state must be passed" using (eventOutputState.type == EventType.ARRIVE)
                }
            }
            else -> throw IllegalArgumentException("An unknown command was passed")
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Load : Commands
        class Departure : Commands
        class Discharge : Commands
        class Arrive : Commands
        class Other : Commands
    }
}