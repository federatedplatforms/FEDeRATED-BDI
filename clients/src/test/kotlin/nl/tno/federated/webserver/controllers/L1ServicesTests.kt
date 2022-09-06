package nl.tno.federated.webserver.controllers

import nl.tno.federated.states.EventType
import nl.tno.federated.states.Milestone
import nl.tno.federated.webserver.L1Services.getIBMIdentityToken // cannot import if function is private
import nl.tno.federated.webserver.L1Services.getSolutionToken
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.util.*
import kotlin.test.assertEquals


class L1ServicesTests {

    @Before
    fun setup() {
    }

    @After
    fun tearDown() {
    }

//    @Ignore
    @Test // Not a real test, here just to inspect what getIBMIdentityToken() is returning
    fun `Get IBM Identity Token`() {
//         Make getIBMIdentityToken() internal if you want to test it singularly.
        val result = getIBMIdentityToken()
        print(result)
    }

    @Ignore
    @Test // Not a real test, here just to inspect what getIBMIdentityToken() is returning
    fun `Get Solution Token`() {
        val result = getSolutionToken()
        print(result)
    }

}