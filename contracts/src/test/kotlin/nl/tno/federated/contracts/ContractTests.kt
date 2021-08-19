package nl.tno.federated.contracts

import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.junit.Test
import java.util.*

class ContractTests {
    private val netParamForMinVersion = testNetworkParameters(minimumPlatformVersion = 4)
    private val sender = TestIdentity(CordaX500Name("SomeEnterprise", "Utrecht", "NL"))
    private val ledgerServices = MockServices(sender, networkParameters = netParamForMinVersion)

    // Enterprise 1
    private val enterpriseDE = TestIdentity(CordaX500Name("German Enterprise", "Berlin", "DE"))

    private val eCMRuriExample = "This is a URI example for an eCMR"
    private val validRdf = "Insert valid RDF"

    private val eventNewStateGoodsAndTransport = EventState(
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            emptyList(),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            linkedMapOf(Pair(EventType.PLANNED, Date())),
            eCMRuriExample, Milestone.START, validRdf, listOf(sender.party, enterpriseDE.party), UniqueIdentifier(externalId = "KLM7915-20210801"))

    private val eventNewStateTransportAndLocation = EventState(
            emptyList(),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            linkedMapOf(Pair(EventType.PLANNED, Date())),
            eCMRuriExample, Milestone.START, validRdf, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

    private val eventNewStateWrong = EventState(
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            emptyList(),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            linkedMapOf(Pair(EventType.PLANNED, Date())),
            eCMRuriExample, Milestone.START, validRdf, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())

    private val eventNewStateWrong2 = EventState(
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id),
            listOf(UniqueIdentifier().id, UniqueIdentifier().id),
            linkedMapOf(Pair(EventType.PLANNED, Date())),
            eCMRuriExample, Milestone.START, validRdf, listOf(sender.party, enterpriseDE.party), UniqueIdentifier())


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

    @Test
    fun `fail - more than one timestamp at time of creation`() {
        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date()), Pair(EventType.PLANNED, Date()))
        )
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, outputState)

                `fails with` ("There must be exactly one timestamp at time of creation")
            }
        }
    }

    @Test
    fun `fail - timestamp is not planned at time of creation`() {
        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = linkedMapOf(Pair(EventType.ACTUAL, Date()))
        )
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, outputState)

                `fails with` ("The type of the timestamp must be PLANNED")
            }
        }
    }

    @Test
    fun `fail - no connection between DT is made`() {
        val outputState = eventNewStateGoodsAndTransport.copy(
                goods = emptyList(), transportMean = emptyList(), location = emptyList(), otherDigitalTwins = emptyList()
        )
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, outputState)

                `fails with` ("There must be at least a connection")
            }
        }
    }

    @Test
    fun `fail - STOP is created but previous event is not START`() {
        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP
        )
        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, outputState)
                reference(EventContract.ID, outputState)

                `fails with` ("Previous event must be of type START")
            }
        }
    }

    @Test
    fun `fail - STOP is created but previous START contains different DT`() {
        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP
        )
        val referenceState = eventNewStateGoodsAndTransport.copy(
                goods = listOf(UniqueIdentifier().id)
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.Create())
                output(EventContract.ID, outputState)
                reference(EventContract.ID, referenceState)

                `fails with` ("Digital twins in the previous START event must equal to those in the current STOP event")
            }
        }
    }

    @Test
    fun `update estimated time simple transaction`() {
        val timestampsIn = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date(2021,1,1,1,1,1)))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date(2021,1,1,1,1,2)))
        val inputState = eventNewStateGoodsAndTransport.copy(
            timestamps = timestampsIn
        )
        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = timestampsOut
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                verifies()
            }
        }
    }

    @Test
    fun `fail - update estimated time without input provided`() {
        val outputState = eventNewStateGoodsAndTransport

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)

                `fails with` ("There must be a previous corresponding event as input")
            }
        }
    }

    @Test
    fun `fail - input and output different when updating estimate`() {

        val timestampsIn = linkedMapOf(Pair(EventType.PLANNED, Date()))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))
        val inputState = eventNewStateGoodsAndTransport.copy(
            timestamps = timestampsIn
        )
        val outputState = inputState.copy(
            goods = listOf(UniqueIdentifier().id),
            timestamps = timestampsOut
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                `fails with` ("Besides times, id and participants, input and output states must be equal")
            }
        }
    }

    @Test
    fun `fail - updating estimate of a state whose last element in timestamps is ACTUAL`() {

        val timestampsIn = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ACTUAL, Date()), Pair(EventType.ESTIMATED, Date()))
        val inputState = eventNewStateGoodsAndTransport.copy(timestamps = timestampsIn)
        val outputState = inputState.copy(timestamps = timestampsOut)

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                `fails with` ("Last element of old timestamps cannot be of type ACTUAL")
            }
        }
    }

    @Test
    fun `fail - updating estimate of a state whose first element in timestamps is not PLANNED`() {

        val timestamps = linkedMapOf(Pair(EventType.ACTUAL, Date()), Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.ACTUAL, Date()), Pair(EventType.PLANNED, Date()))
        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = timestamps
        )
        val inputState = outputState.copy(
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                `fails with` ("First element of old timestamps must be of type PLANNED")
            }
        }
    }

    @Test
    fun `fail - updating estimate but last timestamp is not ESTIMATED`() {

        val timestampsIn = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val inputState = eventNewStateGoodsAndTransport.copy(
            timestamps = timestampsIn
        )
        val outputState = eventNewStateGoodsAndTransport.copy(
            timestamps = timestampsOut
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                `fails with` ("The last (added) timestamp must be of type ESTIMATED")
            }
        }
    }

    @Test
    fun `fail - updating estimate but timestamps don't match`() {
        val timestampsIn = linkedMapOf(Pair(EventType.PLANNED, Date(2021,1,2,1,1,1)), Pair(EventType.ESTIMATED, Date()))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date(2021,1,1,1,1,1)), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val inputState = eventNewStateGoodsAndTransport.copy(
            timestamps = timestampsIn
        )
        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = timestampsOut
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.UpdateEstimatedTime())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                `fails with` ("Old timestamps and new timestamps must be equal, net of the new or changed element")
            }
        }
    }

    @Test
    fun `execute START event simple transaction`() {

        val timestamps = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                timestamps = timestamps
        )
        val inputState = outputState.copy(
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputState)

                verifies()
            }
        }
    }

    @Test
    fun `execute STOP event simple transaction`() {
        val timestamps1 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps3 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
            milestone = Milestone.STOP,
            timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps3
        )
        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStopState)
                input(EventContract.ID, inputStartState)

                verifies()
            }
        }
    }

    @Test
    fun `fail - execute event without input`() {

        val timestamps = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))

        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps
        )
        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)

                `fails with` ("There must be a previous corresponding event as input")
            }
        }
    }

    @Test
    fun `fail - execution of event whose previous has different DT`() {

        val timestamps1 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps3 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps3,
                goods = listOf(UniqueIdentifier().id)
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("Besides times, id and participants, input and output states must be equal")
            }
        }
    }

    @Test
    fun `fail - execution of event whose previous has last timestamp ACTUAL`() {

        val timestamps1 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ACTUAL, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps3 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ACTUAL, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps3
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("Last element of old timestamps cannot be of type ACTUAL")
            }
        }
    }

    @Test
    fun `fail - execution of event whose previous has first timestamp not PLANNED`() {

        val timestamps1 = linkedMapOf(Pair(EventType.ESTIMATED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps3 = linkedMapOf(Pair(EventType.ESTIMATED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps3
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("First element of old timestamps must be of type PLANNED")
            }
        }
    }

    @Test
    fun `fail - execution of event whose last timestamp is not ACTUAL`() {

        val timestamps1 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ESTIMATED, Date()))

        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps3 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps3
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("The last (added) timestamp must be of type ACTUAL")
            }
        }
    }

    @Test
    fun `fail - execution of event with two previous START event`() {

        val timestamps = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps2
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("There must be only one previous START event")
            }
        }
    }

    @Test
    fun `fail - execution of event with previous START event involving different DT`() {

        val timestampsInStop = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))
        val timestampsOut = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))

        val timestampsInStart = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))

        val inputStopState = eventNewStateGoodsAndTransport.copy(
            milestone = Milestone.STOP,
            timestamps = timestampsInStop
        )
        val outputState = inputStopState.copy(
                
                timestamps = timestampsOut
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestampsInStart,
                goods = listOf(UniqueIdentifier().id)
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("The corresponding START event must involve the same digital twins")
            }
        }
    }

    @Test
    fun `fail - execution of event with previous START whose last timestamp is not ACTUAL`() {

        val timestamps1 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ACTUAL, Date()))
        val timestamps1Deleted = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()))

        val timestamps2 = linkedMapOf(Pair(EventType.PLANNED, Date()), Pair(EventType.ESTIMATED, Date()), Pair(EventType.ESTIMATED, Date()))

        val outputState = eventNewStateGoodsAndTransport.copy(
                milestone = Milestone.STOP,
                timestamps = timestamps1
        )
        val inputStopState = outputState.copy(
                timestamps = timestamps1Deleted
        )

        val inputStartState = outputState.copy(
                milestone = Milestone.START,
                timestamps = timestamps2
        )

        ledgerServices.ledger {
            transaction {
                command(sender.publicKey, EventContract.Commands.ExecuteEvent())
                output(EventContract.ID, outputState)
                input(EventContract.ID, inputStartState)
                input(EventContract.ID, inputStopState)

                `fails with` ("The last timestamps of the corresponding START event must be of type ACTUAL")
            }
        }
    }
}