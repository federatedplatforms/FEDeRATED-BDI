package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * May represent a transport means, a transport equipment or cargo (goods / dangerous goods)
 */

@Schema(name = "DigitalTwin", description = "May represent a transport means, a transport equipment or cargo (goods / dangerous goods)")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-13T16:38:33.391+02:00[Europe/Amsterdam]")
public class DigitalTwin {

  private String digitalTwinType;

  private String digitalTwinID;

  /**
   * Default constructor
   * @deprecated Use {@link DigitalTwin#DigitalTwin(String)}
   */
  @Deprecated
  public DigitalTwin() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DigitalTwin(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
  }

  public DigitalTwin digitalTwinType(String digitalTwinType) {
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

  public DigitalTwin digitalTwinID(String digitalTwinID) {
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
    DigitalTwin digitalTwin = (DigitalTwin) o;
    return Objects.equals(this.digitalTwinType, digitalTwin.digitalTwinType) &&
        Objects.equals(this.digitalTwinID, digitalTwin.digitalTwinID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinType, digitalTwinID);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DigitalTwin {\n");
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

