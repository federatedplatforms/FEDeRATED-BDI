package nl.tno.federated.contracts

import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import org.junit.Test
import java.sql.Timestamp

class ContractTests {
    private val ledgerServices = MockServices()
    private val enterprise = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))

    @Test
    fun `arrival test`() {
        ledgerServices.ledger {
            transaction {
                command(enterprise.publicKey, MilestoneContract.Commands.Arrive())
                output(MilestoneContract.ID, MilestoneState(MilestoneType.ARRIVE, listOf(UniqueIdentifier()), Timestamp(System.currentTimeMillis()), listOf(enterprise.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `arrival test without twins`() {
        ledgerServices.ledger {
            transaction {
                command(enterprise.publicKey, MilestoneContract.Commands.Arrive())
                output(MilestoneContract.ID, MilestoneState(MilestoneType.ARRIVE, emptyList(), Timestamp(System.currentTimeMillis()), listOf(enterprise.party), UniqueIdentifier()))

                `fails with`("Digital twins must be linked.")
            }
        }
    }
}