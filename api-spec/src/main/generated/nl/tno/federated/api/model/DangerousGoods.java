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
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-07-07T10:20:12.669+02:00[Europe/Amsterdam]")
public class DangerousGoods implements LoadEventInvolvedDigitalTwinsInner {

  private String digitalTwinType;

  private String digitalTwinID;

  private String grossMass;

  private String grossVolume;

  private String unDGCode;

  private String dangerousGoodsRegulationCode;

  private String dangerousGoodsTechnicalName;

  private String dangerousGoodsEMSID;

  private String dangerousGoodsPackagingDangerLevelCode;

  private String dangerousGoodsHazardClassificationID;

  private String dangerousGoodsProperShippingName;

  private String dangerousGoodsSupplementaryInformation;

  private String dangerousGoodsFlashpointTemperature;

  /**
   * Default constructor
   * @deprecated Use {@link DangerousGoods#DangerousGoods(String, String, String, String, String, String, String, String, String, String, String, String)}
   */
  @Deprecated
  public DangerousGoods() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DangerousGoods(String digitalTwinType, String grossMass, String grossVolume, String unDGCode, String dangerousGoodsRegulationCode, String dangerousGoodsTechnicalName, String dangerousGoodsEMSID, String dangerousGoodsPackagingDangerLevelCode, String dangerousGoodsHazardClassificationID, String dangerousGoodsProperShippingName, String dangerousGoodsSupplementaryInformation, String dangerousGoodsFlashpointTemperature) {
    this.digitalTwinType = digitalTwinType;
    this.grossMass = grossMass;
    this.grossVolume = grossVolume;
    this.unDGCode = unDGCode;
    this.dangerousGoodsRegulationCode = dangerousGoodsRegulationCode;
    this.dangerousGoodsTechnicalName = dangerousGoodsTechnicalName;
    this.dangerousGoodsEMSID = dangerousGoodsEMSID;
    this.dangerousGoodsPackagingDangerLevelCode = dangerousGoodsPackagingDangerLevelCode;
    this.dangerousGoodsHazardClassificationID = dangerousGoodsHazardClassificationID;
    this.dangerousGoodsProperShippingName = dangerousGoodsProperShippingName;
    this.dangerousGoodsSupplementaryInformation = dangerousGoodsSupplementaryInformation;
    this.dangerousGoodsFlashpointTemperature = dangerousGoodsFlashpointTemperature;
  }

  public DangerousGoods digitalTwinType(String digitalTwinType) {
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

  public DangerousGoods digitalTwinID(String digitalTwinID) {
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

  public DangerousGoods grossMass(String grossMass) {
    this.grossMass = grossMass;
    return this;
  }

  /**
   * Get grossMass
   * @return grossMass
  */
  @NotNull 
  @Schema(name = "grossMass", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grossMass")
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
  @Schema(name = "grossVolume", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("grossVolume")
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
  @Schema(name = "UNDGCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("UNDGCode")
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
  @Schema(name = "dangerousGoodsRegulationCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsRegulationCode")
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
  @Schema(name = "dangerousGoodsTechnicalName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsTechnicalName")
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
  @Schema(name = "dangerousGoodsEMSID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsEMSID")
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
  @Schema(name = "dangerousGoodsPackagingDangerLevelCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsPackagingDangerLevelCode")
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
  @Schema(name = "dangerousGoodsHazardClassificationID", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsHazardClassificationID")
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
  @Schema(name = "dangerousGoodsProperShippingName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsProperShippingName")
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
  @Schema(name = "dangerousGoodsSupplementaryInformation", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsSupplementaryInformation")
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
  @Schema(name = "dangerousGoodsFlashpointTemperature", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dangerousGoodsFlashpointTemperature")
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

