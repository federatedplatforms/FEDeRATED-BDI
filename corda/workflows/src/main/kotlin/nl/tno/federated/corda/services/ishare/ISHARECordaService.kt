package nl.tno.federated.corda.services.ishare

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import nl.tno.federated.ishare.ISHAREClient
import nl.tno.federated.ishare.model.token.ISHARETokenRequest
import nl.tno.federated.ishare.model.token.ISHARETokenResponse

/**
 * CordaService delegate that provides the ishare functionality
 */
@CordaService
class ISHARECordaService(serviceHub: AppServiceHub) : SingletonSerializeAsToken() {

    private val ishareClient by lazy { ISHAREClient("ishare.properties") }

    fun checkPartyWithScheme(partyEORI: String): Boolean = ishareClient.checkPartyWithScheme(partyEORI)

    fun checkAccessToken(token: String): Pair<Boolean, String> = ishareClient.checkAccessToken(token)

    fun checkTokenRequest(tokenRequest: ISHARETokenRequest): Pair<Boolean, String> = ishareClient.checkTokenRequest(tokenRequest)

    fun getTokenRequest(): ISHARETokenRequest = ishareClient.getTokenRequest()

    fun createTokenResponse(client_id: String): ISHARETokenResponse = ishareClient.createTokenResponse(client_id)

    fun ishareEnabled(): Boolean = ishareClient.ishareEnabled()
}