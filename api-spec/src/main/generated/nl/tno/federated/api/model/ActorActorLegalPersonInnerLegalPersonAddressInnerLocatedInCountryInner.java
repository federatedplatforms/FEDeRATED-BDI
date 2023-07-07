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
 * ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner
 */

@JsonTypeName("Actor_actorLegalPerson_inner_legalPersonAddress_inner_locatedInCountry_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner {

  @JsonProperty("countryISOCode")
  @Valid
  private List<Object> countryISOCode = new ArrayList<>();

  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner countryISOCode(List<Object> countryISOCode) {
    this.countryISOCode = countryISOCode;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner addCountryISOCodeItem(Object countryISOCodeItem) {
    this.countryISOCode.add(countryISOCodeItem);
    return this;
  }

  /**
   * Get countryISOCode
   * @return countryISOCode
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "countryISOCode", required = true)
  public List<Object> getCountryISOCode() {
    return countryISOCode;
  }

  public void setCountryISOCode(List<Object> countryISOCode) {
    this.countryISOCode = countryISOCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner actorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner = (ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner) o;
    return Objects.equals(this.countryISOCode, actorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner.countryISOCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(countryISOCode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCountryInner {\n");
    sb.append("    countryISOCode: ").append(toIndentedString(countryISOCode)).append("\n");
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

