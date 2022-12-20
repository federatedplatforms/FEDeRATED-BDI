package nl.tno.federated.corda.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.utilities.unwrap
import nl.tno.federated.corda.services.ishare.ISHARECordaService
import nl.tno.federated.ishare.ISHAREException
import nl.tno.federated.ishare.model.token.ISHARETokenRequest
import nl.tno.federated.ishare.model.token.ISHARETokenResponse
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

@InitiatingFlow
class ISHARETokenFlow(private val participant: Party) : FlowLogic<TokenResponse>() {
    /**
     * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
     * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
     */
    companion object {
        object STARTING_TOKENPROCES : Step("Starting.")
        object CREATING_CLIENTASSERTION : Step("Creating iSHARE Client Assertion.")
        object CREATING_TOKENREQUEST : Step("Putting client assertion into a token request.")
        object SENDING_TOKENREQUEST : Step("Sending the tokenrequest to party.")
        object EXTRACT_TOKEN : Step("Extracting the token.")
        object ENDING_TOKENPROCES : Step("All Done.")

        fun tracker() = ProgressTracker(
            STARTING_TOKENPROCES,
            CREATING_CLIENTASSERTION,
            CREATING_TOKENREQUEST,
            SENDING_TOKENREQUEST,
            EXTRACT_TOKEN,
            ENDING_TOKENPROCES
        )

        val log = LoggerFactory.getLogger(ISHARETokenFlow::class.java)
    }

    override val progressTracker = tracker()

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    override fun call(): TokenResponse {
        log.info("iSHARE token flow called for: {}", participant.name)

        progressTracker.currentStep = STARTING_TOKENPROCES
        // Stage 1 : Create a tokenrequest
        progressTracker.currentStep = CREATING_TOKENREQUEST
        val tokenRequest = serviceHub.cordaService(ISHARECordaService::class.java).getTokenRequest().toCordaSerializable()
        // send a token request to each party involved, and save it in a map so the main flow can use them in sending the messages
        progressTracker.currentStep = SENDING_TOKENREQUEST
        val tokenSession = initiateFlow(participant)
        // receive the answer and extract token or error
        progressTracker.currentStep = SENDING_TOKENREQUEST
        val response = tokenSession.sendAndReceive<TokenResponse>(tokenRequest)
        progressTracker.currentStep = ENDING_TOKENPROCES
        return response.unwrap { data -> data }
    }
}

@InitiatedBy(ISHARETokenFlow::class)
class ISHARETokenResponder(val counterSession: FlowSession) : FlowLogic<Unit>() {

    companion object {
        object RECEIVING_TOKENREQUEST : Step("Receiving a token request")
        object VERIFYING_TOKENREQUEST : Step("Verifying the token request.")
        object CREATING_ACCESSTOKEN : Step("Creating an access token.")
        object RETURNING_ACCESSTOKEN : Step("Returning the token to the main flow.")

        fun tracker() = ProgressTracker(
            RECEIVING_TOKENREQUEST,
            VERIFYING_TOKENREQUEST,
            CREATING_ACCESSTOKEN,
            RETURNING_ACCESSTOKEN
        )

        val log = LoggerFactory.getLogger(ISHARETokenResponder::class.java)
    }

    override val progressTracker: ProgressTracker = tracker()

    @Suspendable
    override fun call() {
        progressTracker.currentStep = RECEIVING_TOKENREQUEST
        val request = counterSession.receive<TokenRequest>().unwrap { data -> data }

        log.info("iSHARE token responder flow called for request: {}", request)

        progressTracker.currentStep = RECEIVING_TOKENREQUEST
        val cordaService = serviceHub.cordaService(ISHARECordaService::class.java)
        try {
            val tokenCorrect = cordaService.checkTokenRequest(ISHARETokenRequest(request.grant_type, request.scope, request.client_id, request.client_assertion_type, request.client_assertion))
            if (!tokenCorrect.first) {
                log.warn("Error creating access token: client assertion is not valid: {}", request)
                counterSession.send(TokenResponse(error = TokenResponseError("Error creating access token: client assertion is not valid")))
            }
            val iSHAREMember: Boolean = await(
                // Pass in an implementation of [FlowExternalOperation]
                RetrieveDataFromExternalSystem(
                    cordaService,
                    request.client_id
                )
            )

            if (!iSHAREMember) {
                log.warn("Error creating access token: Participant is not active in the iSHARE scheme: {}", request)
                counterSession.send(TokenResponse(error = TokenResponseError("Error creating access token: Participant is not active in the iSHARE scheme")))
            }
        } catch (e: ISHAREException) {
            log.warn("Error creating access token: an error has occured, message: {}", e.message, e)
            counterSession.send(TokenResponse(error = TokenResponseError("Error creating access token: an error has occured (${e.message})")))
        }
        progressTracker.currentStep = CREATING_ACCESSTOKEN
        val iSHAREAccesstoken = cordaService.createTokenResponse(request.client_id).toCordaSerializable()
        progressTracker.currentStep = RETURNING_ACCESSTOKEN
        counterSession.send(iSHAREAccesstoken)
    }

    /**
     * https://docs.r3.com/en/platform/corda/4.9/community/api-flows.html#calling-external-systems-inside-of-flows
     */
    class RetrieveDataFromExternalSystem(
        private val ishareService: ISHARECordaService,
        private val partyEORI: String
    ) : FlowExternalOperation<Boolean> {

        // Implement [execute] which will be run on a thread outside of the flow's context
        override fun execute(deduplicationId: String): Boolean {
            return ishareService.checkPartyWithScheme(partyEORI)
        }
    }
}

private fun ISHARETokenRequest.toCordaSerializable() = TokenRequest(grant_type, scope, client_id, client_assertion_type, client_assertion)
private fun ISHARETokenResponse.toCordaSerializable() = TokenResponse(access_token, token_type, expires_in, scope)

@CordaSerializable
data class TokenRequest(
    val grant_type: String,
    val scope: String,
    val client_id: String,
    val client_assertion_type: String,
    val client_assertion: String
)

@CordaSerializable
data class TokenResponse(
    val access_token: String = "",
    val token_type: String? = "Bearer",
    val expires_in: Long? = Instant.now().plusSeconds(3600).epochSecond,
    val scope: String? = "iSHARE",
    val error: TokenResponseError? = null
)

@CordaSerializable
data class TokenResponseError(
    val message: String
)