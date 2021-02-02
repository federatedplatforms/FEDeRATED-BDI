package nl.tno.federated

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.flows.CargoCreationResponder
import nl.tno.federated.flows.CreateCargoFlow
import nl.tno.federated.flows.CreateTruckFlow
import nl.tno.federated.flows.TruckCreationResponder
import org.junit.After
import org.junit.Before
import org.junit.Test


class DigitalTwinFlowTests {

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
        startedNodes.forEach { it.registerInitiatedFlow(CargoCreationResponder::class.java) }
        startedNodes.forEach { it.registerInitiatedFlow(TruckCreationResponder::class.java) }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    fun `Cargo Creation, signedTransaction returned by the flow is signed by the acceptor`() {
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

        val flow = CreateCargoFlow(
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
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifyRequiredSignatures()
    }

    @Test
    fun `Truck Creation, signedTransaction returned by the flow is signed by the acceptor`() {
        val licensePlate = "835TPL4T3"

        val flow = CreateTruckFlow(
            licensePlate
        )
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifyRequiredSignatures()
    }

}