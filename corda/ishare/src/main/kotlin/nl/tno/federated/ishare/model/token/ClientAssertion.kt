package nl.tno.federated.ishare.model.token

data class ClientAssertion(
    val header: Header,
    val body: Body,
    val signature: String
)

data class Header(
    val alg: String,
    val typ: String,
    val x5c: List<String>
)

data class Body(
    val iss: String,
    val sub: String,
    val aud: String,
    val jti: String,
    val exp: Int,
    val iat: Int
)
