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
 * ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner
 */

@JsonTypeName("Actor_actorLegalPerson_inner_legalPersonAddress_inner_locatedInCity_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T10:20:12.669+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner {

  @Valid
  private List<String> cityName = new ArrayList<>();

  /**
   * Default constructor
   * @deprecated Use {@link ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner#ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner(List<String>)}
   */
  @Deprecated
  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner(List<String> cityName) {
    this.cityName = cityName;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner cityName(List<String> cityName) {
    this.cityName = cityName;
    return this;
  }

  public ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner addCityNameItem(String cityNameItem) {
    if (this.cityName == null) {
      this.cityName = new ArrayList<>();
    }
    this.cityName.add(cityNameItem);
    return this;
  }

  /**
   * Get cityName
   * @return cityName
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "cityName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cityName")
  public List<String> getCityName() {
    return cityName;
  }

  public void setCityName(List<String> cityName) {
    this.cityName = cityName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner actorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner = (ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner) o;
    return Objects.equals(this.cityName, actorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner.cityName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cityName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonInnerLegalPersonAddressInnerLocatedInCityInner {\n");
    sb.append("    cityName: ").append(toIndentedString(cityName)).append("\n");
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

