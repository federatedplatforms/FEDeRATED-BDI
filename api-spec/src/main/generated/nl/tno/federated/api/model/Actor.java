package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Actor has a role and is described by a legal person - relevant for us: consignor, consignee, carrier
 */

@Schema(name = "Actor", description = "Actor has a role and is described by a legal person - relevant for us: consignor, consignee, carrier")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T10:20:12.669+02:00[Europe/Amsterdam]")
public class Actor {

  @Valid
  private List<@Valid ActorActorLegalPersonInner> actorLegalPerson = new ArrayList<>();

  /**
   * Gets or Sets actorLogisticsRole
   */
  public enum ActorLogisticsRoleEnum {
    CONSIGNEE("Consignee"),
    
    CONSIGNOR("Consignor"),
    
    CARRIER("Carrier");

    private String value;

    ActorLogisticsRoleEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static ActorLogisticsRoleEnum fromValue(String value) {
      for (ActorLogisticsRoleEnum b : ActorLogisticsRoleEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @Valid
  private List<ActorLogisticsRoleEnum> actorLogisticsRole = new ArrayList<>();

  /**
   * Default constructor
   * @deprecated Use {@link Actor#Actor(List<@Valid ActorActorLegalPersonInner>, List<ActorLogisticsRoleEnum>)}
   */
  @Deprecated
  public Actor() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Actor(List<@Valid ActorActorLegalPersonInner> actorLegalPerson, List<ActorLogisticsRoleEnum> actorLogisticsRole) {
    this.actorLegalPerson = actorLegalPerson;
    this.actorLogisticsRole = actorLogisticsRole;
  }

  public Actor actorLegalPerson(List<@Valid ActorActorLegalPersonInner> actorLegalPerson) {
    this.actorLegalPerson = actorLegalPerson;
    return this;
  }

  public Actor addActorLegalPersonItem(ActorActorLegalPersonInner actorLegalPersonItem) {
    if (this.actorLegalPerson == null) {
      this.actorLegalPerson = new ArrayList<>();
    }
    this.actorLegalPerson.add(actorLegalPersonItem);
    return this;
  }

  /**
   * Get actorLegalPerson
   * @return actorLegalPerson
  */
  @NotNull @Valid @Size(min = 1) 
  @Schema(name = "actorLegalPerson", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("actorLegalPerson")
  public List<@Valid ActorActorLegalPersonInner> getActorLegalPerson() {
    return actorLegalPerson;
  }

  public void setActorLegalPerson(List<@Valid ActorActorLegalPersonInner> actorLegalPerson) {
    this.actorLegalPerson = actorLegalPerson;
  }

  public Actor actorLogisticsRole(List<ActorLogisticsRoleEnum> actorLogisticsRole) {
    this.actorLogisticsRole = actorLogisticsRole;
    return this;
  }

  public Actor addActorLogisticsRoleItem(ActorLogisticsRoleEnum actorLogisticsRoleItem) {
    if (this.actorLogisticsRole == null) {
      this.actorLogisticsRole = new ArrayList<>();
    }
    this.actorLogisticsRole.add(actorLogisticsRoleItem);
    return this;
  }

  /**
   * Get actorLogisticsRole
   * @return actorLogisticsRole
  */
  @NotNull @Size(min = 1) 
  @Schema(name = "actorLogisticsRole", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("actorLogisticsRole")
  public List<ActorLogisticsRoleEnum> getActorLogisticsRole() {
    return actorLogisticsRole;
  }

  public void setActorLogisticsRole(List<ActorLogisticsRoleEnum> actorLogisticsRole) {
    this.actorLogisticsRole = actorLogisticsRole;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Actor actor = (Actor) o;
    return Objects.equals(this.actorLegalPerson, actor.actorLegalPerson) &&
        Objects.equals(this.actorLogisticsRole, actor.actorLogisticsRole);
  }

  @Override
  public int hashCode() {
    return Objects.hash(actorLegalPerson, actorLogisticsRole);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Actor {\n");
    sb.append("    actorLegalPerson: ").append(toIndentedString(actorLegalPerson)).append("\n");
    sb.append("    actorLogisticsRole: ").append(toIndentedString(actorLogisticsRole)).append("\n");
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

