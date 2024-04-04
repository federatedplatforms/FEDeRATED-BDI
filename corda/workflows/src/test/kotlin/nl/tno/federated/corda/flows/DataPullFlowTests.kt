package nl.tno.federated.corda.flows

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkNotarySpec
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.MockServices
import net.corda.testing.node.StartedMockNode
import nl.tno.federated.corda.services.data.fetcher.DataFetcherCordaService
import nl.tno.federated.corda.states.DataPullState
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DataPullFlowTests {
    private lateinit var network: MockNetwork
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode

    private val aName = CordaX500Name("PartyA", "Reykjavik", "IS")
    private val bName = CordaX500Name("PartyB", "Rotterdam", "NL")

    @Before
    fun setup() {
        network = MockNetwork(
            listOf("nl.tno.federated"),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary", "Brussels", "BE"))),
            networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )

        a = network.createNode(MockNodeParameters(legalName = aName))
        a = network.createNode(MockNodeParameters(legalName = aName))
        b = network.createNode(MockNodeParameters(legalName = bName))

        val startedNodes = arrayListOf(a, b)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach {
            it.registerInitiatedFlow(DataPullQueryResponderFlow::class.java)
            it.registerInitiatedFlow(DataPullResultResponderFlow::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unmockkAll()
    }

    ////////////////////
    ////// TESTS ///////
    ////////////////////

    @Test
    fun `Simple data pull flow test`() {
        val query = "Very special Query"
        val flow = DataPullFlow(bName, query)
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifyRequiredSignatures()

        // The following assertion verifies that the state eventually saved in the vault actually contains the result of the query
        val resultOfTheQueryInRequesterVault = a.services.vaultService.queryBy<DataPullState>().states.single().state.data
        assertEquals(query, resultOfTheQueryInRequesterVault.results, "The result of the query must be in the state at the end of the flows")

        // "Proxy" test for L1
        // The following assertion verifies that when you use the info you get returned from the flow (a SignedTransaction) you
        // can still filter the states in the vault and retrieve the result. This mimics the behaviour of the application at L1
        val uuidOfStateWithResult = (signedTx.coreTransaction.getOutput(0) as DataPullState).linearId.id
        val resultOfTheQueryInRequesterVaultFromUUID = a.services.vaultService.queryBy<DataPullState>().states
            .filter { it.state.data.linearId.id == uuidOfStateWithResult }
        assertEquals(1, resultOfTheQueryInRequesterVaultFromUUID.size, "Exactly 1 state should be retrieved by the search in the vault via UUID")
        assertEquals(query, resultOfTheQueryInRequesterVaultFromUUID.single().state.data.results, "The result of the query must be in the state with UUID retrieved from the SignedTransaction")
    }
}