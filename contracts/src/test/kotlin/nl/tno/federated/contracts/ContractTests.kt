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
    private val cargoDigitalTwinState = DigitalTwinState(physicalOject = PhysicalObject.CARGO, cargo = cargo, participants = listOf(sender.party), linearId = dtUUID)

    private val truckDigitalTwinState = DigitalTwinState(physicalOject = PhysicalObject.TRANSPORTMEAN, truck = Truck(licensePlate = "B1TC01N"), participants = listOf(sender.party), linearId = dtUUID)

    @Test
    fun `create cargo test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateCargo())
                output(DigitalTwinContract.ID, cargoDigitalTwinState)

                verifies()
            }
        }
    }

    @Test
    fun `create truck test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateTruck())
                output(DigitalTwinContract.ID, truckDigitalTwinState)

                verifies()
            }
        }
    }

    @Test
    fun `physical object mismatch test 1`() {
        val wrongState = cargoDigitalTwinState.copy(physicalOject = PhysicalObject.TRANSPORTMEAN)

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateCargo())
                output(DigitalTwinContract.ID, wrongState)

                `fails with` ("Physical Object must be of type CARGO")
            }
        }
    }

    @Test
    fun `physical object mismatch test 2`() {
        val wrongState = cargoDigitalTwinState.copy(physicalOject = PhysicalObject.TRANSPORTMEAN)

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateTruck())
                output(DigitalTwinContract.ID, wrongState)

                `fails with` ("Truck attribute cannot be null")
            }
        }
    }

    @Test
    fun `attributes overloading`() {
        val wrongState = cargoDigitalTwinState.copy(truck = Truck("5UP3RPL4T3"))

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateCargo())
                output(DigitalTwinContract.ID, wrongState)

                `fails with` ("Truck attribute must be null")
            }
        }
    }

    @Test
    fun `load test`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                input(DigitalTwinContract.ID, cargoDigitalTwinState)
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
                input(DigitalTwinContract.ID, cargoDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(dtUUID), Timestamp(System.currentTimeMillis()), locationBerlin, listOf(sender.party), UniqueIdentifier()))

                `fails with`("A counterparty must exist, sender shouldn't transact with itself alone")
            }
        }
    }
}