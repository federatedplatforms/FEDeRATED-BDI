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
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T10:20:12.669+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonInnerLegalPersonAddressInner {

  @Valid
  private List<String> postalCode = new ArrayList<>();

  @Valid
  private List<String> locatedAtStreetName = new ArrayList<>();

  @Valid
  private List<String> postalAddress = new ArrayList<>();

  @Valid
  private List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry = new ArrayList<>();

  @Valid
  private List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity = new ArrayList<>();

  /**
   * Default constructor
   * @deprecated Use {@link ActorActorLegalPersonInnerLegalPersonAddressInner#ActorActorLegalPersonInnerLegalPersonAddressInner(List<String>, List<String>, List<String>, List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner>, List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner>)}
   */
  @Deprecated
  public ActorActorLegalPersonInnerLegalPersonAddressInner() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorActorLegalPersonInnerLegalPersonAddressInner(List<String> postalCode, List<String> locatedAtStreetName, List<String> postalAddress, List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry, List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity) {
    this.postalCode = postalCode;
    this.locatedAtStreetName = locatedAtStreetName;
    this.postalAddress = postalAddress;
    this.locatedInCountry = locatedInCountry;
    this.locatedInCity = locatedInCity;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner postalCode(List<String> postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addPostalCodeItem(String postalCodeItem) {
    if (this.postalCode == null) {
      this.postalCode = new ArrayList<>();
    }
    this.postalCode.add(postalCodeItem);
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "postalCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalCode")
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
    if (this.locatedAtStreetName == null) {
      this.locatedAtStreetName = new ArrayList<>();
    }
    this.locatedAtStreetName.add(locatedAtStreetNameItem);
    return this;
  }

  /**
   * Get locatedAtStreetName
   * @return locatedAtStreetName
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "locatedAtStreetName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedAtStreetName")
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
    if (this.postalAddress == null) {
      this.postalAddress = new ArrayList<>();
    }
    this.postalAddress.add(postalAddressItem);
    return this;
  }

  /**
   * Get postalAddress
   * @return postalAddress
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "postalAddress", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalAddress")
  public List<String> getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(List<String> postalAddress) {
    this.postalAddress = postalAddress;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner locatedInCountry(List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addLocatedInCountryItem(ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner locatedInCountryItem) {
    if (this.locatedInCountry == null) {
      this.locatedInCountry = new ArrayList<>();
    }
    this.locatedInCountry.add(locatedInCountryItem);
    return this;
  }

  /**
   * Get locatedInCountry
   * @return locatedInCountry
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "locatedInCountry", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCountry")
  public List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> getLocatedInCountry() {
    return locatedInCountry;
  }

  public void setLocatedInCountry(List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner> locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner locatedInCity(List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity) {
    this.locatedInCity = locatedInCity;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInner addLocatedInCityItem(ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner locatedInCityItem) {
    if (this.locatedInCity == null) {
      this.locatedInCity = new ArrayList<>();
    }
    this.locatedInCity.add(locatedInCityItem);
    return this;
  }

  /**
   * Get locatedInCity
   * @return locatedInCity
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "locatedInCity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCity")
  public List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> getLocatedInCity() {
    return locatedInCity;
  }

  public void setLocatedInCity(List<@Valid ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner> locatedInCity) {
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

