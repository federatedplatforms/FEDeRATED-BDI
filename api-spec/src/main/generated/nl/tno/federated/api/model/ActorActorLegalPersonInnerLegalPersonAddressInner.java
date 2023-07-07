package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ActorActorLegalPersonInnerLegalPersonAddressInner
 */

@JsonTypeName("Actor_actorLegalPerson_inner_legalPersonAddress_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonInnerLegalPersonAddressInner {

  @JsonProperty("postalCode")
  @Valid
  private List<String> postalCode = new ArrayList<>();

  @JsonProperty("locatedAtStreetName")
  @Valid
  private List<String> locatedAtStreetName = new ArrayList<>();

  @JsonProperty("postalAddress")
  @Valid
  private List<String> postalAddress = new ArrayList<>();

  @JsonProperty("locatedInCountry")
  @Valid
  private List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry = new ArrayList<>();

  @JsonProperty("locatedInCity")
  @Valid
  private List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity = new ArrayList<>();

  public ActorActorLegalPersonInnerLegalPersonAddressInner postalCode(List<String> postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addPostalCodeItem(String postalCodeItem) {
    this.postalCode.add(postalCodeItem);
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "postalCode", required = true)
  public List<String> getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(List<String> postalCode) {
    this.postalCode = postalCode;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner locatedAtStreetName(List<String> locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addLocatedAtStreetNameItem(String locatedAtStreetNameItem) {
    this.locatedAtStreetName.add(locatedAtStreetNameItem);
    return this;
  }

  /**
   * Get locatedAtStreetName
   * @return locatedAtStreetName
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "locatedAtStreetName", required = true)
  public List<String> getLocatedAtStreetName() {
    return locatedAtStreetName;
  }

  public void setLocatedAtStreetName(List<String> locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner postalAddress(List<String> postalAddress) {
    this.postalAddress = postalAddress;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addPostalAddressItem(String postalAddressItem) {
    this.postalAddress.add(postalAddressItem);
    return this;
  }

  /**
   * Get postalAddress
   * @return postalAddress
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "postalAddress", required = true)
  public List<String> getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(List<String> postalAddress) {
    this.postalAddress = postalAddress;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner locatedInCountry(List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addLocatedInCountryItem(ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner locatedInCountryItem) {
    this.locatedInCountry.add(locatedInCountryItem);
    return this;
  }

  /**
   * Get locatedInCountry
   * @return locatedInCountry
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "locatedInCountry", required = true)
  public List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> getLocatedInCountry() {
    return locatedInCountry;
  }

  public void setLocatedInCountry(List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner locatedInCity(List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity) {
    this.locatedInCity = locatedInCity;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addLocatedInCityItem(ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner locatedInCityItem) {
    this.locatedInCity.add(locatedInCityItem);
    return this;
  }

  /**
   * Get locatedInCity
   * @return locatedInCity
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "locatedInCity", required = true)
  public List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> getLocatedInCity() {
    return locatedInCity;
  }

  public void setLocatedInCity(List<ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity) {
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
    ActorActorLegalPersonInnerLegalPersonAddressInner actorActorLegalPersonInnerLegalPersonAddressInner = (ActorActorLegalPersonInnerLegalPersonAddressInner) o;
    return Objects.equals(this.postalCode, actorActorLegalPersonInnerLegalPersonAddressInner.postalCode) &&
        Objects.equals(this.locatedAtStreetName, actorActorLegalPersonInnerLegalPersonAddressInner.locatedAtStreetName) &&
        Objects.equals(this.postalAddress, actorActorLegalPersonInnerLegalPersonAddressInner.postalAddress) &&
        Objects.equals(this.locatedInCountry, actorActorLegalPersonInnerLegalPersonAddressInner.locatedInCountry) &&
        Objects.equals(this.locatedInCity, actorActorLegalPersonInnerLegalPersonAddressInner.locatedInCity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(postalCode, locatedAtStreetName, postalAddress, locatedInCountry, locatedInCity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonInnerLegalPersonAddressInner {\n");
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

