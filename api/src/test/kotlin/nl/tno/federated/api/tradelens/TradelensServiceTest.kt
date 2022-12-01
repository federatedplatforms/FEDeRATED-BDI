package nl.tno.federated.api.tradelens

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = ["tradelens.orgId=123", "tradelens.apikey=345"] // override with fake orgId and apikey for our test.
)
@RunWith(SpringRunner::class)
class TradelensServiceTest {

    @Autowired
    private lateinit var tradelensService: TradelensService

    @Autowired
    private lateinit var ibmIdentityTokenRestTemplate: RestTemplate

    @Autowired
    private lateinit var tradelensRestTemplate: RestTemplate

    private lateinit var ibmMockServer: MockRestServiceServer
    private lateinit var tradelensMockServer: MockRestServiceServer

    @Before
    fun before() {
        ibmMockServer = MockRestServiceServer.createServer(ibmIdentityTokenRestTemplate);
        tradelensMockServer = MockRestServiceServer.createServer(tradelensRestTemplate);
    }

    /**
     * Retrieving the tradelens data requires retrieval of an access_token and solution_token for the tradelens calls.
     * This test simulates the interaction with the ibm identity token endpoint, the tradelens exchange token endpoint and retrieval of the transport equipment data.
     */
    @Test
    fun getTransportEquipmentDataTest() {

        val consigmentId = "any"
        val twindId = UUID.randomUUID()
        val solutionToken = "some_value"
        val sampleResponse = "tradelens response"

        // First interaction is retrieving the identity token using the API key
        ibmMockServer.expect(ExpectedCount.once(),
            requestTo(URI("https://iam.cloud.ibm.com:443/identity/token")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string("grant_type=urn:ibm:params:oauth:grant-type:apikey&apikey=345"))
            .andRespond(withStatus(HttpStatus.OK)
                .body("""{"access_token":"token","refresh_token":"not_supported","token_type":"Bearer","expires_in":3600,"expiration":1666874438,"scope":"ibm openid"}""")
            )

        // Next we fetch the solution token with the organisationId and the access token
        tradelensMockServer.expect(ExpectedCount.once(),
            requestTo(URI("https://platform-sandbox.tradelens.com:443/sa/api/v1/auth/exchange_token/organizations/123")))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().string("""{"access_token":"token","refresh_token":"not_supported","token_type":"Bearer","expires_in":3600,"expiration":1666874438,"scope":"ibm openid"}"""))
            .andRespond(withStatus(HttpStatus.OK)
                .body("""{"solution_token":"$solutionToken"}""")
            )

        tradelensMockServer.expect(ExpectedCount.once(),
            requestTo(URI("https://platform-sandbox.tradelens.com:443/api/v1/transportEquipment/currentProgress/consignmentId/$consigmentId/transportEquipmentId/$twindId")))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $solutionToken"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .body(sampleResponse)
            )

        // response body:
        val data = tradelensService.getTransportEquipmentData(consigmentId, twindId)
        assertEquals(data, sampleResponse)

    }
}