package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
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
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class DataPullFlowTests {

    lateinit var network: MockNetwork
    lateinit var a: StartedMockNode
    lateinit var b: StartedMockNode
    lateinit var c: StartedMockNode
    lateinit var d: StartedMockNode

    private val countriesInvolved = setOf("NL", "DE", "FR")
    private val unknownCountries = setOf("NL", "ZZ")

    val fakeResult = "Very nice result, the best result ever, I've never seen such a good result. Let's make results great again."

    @Before
    fun setup() {

        mockkObject(GraphDBService)
        every { GraphDBService.generalSPARQLquery(any()) } returns fakeResult

        network = MockNetwork(
                listOf("nl.tno.federated"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","Brussels","BE"))),
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4)
        )
        a = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyA","Reykjavik","IS")))
        b = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyB","Rotterdam","NL")))
        c = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyC","Berlin","DE")))
        d = network.createNode(MockNodeParameters(legalName = CordaX500Name("PartyD","Paris","FR")))
        val startedNodes = arrayListOf(a, b, c, d)

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(DataPullQueryResponderFlow::class.java) }
        startedNodes.forEach { it.registerInitiatedFlow(DataPullResultResponderFlow::class.java) }
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

        val flow = DataPullQueryFlow("PartyB", "Very special Query")
        val future = a.startFlow(flow)
        network.runNetwork()

        val signedTx = future.getOrThrow()
        signedTx.verifySignaturesExcept(a.info.singleIdentity().owningKey)

        val resultOfTheQueryInRequesterVault = a.services.vaultService.queryBy<DataPullState>().states.single().state.data

        println("STATE IN THE VAULT")
        print(resultOfTheQueryInRequesterVault)

        assertTrue{
            resultOfTheQueryInRequesterVault.result.single() == fakeResult
        }
    }
}