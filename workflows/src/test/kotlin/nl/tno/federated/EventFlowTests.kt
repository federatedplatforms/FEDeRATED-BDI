package nl.tno.federated

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.*
import nl.tno.federated.states.*
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

    val cargo = Cargo(
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

    val eCMRuriExample = "This is a URI example for an eCMR"

    @Before
    fun setup() {
        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB"))),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters())
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Brussels","BE")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        val startedNodes = arrayListOf(a, b)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(NewEventResponder::class.java) }
        startedNodes.forEach { it.registerInitiatedFlow(ExecuteEventResponder::class.java) }
        network.runNetwork()

    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Simple flow transaction`() {

        val createDTflow = CreateCargoFlow(cargo)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDT, location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `Simple two-events flow transaction`() {

        val createDTflow = CreateCargoFlow(cargo)

        // Execute the flow to create a cargo
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        // Retrieving ID of the new DT (in this case only the cargo)
        var newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }.single()

        // Executing the flow for the first load event - location needed
        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, listOf(idOfNewlyCreatedDT), location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Executing the flow to create a Truck - needed for the new departure event
        val createDTtruckFlow = CreateTruckFlow(Truck("PL4T3N1C3"))
        val futureTruck = a.startFlow(createDTtruckFlow)
        network.runNetwork()

        val signedTxTruck = futureTruck.getOrThrow()
        signedTxTruck.verifyRequiredSignatures()

        // Retrieving ID of the new DT (in this case the cargo and the Truck)
        newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDTs = newlyCreatedDT.map { it.state.data.linearId }

        // Executing new event flow
        val flowNewEvent = NewEventFlow(EventType.DEPART, idOfNewlyCreatedDTs, location, eCMRuriExample, Milestone.EXECUTED)
        val newEventFuture = a.startFlow(flowNewEvent)
        network.runNetwork()

        val signedTxNewEvent = newEventFuture.getOrThrow()
        signedTxNewEvent.verifyRequiredSignatures()
    }

    @Test
    fun `Transaction fails when a unique identifier for a DT that doesn't exist is passed`() {

        val createDTflow = CreateCargoFlow(cargo)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, listOf(UniqueIdentifier()), location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `SignedTransaction returned by the flow is signed by the acceptor`() {
        val createDTflow = CreateCargoFlow(cargo)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDT, location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)
    }

    @Test
    fun `flow records a transaction in both parties' transaction storages`() {

        val createDTflow = CreateCargoFlow( cargo )
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDT, location, eCMRuriExample, Milestone.EXECUTED)
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

        val createDTflow = CreateCargoFlow(cargo)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDT, location, eCMRuriExample, Milestone.EXECUTED)
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

        val createDTflow = CreateCargoFlow(cargo)
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()


        val newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }


        val location = Location("IT", "Milan")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDT, location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `flow rejects invalid events`() {
        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, emptyList(), location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        assertFailsWith<TransactionVerificationException> { future.getOrThrow() }
    }

    @Test
    fun `Simple discharge transaction`() {

        val createDTflow = CreateCargoFlow(cargo)

        // Execute the flow to create a cargo
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        // Executing the flow to create a Truck
        val createDTtruckFlow = CreateTruckFlow(Truck("PL4T3N1C3"))
        val futureTruck = a.startFlow(createDTtruckFlow)
        network.runNetwork()

        val signedTxTruck = futureTruck.getOrThrow()
        signedTxTruck.verifyRequiredSignatures()

        // Retrieving ID of the new DT (in this case only the cargo)
        val newlyCreatedDTs = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDTs = newlyCreatedDTs.map { it.state.data.linearId }

        // Executing the flow for the first load event - location needed
        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, idOfNewlyCreatedDTs, location, eCMRuriExample, Milestone.EXECUTED)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Executing new event flow
        val flowNewEvent = NewEventFlow(EventType.DISCHARGE, idOfNewlyCreatedDTs, location, eCMRuriExample, Milestone.EXECUTED)
        val newEventFuture = a.startFlow(flowNewEvent)
        network.runNetwork()

        val signedTxNewEvent = newEventFuture.getOrThrow()
        signedTxNewEvent.verifyRequiredSignatures()
    }

    @Test
    fun `Simple planned-to-execute event flow`() {

        val createDTflow = CreateCargoFlow(cargo)

        // Execute the flow to create a cargo
        val futureDT = a.startFlow(createDTflow)
        network.runNetwork()

        val signedTxDT = futureDT.getOrThrow()
        signedTxDT.verifyRequiredSignatures()

        // Retrieving ID of the new DT (in this case only the cargo)
        var newlyCreatedDT = a.services.vaultService.queryBy<DigitalTwinState>().states
        val idOfNewlyCreatedDT = newlyCreatedDT.map { it.state.data.linearId }.single()

        // Executing the flow for the first load event - location needed
        val location = Location("BE", "Brussels")
        val flow = NewEventFlow(EventType.LOAD, listOf(idOfNewlyCreatedDT), location, eCMRuriExample, Milestone.PLANNED)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        // Getting id of planned event
        val idOfPlannedEvent = a.services.vaultService.queryBy<EventState>().states.map { it.state.data.linearId }.single()

        // Executing planned event
        val flowNewEvent = ExecuteEventFlow(idOfPlannedEvent.id)
        val newEventFuture = a.startFlow(flowNewEvent)
        network.runNetwork()

        val signedTxNewEvent = newEventFuture.getOrThrow()
        signedTxNewEvent.verifyRequiredSignatures()
    }
}