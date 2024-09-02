package nl.tno.federated.api.event.distribution.rules

import io.mockk.every
import io.mockk.mockk
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.NodeInfo
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import org.junit.Assert.assertEquals
import org.junit.Test

import java.security.PublicKey
import kotlin.test.assertTrue

class BroadcastEventDistributionRuleTest {

    private val destination = CordaX500Name("TNO", "Den Haag", "NL")
    private val destinations = listOf(CordaEventDestination(destination))

    private val cordaNodeService = mockk<CordaNodeService>()
    private val nodeInfo = mockk<NodeInfo>()
    private val owningKey = mockk<PublicKey>()

    private val rule = BroadcastEventDistributionRule(cordaNodeService = cordaNodeService)

    @Test
    fun getDestinations() {
        every { nodeInfo.legalIdentities }.returns(listOf(Party(destination, owningKey)))
        every { cordaNodeService.getPeersExcludingSelfAndNotary() } returns listOf(nodeInfo.legalIdentities.first())

        val destination1 = destinations.first().destination
        val destination2 = rule.getDestinations().first()

        val dest = CordaEventDestination.parse(destination2)
        assertEquals(destination1, dest.destination)
    }

    @Test
    fun appliesTo() {
        assertTrue { rule.appliesTo("rdfEventString") }
    }
}