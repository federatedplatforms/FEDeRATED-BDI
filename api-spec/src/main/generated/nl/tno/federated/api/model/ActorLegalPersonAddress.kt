package nl.tno.federated.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.Valid
import javax.validation.constraints.Size

/**
 *
 * @param postalCode
 * @param locatedAtStreetName
 * @param postalAddress
 * @param locatedInCountry
 * @param locatedInCity
 */
data class ActorLegalPersonAddress(

    @get:Size(min = 1)
    @field:JsonProperty("postalCode", required = true) val postalCode: kotlin.collections.List<kotlin.String>,

    @get:Size(min = 1)
    @field:JsonProperty("locatedAtStreetName", required = true) val locatedAtStreetName: kotlin.collections.List<kotlin.String>,

    @get:Size(min = 1)
    @field:JsonProperty("postalAddress", required = true) val postalAddress: kotlin.collections.List<kotlin.String>,

    @field:Valid
    @get:Size(min = 1)
    @field:JsonProperty("locatedInCountry", required = true) val locatedInCountry: kotlin.collections.List<kotlin.String>,

    @field:Valid
    @get:Size(min = 1)
    @field:JsonProperty("locatedInCity", required = true) val locatedInCity: kotlin.collections.List<kotlin.String>
) {

}

