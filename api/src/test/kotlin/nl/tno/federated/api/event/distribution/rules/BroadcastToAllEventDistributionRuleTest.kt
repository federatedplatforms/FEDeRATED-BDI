package nl.tno.federated.api.event.distribution.rules

import io.mockk.every
import io.mockk.mockk
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.NodeInfo
import nl.tno.federated.api.corda.CordaNodeService
import nl.tno.federated.api.event.distribution.corda.CordaEventDestination
import org.junit.Test

import java.security.PublicKey

class BroadcastToAllEventDistributionRuleTest {

    private val destination = CordaX500Name("TNO", "Den Haag", "NL")
    private val destinations = listOf(CordaEventDestination(destination))

    private val cordaNodeService = mockk<CordaNodeService>()
    private val nodeInfo = mockk<NodeInfo>()
    private val owningKey = mockk<PublicKey>()

    private val rule = BroadcastToAllEventDistributionRule(cordaNodeService = cordaNodeService)

    @Test
    fun getDestinations() {
        every { nodeInfo.legalIdentities }.returns(listOf(Party(destination, owningKey)))
        every { cordaNodeService.getNetworkMapSnapshot() } returns listOf(nodeInfo)
        val destination1 = destinations.first().destination
        val destination2 = rule.getDestinations().first()
        kotlin.test.assertEquals(destination1.commonName, destination2.destination.commonName)
        kotlin.test.assertEquals(destination1.locality, destination2.destination.locality)
        kotlin.test.assertEquals(destination1.country, destination2.destination.country)
    }

    @Test
    fun appliesTo() {
        kotlin.test.assertTrue { rule.appliesTo("rdfEventString") }
    }
}