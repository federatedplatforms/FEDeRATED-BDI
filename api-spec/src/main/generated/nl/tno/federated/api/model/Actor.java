package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import nl.tno.federated.api.model.ActorActorLegalPerson;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Actor has a role and is described by a legal person - relevant for us: consignor, consignee, carrier
 */

@Schema(name = "Actor", description = "Actor has a role and is described by a legal person - relevant for us: consignor, consignee, carrier")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:21:14.673+02:00[Europe/Amsterdam]")
public class Actor {

  private ActorActorLegalPerson actorLegalPerson;

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

  private ActorLogisticsRoleEnum actorLogisticsRole;

  /**
   * Default constructor
   * @deprecated Use {@link Actor#Actor(ActorActorLegalPerson, ActorLogisticsRoleEnum)}
   */
  @Deprecated
  public Actor() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Actor(ActorActorLegalPerson actorLegalPerson, ActorLogisticsRoleEnum actorLogisticsRole) {
    this.actorLegalPerson = actorLegalPerson;
    this.actorLogisticsRole = actorLogisticsRole;
  }

  public Actor actorLegalPerson(ActorActorLegalPerson actorLegalPerson) {
    this.actorLegalPerson = actorLegalPerson;
    return this;
  }

  /**
   * Get actorLegalPerson
   * @return actorLegalPerson
  */
  @NotNull @Valid 
  @Schema(name = "actorLegalPerson", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("actorLegalPerson")
  public ActorActorLegalPerson getActorLegalPerson() {
    return actorLegalPerson;
  }

  public void setActorLegalPerson(ActorActorLegalPerson actorLegalPerson) {
    this.actorLegalPerson = actorLegalPerson;
  }

  public Actor actorLogisticsRole(ActorLogisticsRoleEnum actorLogisticsRole) {
    this.actorLogisticsRole = actorLogisticsRole;
    return this;
  }

  /**
   * Get actorLogisticsRole
   * @return actorLogisticsRole
  */
  @NotNull 
  @Schema(name = "actorLogisticsRole", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("actorLogisticsRole")
  public ActorLogisticsRoleEnum getActorLogisticsRole() {
    return actorLogisticsRole;
  }

  public void setActorLogisticsRole(ActorLogisticsRoleEnum actorLogisticsRole) {
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

