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
 * ActorActorLegalPersonInner
 */

@JsonTypeName("Actor_actorLegalPerson_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class ActorActorLegalPersonInner {

  @JsonProperty("legalPersonAddress")
  @Valid
  private List<ActorActorLegalPersonInnerLegalPersonAddressInner> legalPersonAddress = new ArrayList<>();

  @JsonProperty("legalPersonName")
  @Valid
  private List<String> legalPersonName = new ArrayList<>();

  @JsonProperty("legalPersonID")
  @Valid
  private List<String> legalPersonID = new ArrayList<>();

  public ActorActorLegalPersonInner legalPersonAddress(List<ActorActorLegalPersonInnerLegalPersonAddressInner> legalPersonAddress) {
    this.legalPersonAddress = legalPersonAddress;
    return this;
  }

  public ActorActorLegalPersonInner addLegalPersonAddressItem(ActorActorLegalPersonInnerLegalPersonAddressInner legalPersonAddressItem) {
    this.legalPersonAddress.add(legalPersonAddressItem);
    return this;
  }

  /**
   * Get legalPersonAddress
   * @return legalPersonAddress
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "legalPersonAddress", required = true)
  public List<ActorActorLegalPersonInnerLegalPersonAddressInner> getLegalPersonAddress() {
    return legalPersonAddress;
  }

  public void setLegalPersonAddress(List<ActorActorLegalPersonInnerLegalPersonAddressInner> legalPersonAddress) {
    this.legalPersonAddress = legalPersonAddress;
  }

  public ActorActorLegalPersonInner legalPersonName(List<String> legalPersonName) {
    this.legalPersonName = legalPersonName;
    return this;
  }

  public ActorActorLegalPersonInner addLegalPersonNameItem(String legalPersonNameItem) {
    this.legalPersonName.add(legalPersonNameItem);
    return this;
  }

  /**
   * Get legalPersonName
   * @return legalPersonName
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "legalPersonName", required = true)
  public List<String> getLegalPersonName() {
    return legalPersonName;
  }

  public void setLegalPersonName(List<String> legalPersonName) {
    this.legalPersonName = legalPersonName;
  }

  public ActorActorLegalPersonInner legalPersonID(List<String> legalPersonID) {
    this.legalPersonID = legalPersonID;
    return this;
  }

  public ActorActorLegalPersonInner addLegalPersonIDItem(String legalPersonIDItem) {
    this.legalPersonID.add(legalPersonIDItem);
    return this;
  }

  /**
   * Get legalPersonID
   * @return legalPersonID
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "legalPersonID", required = true)
  public List<String> getLegalPersonID() {
    return legalPersonID;
  }

  public void setLegalPersonID(List<String> legalPersonID) {
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
    ActorActorLegalPersonInner actorActorLegalPersonInner = (ActorActorLegalPersonInner) o;
    return Objects.equals(this.legalPersonAddress, actorActorLegalPersonInner.legalPersonAddress) &&
        Objects.equals(this.legalPersonName, actorActorLegalPersonInner.legalPersonName) &&
        Objects.equals(this.legalPersonID, actorActorLegalPersonInner.legalPersonID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(legalPersonAddress, legalPersonName, legalPersonID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ActorActorLegalPersonInner {\n");
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

