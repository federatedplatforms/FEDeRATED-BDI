package nl.tno.federated

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.*
import nl.tno.federated.states.DigitalTwinState
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Location
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


class EventFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))
        )
        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Brussels","BE")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        val startedNodes = arrayListOf(a, b)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(LoadResponder::class.java) }
        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple flow transaction`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple new event flow transaction`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        var newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }.single()

        val location = Location("BE", "Brussels")
        val flow = LoadFlow(listOf(idOfNewlyCreatedDT), location)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val createDTtruckFlow = CreateTruckFlow("PL4T3N1C3")
        val futureTruck = a.startFlow(createDTtruckFlow)
        network.runNetwork()

        val signedTxTruck = futureTruck.getOrThrow()
        signedTxTruck.verifyRequiredSignatures()

        val newlyCreatedEvent =  a.services.vaultService.queryBy<EventState>().states.single()
        val idOfNewlyCreatedEvent = newlyCreatedEvent.state.data.linearId

        val newlyCreatedDTs = a.services.vaultService.queryBy<DigitalTwinState>().states // assumption is that it will pop the last created, a more specific sorting will be needed
        val idOfNewlyCreatedDTs = newlyCreatedDTs.map { it.state.data.linearId }



        val flowNewEvent = NewEventFlow(EventType.DEPART, idOfNewlyCreatedEvent, idOfNewlyCreatedDTs, location)
        val newEventFuture = a.startFlow(flowNewEvent)
        network.runNetwork()

        val signedTxNewEvent = futureTruck.getOrThrow()
        signedTxNewEvent.verifyRequiredSignatures()
    }

    @Test
    fun `Transaction fails when a unique identifier for a DT that doesn't exist is passed`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        val location = Location("BE", "Brussels")
        val flow = LoadFlow(listOf(UniqueIdentifier()), location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the acceptor`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `flow records a transaction in both parties' transaction storages`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
    }

    @Test
    fun `flow doesn't record a transaction unrelated to a party`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTx = future.getOrThrow()

        // We check the recorded transaction in both transaction storages.
        for (node in listOf(a, b)) {
            assertEquals(signedTx, node.services.validatedTransactions.getTransaction(signedTx.id))
        }
        assertNull(c.services.validatedTransactions.getTransaction(signedTx.id))
    }

    @Test
    fun `flow rejects tx with no counterparty`() {

        // Params for test cargo
        val dangerous = false
        val dryBulk = true
        val excise = true
        val liquidBulk = false
        val maximumSize = 123
        val maximumTemperature = "123"
        val maximumVolume = 123
        val minimumSize = 123
        val minimumTemperature = "123"
        val minimumVolume = 123
        val minimumWeight = 123.123
        val natureOfCargo = "C4"
        val numberOfTEU = 123
        val properties = "kaboom"
        val reefer = false
        val tarWeight = 123.123
        val temperature = "123"
        val type = "Game"
        val waste = false

        val createDTflow = CreateCargoFlow(
            dangerous,
            dryBulk,
            excise,
            liquidBulk,
            maximumSize,
            maximumTemperature,
            maximumVolume,
            minimumSize,
            minimumTemperature,
            minimumVolume,
            minimumWeight,
            natureOfCargo,
            numberOfTEU,
            properties,
            reefer,
            tarWeight,
            temperature,
            type,
            waste
        )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("IT", "Milan")
        val flow = LoadFlow(idOfNewlyCreatedDT, location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `flow rejects invalid events`() {
        val location = Location("BE", "Brussels")
        val flow = LoadFlow(emptyList(), location)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }
}