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

class ContractTests {
    private val netParamForMinVersion = testNetworkParameters(minimumPlatformVersion = 4)
    private val sender = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))
    private val ledgerServices = MockServices(sender, networkParameters = netParamForMinVersion)

    // Enterprise 1
    private val enterpriseDE = TestIdentity(CordaX500Name("German Enterprise", "Berlin", "DE"))
    private val locationBerlin = Location("DE", "Berlin")

    private val licensePlate = "B1TC01N"

    // Set up of DT for event testing
    private val truckUUID = UniqueIdentifier(externalId = licensePlate)
    private val cargoUUID = UniqueIdentifier()
    private val cargoUUID2 = UniqueIdentifier()

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
    private val cargoDigitalTwinState = DigitalTwinState(physicalObject = PhysicalObject.CARGO, cargo = cargo, participants = listOf(sender.party), linearId = cargoUUID)
    private val cargoDigitalTwinState2 = cargoDigitalTwinState.copy(cargo = cargo.copy(dangerous = true), linearId = cargoUUID2)

    private val truckDigitalTwinState = DigitalTwinState(physicalObject = PhysicalObject.TRANSPORTMEAN, truck = Truck(licensePlate = licensePlate), participants = listOf(sender.party), linearId = truckUUID)


    private val eCMRuriExample = "This is a URI example for an eCMR"

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
    fun `create truck without matching external id fails`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, DigitalTwinContract.Commands.CreateTruck())
                output(DigitalTwinContract.ID, truckDigitalTwinState.copy(linearId = UniqueIdentifier()))

                `fails with`("Truck digital twins must have license plates as external id")
            }
        }
    }

    @Test
    fun `physical object mismatch test 1`() {
        val wrongState = cargoDigitalTwinState.copy(physicalObject = PhysicalObject.TRANSPORTMEAN)

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
        val wrongState = cargoDigitalTwinState.copy(physicalObject = PhysicalObject.TRANSPORTMEAN)

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
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(cargoUUID), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `load test without twins`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                output(EventContract.ID, EventState(EventType.LOAD, emptyList(), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Digital twins must be linked")
            }
        }
    }

    @Test
    fun `load test without counterparty`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(cargoUUID), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party), UniqueIdentifier()))

                `fails with`("A counterparty must exist, sender shouldn't transact with itself alone")
            }
        }
    }

    @Test
    fun `mismatch - # DT reference states greater than # DT UUID`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(cargoUUID), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("The number of DT reference states must be equal to the number of DT UUID in the event state")
            }
        }
    }

    @Test
    fun `mismatch - # DT UUIDs greater than # DT reference states`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(cargoUUID, truckUUID), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Digital twins must exist")
            }
        }
    }

    @Test
    fun `Load event linked to more than one cargo object`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, cargoDigitalTwinState2)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(cargoUUID, cargoUUID2, truckUUID), Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample, listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Every LOAD event must be linked to exactly one cargo object")
            }
        }
    }

    @Test
    fun `simple discharge event`() {
        val previousLoadEvent = EventState(EventType.LOAD, listOf(cargoUUID, truckUUID),
                Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Discharge())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                input(EventContract.ID, previousLoadEvent)
                output(EventContract.ID, EventState(EventType.DISCHARGE, listOf(cargoUUID, truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `discharge event with double load input`() {
        val previousLoadEvent = EventState(EventType.LOAD, listOf(cargoUUID, truckUUID),
                Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                listOf(sender.party, enterpriseDE.party), UniqueIdentifier())
        val previousLoadEvent2 = EventState(EventType.LOAD, listOf(cargoUUID2, truckUUID),
                Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Discharge())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, cargoDigitalTwinState2)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                input(EventContract.ID, previousLoadEvent)
                input(EventContract.ID, previousLoadEvent2)
                output(EventContract.ID, EventState(EventType.DISCHARGE, listOf(cargoUUID, cargoUUID2, truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                verifies()
            }
        }
    }

    @Test
    fun `discharge event with missing load input`() {
        val previousLoadEvent = EventState(EventType.LOAD, listOf(cargoUUID, truckUUID),
                Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Discharge())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, cargoDigitalTwinState2)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                input(EventContract.ID, previousLoadEvent)
                output(EventContract.ID, EventState(EventType.DISCHARGE, listOf(cargoUUID, cargoUUID2, truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("Every DT in DISCHARGE event must be in a previous LOAD event")
            }
        }
    }

    @Test
    fun `discharge event with input other than load type`() {
        val previousLoadEvent = EventState(EventType.OTHER, listOf(cargoUUID, truckUUID),
                Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Discharge())
                reference(DigitalTwinContract.ID, cargoDigitalTwinState)
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                input(EventContract.ID, previousLoadEvent)
                output(EventContract.ID, EventState(EventType.DISCHARGE, listOf(cargoUUID, truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))
                `fails with`("Every input state must be of type LOAD")
            }
        }
    }

    @Test
    fun `discharge event without input`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Discharge())
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                output(EventContract.ID, EventState(EventType.DISCHARGE, listOf(truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, eCMRuriExample,
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("At least one event input state must be passed")
            }
        }
    }

    @Test
    fun `new event without eCMR URI`() {
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, "",
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("An eCMR URI must be passed")
            }
        }
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Load())
                reference(DigitalTwinContract.ID, truckDigitalTwinState)
                output(EventContract.ID, EventState(EventType.LOAD, listOf(truckUUID),
                        Timestamp(System.currentTimeMillis()), locationBerlin, " \t\n",
                        listOf(sender.party, enterpriseDE.party), UniqueIdentifier()))

                `fails with`("An eCMR URI must be passed")
            }
        }
    }
}