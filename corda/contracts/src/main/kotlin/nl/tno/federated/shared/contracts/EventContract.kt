package nl.tno.federated.shared.contracts

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

// ************
// * Contract *
// ************
class EventContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        val ID = EventContract::class.qualifiedName!!
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Transactions must always be confirmed, no contract check should be done.
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Other : Commands
        class Create : Commands
        class UpdateEstimatedTime : Commands
        class ExecuteEvent : Commands
        class AccidentEvent : Commands
    }
}

