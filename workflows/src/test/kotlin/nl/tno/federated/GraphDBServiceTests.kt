package nl.tno.federated

import io.mockk.every
import io.mockk.mockkObject
import nl.tno.federated.services.GraphDBService
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


class GraphDBServiceTests {

    @Before
    fun setup() {
        mockkObject(GraphDBService) // applies mocking to an Object
        every { GraphDBService.validateData() } returns 200


    }

    @After
    fun tearDown() {
    }

    @Test
    fun `Simple flow transaction`() {
        assertEquals(GraphDBService.validateData(),200)
    }

}