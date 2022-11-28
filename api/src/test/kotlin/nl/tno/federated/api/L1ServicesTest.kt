package nl.tno.federated.api

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner::class)
class L1ServicesTest {

    @Autowired
    lateinit var l1Services: L1Services

    @MockBean(name = "ishareRestTemplate")
    lateinit var ishareRestTemplate: RestTemplate

    @Test
    fun `extractAccessTokenFromHeader`() {
        assertEquals("SOMETHINGHASHED", l1Services.extractAccessTokenFromHeader("Bearer SOMETHINGHASHED"))
    }

    @Test(expected = ResponseStatusException::class)
    fun `extractAccessTokenFromHeaderIncorrectFormat`() {
        l1Services.extractAccessTokenFromHeader("BearerSOMETHINGHASHED")
    }

    @Test
    fun `validateToken`() {
        `when`(ishareRestTemplate.exchange(eq("/validate/token"), eq(HttpMethod.POST), any(HttpEntity::class.java), eq(String::class.java)))
            .thenReturn(ResponseEntity.ok("""{ "success" : "true" }"""))
        assertTrue(l1Services.validateToken("xxxxx.yyyyy.zzzzz"))
    }

    @Test
    fun `userIsAuthorizedWithIncorrectToken`() {
        assertFalse(l1Services.userIsAuthorized("noway"))
    }

    @Test
    fun `userIsAuthorizedWithBuildInBackdoorToken`() {
        assertTrue(l1Services.userIsAuthorized("doitanyway"))
    }
}