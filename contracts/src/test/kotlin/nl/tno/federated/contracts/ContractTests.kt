package nl.tno.federated.contracts

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.tno.federated.states.Location
import nl.tno.federated.states.MilestoneState
import nl.tno.federated.states.MilestoneType
import org.junit.Test
import java.sql.Timestamp

class ContractTests {
    private val ledgerServices = MockServices()
    private val sender = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))

    // Enterprise 1
    private val enterpriseDE = TestIdentity(CordaX500Name("German Enterprise", "Berlin", "DE"))
    private val locationBerlin = Location("DE", "Berlin")

    @Test
    fun `arrival test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, MilestoneContract.Commands.Arrive())
                output(MilestoneContract.ID, MilestoneState(MilestoneType.ARRIVE, listOf(UniqueIdentifier()), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `arrival test without twins`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, MilestoneContract.Commands.Arrive())
                output(MilestoneContract.ID, MilestoneState(MilestoneType.ARRIVE, emptyList(), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Digital twins must be linked.")
            }
        }
    }

    @Test
    fun `arrival test without counterparty`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, MilestoneContract.Commands.Arrive())
                output(MilestoneContract.ID, MilestoneState(MilestoneType.ARRIVE, listOf(UniqueIdentifier()), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party), UniqueIdentifier()))

                `fails with`("A counterparty must exist, sender shouldn't transact with itself alone.")
            }
        }
    }
}