package nl.tno.federated.ishare.model.token

import java.time.Instant

data class ISHAREAccessToken(
    val access_token: String = "",
    val token_type: String = "",
    val expires_in: Int = 0,
    val created: Long = 0,
    val scope: String? = null
) {
    fun hasExpired(): Boolean {
        return ((created + expires_in) < (Instant.now().epochSecond - 5))
    }
}