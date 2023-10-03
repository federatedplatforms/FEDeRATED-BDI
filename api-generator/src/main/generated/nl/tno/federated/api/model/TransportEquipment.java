package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import nl.tno.federated.api.model.TransportEquipmentAllOfSize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Equipment to be used for transport of goods
 */

@Schema(name = "TransportEquipment", description = "Equipment to be used for transport of goods")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:42:29.708+02:00[Europe/Amsterdam]")
public class TransportEquipment implements LoadEventInvolvedCargoInner {

  private String digitalTwinType;

  private String digitalTwinID;

  private Double grossMass;

  private Double grossVolume;

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

  private TransportEquipmentSizeTypeEnum transportEquipmentSizeType;

  private TransportEquipmentAllOfSize size;

  /**
   * Default constructor
   * @deprecated Use {@link TransportEquipment#TransportEquipment(String, String, TransportEquipmentSizeTypeEnum, TransportEquipmentAllOfSize)}
   */
  @Deprecated
  public TransportEquipment() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransportEquipment(String digitalTwinType, String digitalTwinID, TransportEquipmentSizeTypeEnum transportEquipmentSizeType, TransportEquipmentAllOfSize size) {
    this.digitalTwinType = digitalTwinType;
    this.digitalTwinID = digitalTwinID;
    this.transportEquipmentSizeType = transportEquipmentSizeType;
    this.size = size;
  }

  public TransportEquipment digitalTwinType(String digitalTwinType) {
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

  public TransportEquipment digitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
    return this;
  }

  /**
   * Get digitalTwinID
   * @return digitalTwinID
  */
  @NotNull 
  @Schema(name = "digitalTwinID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("digitalTwinID")
  public String getDigitalTwinID() {
    return digitalTwinID;
  }

  public void setDigitalTwinID(String digitalTwinID) {
    this.digitalTwinID = digitalTwinID;
  }

  public TransportEquipment grossMass(Double grossMass) {
    this.grossMass = grossMass;
    return this;
  }

  /**
   * Get grossMass
   * @return grossMass
  */
  
  @Schema(name = "grossMass", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grossMass")
  public Double getGrossMass() {
    return grossMass;
  }

  public void setGrossMass(Double grossMass) {
    this.grossMass = grossMass;
  }

  public TransportEquipment grossVolume(Double grossVolume) {
    this.grossVolume = grossVolume;
    return this;
  }

  /**
   * Get grossVolume
   * @return grossVolume
  */
  
  @Schema(name = "grossVolume", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grossVolume")
  public Double getGrossVolume() {
    return grossVolume;
  }

  public void setGrossVolume(Double grossVolume) {
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
  @Schema(name = "transportEquipmentSizeType", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transportEquipmentSizeType")
  public TransportEquipmentSizeTypeEnum getTransportEquipmentSizeType() {
    return transportEquipmentSizeType;
  }

  public void setTransportEquipmentSizeType(TransportEquipmentSizeTypeEnum transportEquipmentSizeType) {
    this.transportEquipmentSizeType = transportEquipmentSizeType;
  }

  public TransportEquipment size(TransportEquipmentAllOfSize size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
  */
  @NotNull @Valid 
  @Schema(name = "size", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("size")
  public TransportEquipmentAllOfSize getSize() {
    return size;
  }

  public void setSize(TransportEquipmentAllOfSize size) {
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

