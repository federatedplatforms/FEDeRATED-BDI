package nl.tno.federated.ishare.model.token

data class ConsumerAccessToken(
    val header: AccessTokenHeader,
    val body: AccessTokenBody,
    val signature: String
)

data class AccessTokenHeader(
    val alg: String,
    val typ: String
)

data class AccessTokenBody(
    val iss: String,
    val sub: String,
    val exp: Int,
    val aud: String,
    val jti: String,
    val iat: Int
)
