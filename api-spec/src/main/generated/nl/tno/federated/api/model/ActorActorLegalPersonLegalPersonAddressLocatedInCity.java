package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * ActorActorLegalPersonLegalPersonAddressLocatedInCity
 */

@JsonTypeName("Actor_actorLegalPerson_legalPersonAddress_locatedInCity")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-13T16:38:33.391+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonLegalPersonAddressLocatedInCity {

  private String cityName;

  /**
   * Default constructor
   * @deprecated Use {@link ActorActorLegalPersonLegalPersonAddressLocatedInCity#ActorActorLegalPersonLegalPersonAddressLocatedInCity(String)}
   */
  @Deprecated
  public ActorActorLegalPersonLegalPersonAddressLocatedInCity() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorActorLegalPersonLegalPersonAddressLocatedInCity(String cityName) {
    this.cityName = cityName;
  }

  public ActorActorLegalPersonLegalPersonAddressLocatedInCity cityName(String cityName) {
    this.cityName = cityName;
    return this;
  }

  /**
   * Get cityName
   * @return cityName
  */
  @NotNull 
  @Schema(name = "cityName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cityName")
  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
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
    ActorActorLegalPersonLegalPersonAddressLocatedInCity actorActorLegalPersonLegalPersonAddressLocatedInCity = (ActorActorLegalPersonLegalPersonAddressLocatedInCity) o;
    return Objects.equals(this.cityName, actorActorLegalPersonLegalPersonAddressLocatedInCity.cityName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cityName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonLegalPersonAddressLocatedInCity {\n");
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

