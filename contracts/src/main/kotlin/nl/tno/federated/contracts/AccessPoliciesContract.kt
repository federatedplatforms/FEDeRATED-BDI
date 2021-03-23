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
class AccessPoliciesContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "nl.tno.federated.contracts.AccessPoliciesContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Constraints for access policies states
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class CreateAccessPolicy : Commands
    }
}