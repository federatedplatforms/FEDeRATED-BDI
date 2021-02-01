package nl.tno.federated.contracts

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.tno.federated.states.*
import org.junit.Test
import java.sql.Timestamp

class ContractTests {
    private val ledgerServices = MockServices()
    private val sender = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))

    // Enterprise 1
    private val enterpriseDE = TestIdentity(CordaX500Name("German Enterprise", "Berlin", "DE"))
    private val locationBerlin = Location("DE", "Berlin")

    // Set up of DT for event testing
    private val dtUUID = UniqueIdentifier()

    // Params for test cargo DT
    private val cargo : Cargo = Cargo(
        dangerous = false,
        dryBulk = true,
        excise = true,
        liquidBulk = false,
        maximumSize = 123,
        maximumTemperature = "123",
        maximumVolume = 123,
        minimumSize = 123,
        minimumTemperature = "123",
        minimumVolume = 123,
        minimumWeight = 123.123,
        natureOfCargo = "C4",
        numberOfTEU = 123,
        properties = "kaboom",
        reefer = false,
        tarWeight = 123.123,
        temperature = "123",
        type = "Game",
        waste = false
    )
    private val digitalTwinState = DigitalTwinState(physicalOject = PhysicalObject.CARGO, cargo = cargo, participants = listOf(sender.party), linearId = dtUUID)

    @Test
    fun `load test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                input(DigitalTwinContract.ID, digitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(dtUUID), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `load test without twins`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                output(EventContract.ID, EventState(EventType.LOAD, emptyList(), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Digital twins must be linked")
            }
        }
    }

    @Test
    fun `load test without counterparty`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                input(DigitalTwinContract.ID, digitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(dtUUID), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party), UniqueIdentifier()))

                `fails with`("A counterparty must exist, sender shouldn't transact with itself alone")
            }
        }
    }
}