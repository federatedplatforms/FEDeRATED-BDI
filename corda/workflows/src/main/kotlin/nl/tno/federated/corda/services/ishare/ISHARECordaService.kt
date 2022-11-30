package nl.tno.federated.corda.services.ishare

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import nl.tno.federated.ishare.ISHAREClient
import nl.tno.federated.ishare.model.token.ISHARETokenRequest
import nl.tno.federated.ishare.model.token.ISHARETokenResponse
import org.slf4j.LoggerFactory

/**
 * CordaService delegate that provides the ishare functionality
 */
@CordaService
class ISHARECordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private val logger = LoggerFactory.getLogger(ISHARECordaService::class.java)
    private val ishareClient = ISHAREClient()

    fun checkPartyWithScheme(partyEORI: String): Boolean {
        return ishareClient.checkPartyWithScheme(partyEORI)
    }

    fun checkAccessToken(token: String): Pair<Boolean, String> {
        return ishareClient.checkAccessToken(token)
    }

    fun checkTokenRequest(tokenRequest: ISHARETokenRequest): Pair<Boolean, String> {
        return ishareClient.checkTokenRequest(tokenRequest)
    }

    fun getTokenRequest(): ISHARETokenRequest {
        return ishareClient.getTokenRequest()
    }

    fun createTokenResponse(client_id: String): ISHARETokenResponse {
        return ishareClient.createTokenResponse(client_id)
    }

    fun ishareEnabled(): Boolean = ishareClient.ishareEnabled()
}