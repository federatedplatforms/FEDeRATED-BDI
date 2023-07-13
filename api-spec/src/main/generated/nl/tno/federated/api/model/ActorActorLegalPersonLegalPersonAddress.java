package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * ActorActorLegalPersonLegalPersonAddress
 */

@JsonTypeName("Actor_actorLegalPerson_legalPersonAddress")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-13T16:38:33.391+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonLegalPersonAddress {

  private String postalCode;

  private String locatedAtStreetName;

  private String postalAddress;

  private ActorActorLegalPersonLegalPersonAddressLocatedInCountry locatedInCountry;

  private ActorActorLegalPersonLegalPersonAddressLocatedInCity locatedInCity;

  /**
   * Default constructor
   * @deprecated Use {@link ActorActorLegalPersonLegalPersonAddress#ActorActorLegalPersonLegalPersonAddress(String, String, String, ActorActorLegalPersonLegalPersonAddressLocatedInCountry, ActorActorLegalPersonLegalPersonAddressLocatedInCity)}
   */
  @Deprecated
  public ActorActorLegalPersonLegalPersonAddress() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorActorLegalPersonLegalPersonAddress(String postalCode, String locatedAtStreetName, String postalAddress, ActorActorLegalPersonLegalPersonAddressLocatedInCountry locatedInCountry, ActorActorLegalPersonLegalPersonAddressLocatedInCity locatedInCity) {
    this.postalCode = postalCode;
    this.locatedAtStreetName = locatedAtStreetName;
    this.postalAddress = postalAddress;
    this.locatedInCountry = locatedInCountry;
    this.locatedInCity = locatedInCity;
  }

  public ActorActorLegalPersonLegalPersonAddress postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  @NotNull 
  @Schema(name = "postalCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public ActorActorLegalPersonLegalPersonAddress locatedAtStreetName(String locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
    return this;
  }

  /**
   * Get locatedAtStreetName
   * @return locatedAtStreetName
  */
  @NotNull 
  @Schema(name = "locatedAtStreetName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedAtStreetName")
  public String getLocatedAtStreetName() {
    return locatedAtStreetName;
  }

  public void setLocatedAtStreetName(String locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
  }

  public ActorActorLegalPersonLegalPersonAddress postalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
    return this;
  }

  /**
   * Get postalAddress
   * @return postalAddress
  */
  @NotNull 
  @Schema(name = "postalAddress", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalAddress")
  public String getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  public ActorActorLegalPersonLegalPersonAddress locatedInCountry(ActorActorLegalPersonLegalPersonAddressLocatedInCountry locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
    return this;
  }

  /**
   * Get locatedInCountry
   * @return locatedInCountry
  */
  @NotNull @Valid 
  @Schema(name = "locatedInCountry", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCountry")
  public ActorActorLegalPersonLegalPersonAddressLocatedInCountry getLocatedInCountry() {
    return locatedInCountry;
  }

  public void setLocatedInCountry(ActorActorLegalPersonLegalPersonAddressLocatedInCountry locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
  }

  public ActorActorLegalPersonLegalPersonAddress locatedInCity(ActorActorLegalPersonLegalPersonAddressLocatedInCity locatedInCity) {
    this.locatedInCity = locatedInCity;
    return this;
  }

  /**
   * Get locatedInCity
   * @return locatedInCity
  */
  @NotNull @Valid 
  @Schema(name = "locatedInCity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCity")
  public ActorActorLegalPersonLegalPersonAddressLocatedInCity getLocatedInCity() {
    return locatedInCity;
  }

  public void setLocatedInCity(ActorActorLegalPersonLegalPersonAddressLocatedInCity locatedInCity) {
    this.locatedInCity = locatedInCity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorActorLegalPersonLegalPersonAddress actorActorLegalPersonLegalPersonAddress = (ActorActorLegalPersonLegalPersonAddress) o;
    return Objects.equals(this.postalCode, actorActorLegalPersonLegalPersonAddress.postalCode) &&
        Objects.equals(this.locatedAtStreetName, actorActorLegalPersonLegalPersonAddress.locatedAtStreetName) &&
        Objects.equals(this.postalAddress, actorActorLegalPersonLegalPersonAddress.postalAddress) &&
        Objects.equals(this.locatedInCountry, actorActorLegalPersonLegalPersonAddress.locatedInCountry) &&
        Objects.equals(this.locatedInCity, actorActorLegalPersonLegalPersonAddress.locatedInCity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postalCode, locatedAtStreetName, postalAddress, locatedInCountry, locatedInCity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonLegalPersonAddress {\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    locatedAtStreetName: ").append(toIndentedString(locatedAtStreetName)).append("\n");
    sb.append("    postalAddress: ").append(toIndentedString(postalAddress)).append("\n");
    sb.append("    locatedInCountry: ").append(toIndentedString(locatedInCountry)).append("\n");
    sb.append("    locatedInCity: ").append(toIndentedString(locatedInCity)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

