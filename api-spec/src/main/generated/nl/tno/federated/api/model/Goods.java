package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Specialized cargo, goods
 */

@Schema(name = "Goods", description = "Specialized cargo, goods")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class Goods implements LoadEventInvolvedDigitalTwinsInner {

  @JsonProperty("digitalTwinType")
  private String digitalTwinType;

  @JsonProperty("digitalTwinID")
  private String digitalTwinID;

  @JsonProperty("grossMass")
  private String grossMass;

  @JsonProperty("grossVolume")
  private String grossVolume;

  @JsonProperty("goodsTypeCode")
  private String goodsTypeCode;

  @JsonProperty("netMass")
  private Integer netMass;

  @JsonProperty("numberOfUnits")
  private Integer numberOfUnits;

  @JsonProperty("goodsDescription")
  private String goodsDescription;

  public Goods digitalTwinType(String digitalTwinType) {
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

  public Goods digitalTwinID(String digitalTwinID) {
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

  public Goods grossMass(String grossMass) {
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

  public Goods grossVolume(String grossVolume) {
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

  public Goods goodsTypeCode(String goodsTypeCode) {
    this.goodsTypeCode = goodsTypeCode;
    return this;
  }

  /**
   * Get goodsTypeCode
   * @return goodsTypeCode
  */
  @NotNull 
  @Schema(name = "goodsTypeCode", required = true)
  public String getGoodsTypeCode() {
    return goodsTypeCode;
  }

  public void setGoodsTypeCode(String goodsTypeCode) {
    this.goodsTypeCode = goodsTypeCode;
  }

  public Goods netMass(Integer netMass) {
    this.netMass = netMass;
    return this;
  }

  /**
   * Get netMass
   * @return netMass
  */
  @NotNull 
  @Schema(name = "netMass", required = true)
  public Integer getNetMass() {
    return netMass;
  }

  public void setNetMass(Integer netMass) {
    this.netMass = netMass;
  }

  public Goods numberOfUnits(Integer numberOfUnits) {
    this.numberOfUnits = numberOfUnits;
    return this;
  }

  /**
   * Get numberOfUnits
   * @return numberOfUnits
  */
  @NotNull 
  @Schema(name = "numberOfUnits", required = true)
  public Integer getNumberOfUnits() {
    return numberOfUnits;
  }

  public void setNumberOfUnits(Integer numberOfUnits) {
    this.numberOfUnits = numberOfUnits;
  }

  public Goods goodsDescription(String goodsDescription) {
    this.goodsDescription = goodsDescription;
    return this;
  }

  /**
   * Get goodsDescription
   * @return goodsDescription
  */
  @NotNull 
  @Schema(name = "goodsDescription", required = true)
  public String getGoodsDescription() {
    return goodsDescription;
  }

  public void setGoodsDescription(String goodsDescription) {
    this.goodsDescription = goodsDescription;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Goods goods = (Goods) o;
    return Objects.equals(this.digitalTwinType, goods.digitalTwinType) &&
        Objects.equals(this.digitalTwinID, goods.digitalTwinID) &&
        Objects.equals(this.grossMass, goods.grossMass) &&
        Objects.equals(this.grossVolume, goods.grossVolume) &&
        Objects.equals(this.goodsTypeCode, goods.goodsTypeCode) &&
        Objects.equals(this.netMass, goods.netMass) &&
        Objects.equals(this.numberOfUnits, goods.numberOfUnits) &&
        Objects.equals(this.goodsDescription, goods.goodsDescription);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinType, digitalTwinID, grossMass, grossVolume, goodsTypeCode, netMass, numberOfUnits, goodsDescription);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Goods {\n");
    sb.append("    digitalTwinType: ").append(toIndentedString(digitalTwinType)).append("\n");
    sb.append("    digitalTwinID: ").append(toIndentedString(digitalTwinID)).append("\n");
    sb.append("    grossMass: ").append(toIndentedString(grossMass)).append("\n");
    sb.append("    grossVolume: ").append(toIndentedString(grossVolume)).append("\n");
    sb.append("    goodsTypeCode: ").append(toIndentedString(goodsTypeCode)).append("\n");
    sb.append("    netMass: ").append(toIndentedString(netMass)).append("\n");
    sb.append("    numberOfUnits: ").append(toIndentedString(numberOfUnits)).append("\n");
    sb.append("    goodsDescription: ").append(toIndentedString(goodsDescription)).append("\n");
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

