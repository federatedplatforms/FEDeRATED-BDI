package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Equipment to be used for transport of goods
 */

@Schema(name = "TransportEquipment", description = "Equipment to be used for transport of goods")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class TransportEquipment implements LoadEventInvolvedDigitalTwinsInner {

  @JsonProperty("digitalTwinType")
  private String digitalTwinType;

  @JsonProperty("digitalTwinID")
  private String digitalTwinID;

  @JsonProperty("grossMass")
  private String grossMass;

  @JsonProperty("grossVolume")
  private String grossVolume;

  /**
   * Gets or Sets transportEquipmentSizeType
   */
  public enum TransportEquipmentSizeTypeEnum {
    AA("AA"),
    
    AB("AB"),
    
    AC("AC"),
    
    AD("AD"),
    
    AE("AE"),
    
    AF("AF"),
    
    AG("AG"),
    
    AH("AH"),
    
    AI("AI"),
    
    AJ("AJ"),
    
    AK("AK"),
    
    AL("AL"),
    
    AM("AM"),
    
    AN("AN"),
    
    AO("AO"),
    
    AP("AP"),
    
    AQ("AQ"),
    
    AR("AR"),
    
    AS("AS"),
    
    AT("AT"),
    
    AU("AU"),
    
    AV("AV"),
    
    AX("AX"),
    
    AY("AY"),
    
    AZ("AZ"),
    
    BA("BA"),
    
    BB("BB"),
    
    BC("BC"),
    
    BD("BD"),
    
    BE("BE"),
    
    BF("BF"),
    
    BG("BG"),
    
    BH("BH"),
    
    BI("BI"),
    
    BJ("BJ"),
    
    BK("BK"),
    
    BL("BL"),
    
    BM("BM"),
    
    BN("BN"),
    
    BO("BO"),
    
    BP("BP"),
    
    BR("BR");

    private String value;

    TransportEquipmentSizeTypeEnum(String value) {
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
    public static TransportEquipmentSizeTypeEnum fromValue(String value) {
      for (TransportEquipmentSizeTypeEnum b : TransportEquipmentSizeTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  @JsonProperty("transportEquipmentSizeType")
  private TransportEquipmentSizeTypeEnum transportEquipmentSizeType;

  @JsonProperty("size")
  private String size;

  public TransportEquipment digitalTwinType(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
    return this;
  }

  /**
   * Get digitalTwinType
   * @return digitalTwinType
  */
  @NotNull 
  @Schema(name = "digitalTwinType", required = true)
  public String getDigitalTwinType() {
    return digitalTwinType;
  }

  public void setDigitalTwinType(String digitalTwinType) {
    this.digitalTwinType = digitalTwinType;
  }

  public TransportEquipment digitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
    return this;
  }

  /**
   * Get digitalTwinID
   * @return digitalTwinID
  */
  
  @Schema(name = "digitalTwinID", required = false)
  public String getDigitalTwinID() {
    return digitalTwinID;
  }

  public void setDigitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
  }

  public TransportEquipment grossMass(String grossMass) {
    this.grossMass = grossMass;
    return this;
  }

  /**
   * Get grossMass
   * @return grossMass
  */
  @NotNull 
  @Schema(name = "grossMass", required = true)
  public String getGrossMass() {
    return grossMass;
  }

  public void setGrossMass(String grossMass) {
    this.grossMass = grossMass;
  }

  public TransportEquipment grossVolume(String grossVolume) {
    this.grossVolume = grossVolume;
    return this;
  }

  /**
   * Get grossVolume
   * @return grossVolume
  */
  @NotNull 
  @Schema(name = "grossVolume", required = true)
  public String getGrossVolume() {
    return grossVolume;
  }

  public void setGrossVolume(String grossVolume) {
    this.grossVolume = grossVolume;
  }

  public TransportEquipment transportEquipmentSizeType(TransportEquipmentSizeTypeEnum transportEquipmentSizeType) {
    this.transportEquipmentSizeType = transportEquipmentSizeType;
    return this;
  }

  /**
   * Get transportEquipmentSizeType
   * @return transportEquipmentSizeType
  */
  @NotNull 
  @Schema(name = "transportEquipmentSizeType", required = true)
  public TransportEquipmentSizeTypeEnum getTransportEquipmentSizeType() {
    return transportEquipmentSizeType;
  }

  public void setTransportEquipmentSizeType(TransportEquipmentSizeTypeEnum transportEquipmentSizeType) {
    this.transportEquipmentSizeType = transportEquipmentSizeType;
  }

  public TransportEquipment size(String size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
  */
  @NotNull 
  @Schema(name = "size", required = true)
  public String getSize() {
    return size;
  }

  public void setSize(String size) {
    this.size = size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransportEquipment transportEquipment = (TransportEquipment) o;
    return Objects.equals(this.digitalTwinType, transportEquipment.digitalTwinType) &&
        Objects.equals(this.digitalTwinID, transportEquipment.digitalTwinID) &&
        Objects.equals(this.grossMass, transportEquipment.grossMass) &&
        Objects.equals(this.grossVolume, transportEquipment.grossVolume) &&
        Objects.equals(this.transportEquipmentSizeType, transportEquipment.transportEquipmentSizeType) &&
        Objects.equals(this.size, transportEquipment.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinType, digitalTwinID, grossMass, grossVolume, transportEquipmentSizeType, size);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransportEquipment {\n");
    sb.append("    digitalTwinType: ").append(toIndentedString(digitalTwinType)).append("\n");
    sb.append("    digitalTwinID: ").append(toIndentedString(digitalTwinID)).append("\n");
    sb.append("    grossMass: ").append(toIndentedString(grossMass)).append("\n");
    sb.append("    grossVolume: ").append(toIndentedString(grossVolume)).append("\n");
    sb.append("    transportEquipmentSizeType: ").append(toIndentedString(transportEquipmentSizeType)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
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

