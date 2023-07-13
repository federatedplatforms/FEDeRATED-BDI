package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * ActorActorLegalPerson
 */

@JsonTypeName("Actor_actorLegalPerson")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-13T16:38:33.391+02:00[Europe/Amsterdam]")
public class ActorActorLegalPerson {

  private ActorActorLegalPersonLegalPersonAddress legalPersonAddress;

  private String legalPersonName;

  private String legalPersonID;

  /**
   * Default constructor
   * @deprecated Use {@link ActorActorLegalPerson#ActorActorLegalPerson(ActorActorLegalPersonLegalPersonAddress, String, String)}
   */
  @Deprecated
  public ActorActorLegalPerson() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ActorActorLegalPerson(ActorActorLegalPersonLegalPersonAddress legalPersonAddress, String legalPersonName, String legalPersonID) {
    this.legalPersonAddress = legalPersonAddress;
    this.legalPersonName = legalPersonName;
    this.legalPersonID = legalPersonID;
  }

  public ActorActorLegalPerson legalPersonAddress(ActorActorLegalPersonLegalPersonAddress legalPersonAddress) {
    this.legalPersonAddress = legalPersonAddress;
    return this;
  }

  /**
   * Get legalPersonAddress
   * @return legalPersonAddress
  */
  @NotNull @Valid 
  @Schema(name = "legalPersonAddress", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("legalPersonAddress")
  public ActorActorLegalPersonLegalPersonAddress getLegalPersonAddress() {
    return legalPersonAddress;
  }

  public void setLegalPersonAddress(ActorActorLegalPersonLegalPersonAddress legalPersonAddress) {
    this.legalPersonAddress = legalPersonAddress;
  }

  public ActorActorLegalPerson legalPersonName(String legalPersonName) {
    this.legalPersonName = legalPersonName;
    return this;
  }

  /**
   * Get legalPersonName
   * @return legalPersonName
  */
  @NotNull 
  @Schema(name = "legalPersonName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("legalPersonName")
  public String getLegalPersonName() {
    return legalPersonName;
  }

  public void setLegalPersonName(String legalPersonName) {
    this.legalPersonName = legalPersonName;
  }

  public ActorActorLegalPerson legalPersonID(String legalPersonID) {
    this.legalPersonID = legalPersonID;
    return this;
  }

  /**
   * Get legalPersonID
   * @return legalPersonID
  */
  @NotNull 
  @Schema(name = "legalPersonID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("legalPersonID")
  public String getLegalPersonID() {
    return legalPersonID;
  }

  public void setLegalPersonID(String legalPersonID) {
    this.legalPersonID = legalPersonID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ActorActorLegalPerson actorActorLegalPerson = (ActorActorLegalPerson) o;
    return Objects.equals(this.legalPersonAddress, actorActorLegalPerson.legalPersonAddress) &&
        Objects.equals(this.legalPersonName, actorActorLegalPerson.legalPersonName) &&
        Objects.equals(this.legalPersonID, actorActorLegalPerson.legalPersonID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(legalPersonAddress, legalPersonName, legalPersonID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPerson {\n");
    sb.append("    legalPersonAddress: ").append(toIndentedString(legalPersonAddress)).append("\n");
    sb.append("    legalPersonName: ").append(toIndentedString(legalPersonName)).append("\n");
    sb.append("    legalPersonID: ").append(toIndentedString(legalPersonID)).append("\n");
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

