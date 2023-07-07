package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Specialized cargo, dangerous goods
 */

@Schema(name = "DangerousGoods", description = "Specialized cargo, dangerous goods")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T09:40:23.742+02:00[Europe/Amsterdam]")
public class DangerousGoods implements LoadEventInvolvedDigitalTwinsInner {

  @JsonProperty("digitalTwinType")
  private String digitalTwinType;

  @JsonProperty("digitalTwinID")
  private String digitalTwinID;

  @JsonProperty("grossMass")
  private String grossMass;

  @JsonProperty("grossVolume")
  private String grossVolume;

  @JsonProperty("UNDGCode")
  private String unDGCode;

  @JsonProperty("dangerousGoodsRegulationCode")
  private String dangerousGoodsRegulationCode;

  @JsonProperty("dangerousGoodsTechnicalName")
  private String dangerousGoodsTechnicalName;

  @JsonProperty("dangerousGoodsEMSID")
  private String dangerousGoodsEMSID;

  @JsonProperty("dangerousGoodsPackagingDangerLevelCode")
  private String dangerousGoodsPackagingDangerLevelCode;

  @JsonProperty("dangerousGoodsHazardClassificationID")
  private String dangerousGoodsHazardClassificationID;

  @JsonProperty("dangerousGoodsProperShippingName")
  private String dangerousGoodsProperShippingName;

  @JsonProperty("dangerousGoodsSupplementaryInformation")
  private String dangerousGoodsSupplementaryInformation;

  @JsonProperty("dangerousGoodsFlashpointTemperature")
  private String dangerousGoodsFlashpointTemperature;

  public DangerousGoods digitalTwinType(String digitalTwinType) {
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

  public DangerousGoods digitalTwinID(String digitalTwinID) {
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

  public DangerousGoods grossMass(String grossMass) {
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

  public DangerousGoods grossVolume(String grossVolume) {
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

  public DangerousGoods unDGCode(String unDGCode) {
    this.unDGCode = unDGCode;
    return this;
  }

  /**
   * Get unDGCode
   * @return unDGCode
  */
  @NotNull 
  @Schema(name = "UNDGCode", required = true)
  public String getUnDGCode() {
    return unDGCode;
  }

  public void setUnDGCode(String unDGCode) {
    this.unDGCode = unDGCode;
  }

  public DangerousGoods dangerousGoodsRegulationCode(String dangerousGoodsRegulationCode) {
    this.dangerousGoodsRegulationCode = dangerousGoodsRegulationCode;
    return this;
  }

  /**
   * Get dangerousGoodsRegulationCode
   * @return dangerousGoodsRegulationCode
  */
  @NotNull 
  @Schema(name = "dangerousGoodsRegulationCode", required = true)
  public String getDangerousGoodsRegulationCode() {
    return dangerousGoodsRegulationCode;
  }

  public void setDangerousGoodsRegulationCode(String dangerousGoodsRegulationCode) {
    this.dangerousGoodsRegulationCode = dangerousGoodsRegulationCode;
  }

  public DangerousGoods dangerousGoodsTechnicalName(String dangerousGoodsTechnicalName) {
    this.dangerousGoodsTechnicalName = dangerousGoodsTechnicalName;
    return this;
  }

  /**
   * Get dangerousGoodsTechnicalName
   * @return dangerousGoodsTechnicalName
  */
  @NotNull 
  @Schema(name = "dangerousGoodsTechnicalName", required = true)
  public String getDangerousGoodsTechnicalName() {
    return dangerousGoodsTechnicalName;
  }

  public void setDangerousGoodsTechnicalName(String dangerousGoodsTechnicalName) {
    this.dangerousGoodsTechnicalName = dangerousGoodsTechnicalName;
  }

  public DangerousGoods dangerousGoodsEMSID(String dangerousGoodsEMSID) {
    this.dangerousGoodsEMSID = dangerousGoodsEMSID;
    return this;
  }

  /**
   * Get dangerousGoodsEMSID
   * @return dangerousGoodsEMSID
  */
  @NotNull 
  @Schema(name = "dangerousGoodsEMSID", required = true)
  public String getDangerousGoodsEMSID() {
    return dangerousGoodsEMSID;
  }

  public void setDangerousGoodsEMSID(String dangerousGoodsEMSID) {
    this.dangerousGoodsEMSID = dangerousGoodsEMSID;
  }

  public DangerousGoods dangerousGoodsPackagingDangerLevelCode(String dangerousGoodsPackagingDangerLevelCode) {
    this.dangerousGoodsPackagingDangerLevelCode = dangerousGoodsPackagingDangerLevelCode;
    return this;
  }

  /**
   * Get dangerousGoodsPackagingDangerLevelCode
   * @return dangerousGoodsPackagingDangerLevelCode
  */
  @NotNull 
  @Schema(name = "dangerousGoodsPackagingDangerLevelCode", required = true)
  public String getDangerousGoodsPackagingDangerLevelCode() {
    return dangerousGoodsPackagingDangerLevelCode;
  }

  public void setDangerousGoodsPackagingDangerLevelCode(String dangerousGoodsPackagingDangerLevelCode) {
    this.dangerousGoodsPackagingDangerLevelCode = dangerousGoodsPackagingDangerLevelCode;
  }

  public DangerousGoods dangerousGoodsHazardClassificationID(String dangerousGoodsHazardClassificationID) {
    this.dangerousGoodsHazardClassificationID = dangerousGoodsHazardClassificationID;
    return this;
  }

  /**
   * Get dangerousGoodsHazardClassificationID
   * @return dangerousGoodsHazardClassificationID
  */
  @NotNull 
  @Schema(name = "dangerousGoodsHazardClassificationID", required = true)
  public String getDangerousGoodsHazardClassificationID() {
    return dangerousGoodsHazardClassificationID;
  }

  public void setDangerousGoodsHazardClassificationID(String dangerousGoodsHazardClassificationID) {
    this.dangerousGoodsHazardClassificationID = dangerousGoodsHazardClassificationID;
  }

  public DangerousGoods dangerousGoodsProperShippingName(String dangerousGoodsProperShippingName) {
    this.dangerousGoodsProperShippingName = dangerousGoodsProperShippingName;
    return this;
  }

  /**
   * Get dangerousGoodsProperShippingName
   * @return dangerousGoodsProperShippingName
  */
  @NotNull 
  @Schema(name = "dangerousGoodsProperShippingName", required = true)
  public String getDangerousGoodsProperShippingName() {
    return dangerousGoodsProperShippingName;
  }

  public void setDangerousGoodsProperShippingName(String dangerousGoodsProperShippingName) {
    this.dangerousGoodsProperShippingName = dangerousGoodsProperShippingName;
  }

  public DangerousGoods dangerousGoodsSupplementaryInformation(String dangerousGoodsSupplementaryInformation) {
    this.dangerousGoodsSupplementaryInformation = dangerousGoodsSupplementaryInformation;
    return this;
  }

  /**
   * Get dangerousGoodsSupplementaryInformation
   * @return dangerousGoodsSupplementaryInformation
  */
  @NotNull 
  @Schema(name = "dangerousGoodsSupplementaryInformation", required = true)
  public String getDangerousGoodsSupplementaryInformation() {
    return dangerousGoodsSupplementaryInformation;
  }

  public void setDangerousGoodsSupplementaryInformation(String dangerousGoodsSupplementaryInformation) {
    this.dangerousGoodsSupplementaryInformation = dangerousGoodsSupplementaryInformation;
  }

  public DangerousGoods dangerousGoodsFlashpointTemperature(String dangerousGoodsFlashpointTemperature) {
    this.dangerousGoodsFlashpointTemperature = dangerousGoodsFlashpointTemperature;
    return this;
  }

  /**
   * Get dangerousGoodsFlashpointTemperature
   * @return dangerousGoodsFlashpointTemperature
  */
  @NotNull 
  @Schema(name = "dangerousGoodsFlashpointTemperature", required = true)
  public String getDangerousGoodsFlashpointTemperature() {
    return dangerousGoodsFlashpointTemperature;
  }

  public void setDangerousGoodsFlashpointTemperature(String dangerousGoodsFlashpointTemperature) {
    this.dangerousGoodsFlashpointTemperature = dangerousGoodsFlashpointTemperature;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DangerousGoods dangerousGoods = (DangerousGoods) o;
    return Objects.equals(this.digitalTwinType, dangerousGoods.digitalTwinType) &&
        Objects.equals(this.digitalTwinID, dangerousGoods.digitalTwinID) &&
        Objects.equals(this.grossMass, dangerousGoods.grossMass) &&
        Objects.equals(this.grossVolume, dangerousGoods.grossVolume) &&
        Objects.equals(this.unDGCode, dangerousGoods.unDGCode) &&
        Objects.equals(this.dangerousGoodsRegulationCode, dangerousGoods.dangerousGoodsRegulationCode) &&
        Objects.equals(this.dangerousGoodsTechnicalName, dangerousGoods.dangerousGoodsTechnicalName) &&
        Objects.equals(this.dangerousGoodsEMSID, dangerousGoods.dangerousGoodsEMSID) &&
        Objects.equals(this.dangerousGoodsPackagingDangerLevelCode, dangerousGoods.dangerousGoodsPackagingDangerLevelCode) &&
        Objects.equals(this.dangerousGoodsHazardClassificationID, dangerousGoods.dangerousGoodsHazardClassificationID) &&
        Objects.equals(this.dangerousGoodsProperShippingName, dangerousGoods.dangerousGoodsProperShippingName) &&
        Objects.equals(this.dangerousGoodsSupplementaryInformation, dangerousGoods.dangerousGoodsSupplementaryInformation) &&
        Objects.equals(this.dangerousGoodsFlashpointTemperature, dangerousGoods.dangerousGoodsFlashpointTemperature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinType, digitalTwinID, grossMass, grossVolume, unDGCode, dangerousGoodsRegulationCode, dangerousGoodsTechnicalName, dangerousGoodsEMSID, dangerousGoodsPackagingDangerLevelCode, dangerousGoodsHazardClassificationID, dangerousGoodsProperShippingName, dangerousGoodsSupplementaryInformation, dangerousGoodsFlashpointTemperature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DangerousGoods {\n");
    sb.append("    digitalTwinType: ").append(toIndentedString(digitalTwinType)).append("\n");
    sb.append("    digitalTwinID: ").append(toIndentedString(digitalTwinID)).append("\n");
    sb.append("    grossMass: ").append(toIndentedString(grossMass)).append("\n");
    sb.append("    grossVolume: ").append(toIndentedString(grossVolume)).append("\n");
    sb.append("    unDGCode: ").append(toIndentedString(unDGCode)).append("\n");
    sb.append("    dangerousGoodsRegulationCode: ").append(toIndentedString(dangerousGoodsRegulationCode)).append("\n");
    sb.append("    dangerousGoodsTechnicalName: ").append(toIndentedString(dangerousGoodsTechnicalName)).append("\n");
    sb.append("    dangerousGoodsEMSID: ").append(toIndentedString(dangerousGoodsEMSID)).append("\n");
    sb.append("    dangerousGoodsPackagingDangerLevelCode: ").append(toIndentedString(dangerousGoodsPackagingDangerLevelCode)).append("\n");
    sb.append("    dangerousGoodsHazardClassificationID: ").append(toIndentedString(dangerousGoodsHazardClassificationID)).append("\n");
    sb.append("    dangerousGoodsProperShippingName: ").append(toIndentedString(dangerousGoodsProperShippingName)).append("\n");
    sb.append("    dangerousGoodsSupplementaryInformation: ").append(toIndentedString(dangerousGoodsSupplementaryInformation)).append("\n");
    sb.append("    dangerousGoodsFlashpointTemperature: ").append(toIndentedString(dangerousGoodsFlashpointTemperature)).append("\n");
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

