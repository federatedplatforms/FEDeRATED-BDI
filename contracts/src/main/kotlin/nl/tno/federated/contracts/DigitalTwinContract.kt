package nl.tno.federated.contracts

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

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
        // val command = tx.commands.requireSingleCommand<Commands>()

    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class CreateCargo : Commands
        class CreateTruck : Commands
    }
}