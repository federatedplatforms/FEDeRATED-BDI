package nl.tno.federated

import nl.tno.federated.services.GraphDBService
import org.junit.After
import org.junit.Before
import org.junit.Test
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
        assertEquals(true, result)
    }



    @Test
    fun `Verify repository is available`() {
        val resultTrue = GraphDBService.isRepositoryAvailable()
        assertEquals(true, resultTrue)
    }
}