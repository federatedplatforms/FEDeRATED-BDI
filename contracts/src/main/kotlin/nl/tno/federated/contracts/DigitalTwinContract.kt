package nl.tno.federated.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.PhysicalObject

// ************
// * Contract *
// ************
class DigitalTwinContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.DigitalTwinContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.single()
        val inputStates = tx.inputStates
        val outputStates = tx.outputStates

        if (command.value is Commands.CreateTruck || command.value is Commands.CreateCargo) {
            requireThat {
                "There must be no input state" using (inputStates.isEmpty())
                "A single output must be passed" using (outputStates.size == 1)
                "Output state must be a DigitalTwinState" using (outputStates.single() is DigitalTwinState)
                "The transaction for creation must be done with the creator node alone" using (outputStates.single().participants.size == 1)
            }
            val dtState = outputStates.single() as DigitalTwinState

            when (command.value) {
                is Commands.CreateCargo -> {
                    requireThat {
                        "Physical Object must be of type CARGO" using (outputStates.filterIsInstance<DigitalTwinState>()
                            .single().physicalObject == PhysicalObject.CARGO)
                        "Cargo attribute cannot be null" using (outputStates.filterIsInstance<DigitalTwinState>()
                            .single().cargo != null)
                        "Truck attribute must be null" using (outputStates.filterIsInstance<DigitalTwinState>()
                            .single().truck == null)
                    }
                }

                is Commands.CreateTruck -> {
                    requireThat {
                        "Physical Object must be of type TRANSPORTMEAN" using (dtState.physicalObject == PhysicalObject.TRANSPORTMEAN)
                        "Truck attribute cannot be null" using (dtState.truck != null)
                        "Cargo attribute must be null" using (dtState.cargo == null)
                        "Truck digital twins must have license plates as external id" using (dtState.truck!!.licensePlate == dtState.linearId.externalId)
                    }
                }
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class CreateCargo : Commands
        class CreateTruck : Commands
    }
}