package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import nl.tno.federated.api.model.TransportEquipmentAllOfSize;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * TransportEquipmentAllOf
 */

@JsonTypeName("TransportEquipment_allOf")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-14T16:15:39.185+02:00[Europe/Amsterdam]")
public class TransportEquipmentAllOf {

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

  public TransportEquipmentAllOf grossMass(Double grossMass) {
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

  public TransportEquipmentAllOf grossVolume(Double grossVolume) {
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

  public TransportEquipmentAllOf transportEquipmentSizeType(TransportEquipmentSizeTypeEnum transportEquipmentSizeType) {
    this.transportEquipmentSizeType = transportEquipmentSizeType;
    return this;
  }

  /**
   * Get transportEquipmentSizeType
   * @return transportEquipmentSizeType
  */
  
  @Schema(name = "transportEquipmentSizeType", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transportEquipmentSizeType")
  public TransportEquipmentSizeTypeEnum getTransportEquipmentSizeType() {
    return transportEquipmentSizeType;
  }

  public void setTransportEquipmentSizeType(TransportEquipmentSizeTypeEnum transportEquipmentSizeType) {
    this.transportEquipmentSizeType = transportEquipmentSizeType;
  }

  public TransportEquipmentAllOf size(TransportEquipmentAllOfSize size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
  */
  @Valid 
  @Schema(name = "size", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    TransportEquipmentAllOf transportEquipmentAllOf = (TransportEquipmentAllOf) o;
    return Objects.equals(this.grossMass, transportEquipmentAllOf.grossMass) &&
        Objects.equals(this.grossVolume, transportEquipmentAllOf.grossVolume) &&
        Objects.equals(this.transportEquipmentSizeType, transportEquipmentAllOf.transportEquipmentSizeType) &&
        Objects.equals(this.size, transportEquipmentAllOf.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grossMass, grossVolume, transportEquipmentSizeType, size);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransportEquipmentAllOf {\n");
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

