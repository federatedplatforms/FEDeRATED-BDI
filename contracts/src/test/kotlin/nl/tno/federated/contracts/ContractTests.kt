package nl.tno.federated.contracts

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.tno.federated.states.*
import org.junit.Test
import java.sql.Timestamp
import java.util.*

class ContractTests {
    private val netParamForMinVersion = testNetworkParameters(minimumPlatformVersion = 4)
    private val sender = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))
    private val ledgerServices = MockServices(sender, networkParameters = netParamForMinVersion)

    // Enterprise 1
    private val enterpriseDE = TestIdentity(CordaX500Name("German Enterprise", "Berlin", "DE"))

    private val eCMRuriExample = "This is a URI example for an eCMR"

    private val eventNewStateGoodsAndTransport = EventState(
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            emptyList(),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            Timestamp(System.currentTimeMillis()),
            listOf(TimeAndType(Date(),TimeType.ACTUAL)),
            eCMRuriExample, Milestone.START, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

    private val eventNewStateTransportAndLocation = EventState(
            emptyList(),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            Timestamp(System.currentTimeMillis()),
            listOf(TimeAndType(Date(),TimeType.ACTUAL)),
            eCMRuriExample, Milestone.START, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

    private val eventNewStateWrong = EventState(
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            emptyList(),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            Timestamp(System.currentTimeMillis()),
            listOf(TimeAndType(Date(),TimeType.ACTUAL)),
            eCMRuriExample, Milestone.START, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

    private val eventNewStateWrong2 = EventState(
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            Timestamp(System.currentTimeMillis()),
            listOf(TimeAndType(Date(),TimeType.ACTUAL)),
            eCMRuriExample, Milestone.START, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())


    // TODO New tests for stop events

    @Test
    fun `new event simple test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, eventNewStateGoodsAndTransport)

                verifies()
            }
        }
    }

    @Test
    fun `new event simple test 2`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, eventNewStateTransportAndLocation)

                verifies()
            }
        }
    }

    @Test
    fun `fail new event because too many goods`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, eventNewStateWrong)

                `fails with`("There can be one good only")
            }
        }
    }

    @Test
    fun `fail new event because goods and location`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, eventNewStateWrong2)

                `fails with` ("Goods and locations cannot be linked together")
            }
        }
    }
}