package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * May represent a transport equipment, goods or dangerous goods
 */

@Schema(name = "Cargo", description = "May represent a transport equipment, goods or dangerous goods")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-14T16:15:39.185+02:00[Europe/Amsterdam]")
public class Cargo {

  private String digitalTwinType;

  private String digitalTwinID;

  /**
   * Default constructor
   * @deprecated Use {@link Cargo#Cargo(String)}
   */
  @Deprecated
  public Cargo() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Cargo(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
  }

  public Cargo digitalTwinType(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
    return this;
  }

  /**
   * Get digitalTwinType
   * @return digitalTwinType
  */
  @NotNull 
  @Schema(name = "digitalTwinType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("digitalTwinType")
  public String getDigitalTwinType() {
    return digitalTwinType;
  }

  public void setDigitalTwinType(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
  }

  public Cargo digitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
    return this;
  }

  /**
   * Get digitalTwinID
   * @return digitalTwinID
  */
  
  @Schema(name = "digitalTwinID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("digitalTwinID")
  public String getDigitalTwinID() {
    return digitalTwinID;
  }

  public void setDigitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Cargo cargo = (Cargo) o;
    return Objects.equals(this.digitalTwinType, cargo.digitalTwinType) &&
        Objects.equals(this.digitalTwinID, cargo.digitalTwinID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinType, digitalTwinID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Cargo {\n");
    sb.append("    digitalTwinType: ").append(toIndentedString(digitalTwinType)).append("\n");
    sb.append("    digitalTwinID: ").append(toIndentedString(digitalTwinID)).append("\n");
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

