package nl.tno.federated.ishare.model.party

data class PartiesToken(
    val iss: String,
    val sub: String?,
    val exp: Int,
    val iat: Int,
    val jti: String,
    val aud: String,
    val parties_info: PartyInfo
)

data class PartyInfo(
   val count: Int,
   val data: List<PartyData>
)

data class PartyData(
    val party_id: String,
    val party_name: String,
    val adherence: PartyAdherence,
    val certifications: List<PartyCertification>
)

data class PartyAdherence(
    val status: String,
    val start_date: String,
    val end_date: String
)

data class PartyCertification(
   val role: String,
   val start_date: String,
   val end_date: String,
   val loa: Int
)
