package nl.tno.federated

import net.corda.core.contracts.UniqueIdentifier
import nl.tno.federated.services.GraphDBService
import nl.tno.federated.states.EventState
import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals


class GraphDBServiceTests {

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Query everything`() {
        val result = GraphDBService.queryData()
        assertEquals("", result)
    }

    @Test
    fun `Insert new event`() {
        val event = ""
        val result = GraphDBService.insertEvent()
        assert(result)
    }


    @Test
    fun `Verify repository is available`() {
        val resultTrue = GraphDBService.isRepositoryAvailable()
        assert(resultTrue)
    }

    @Test
    fun `Validate invalid event - nonsense RDF`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = "invalid RDF",
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(!GraphDBService.isDataValid(eventState))
    }

    @Test
    fun `Validate invalid event - valid RDF`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = "valid RDF but irrelevant", // TODO
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(!GraphDBService.isDataValid(eventState))
    }

    @Test
    fun `Validate valid event`() {
        val eventState = EventState(emptyList(),
            transportMean = emptyList(),
            location = listOf(UUID.randomUUID()),
            otherDigitalTwins = listOf(UUID.randomUUID()),
            timestamps = linkedMapOf(Pair(EventType.ESTIMATED, Date())),
            ecmruri = "",
            milestone = Milestone.START,
            fullEvent = "valid RDF", // TODO
            participants = emptyList(),
            linearId = UniqueIdentifier()
        )
        assert(GraphDBService.isDataValid(eventState))
    }
}