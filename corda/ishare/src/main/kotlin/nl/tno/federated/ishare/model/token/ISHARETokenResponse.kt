package nl.tno.federated.ishare.model.token

import java.time.Instant

data class ISHARETokenResponse(
    val access_token: String,
    val token_type: String? = "Bearer",
    val expires_in: Long? = Instant.now().plusSeconds(3600).epochSecond,
    val scope: String? = "iSHARE"
)