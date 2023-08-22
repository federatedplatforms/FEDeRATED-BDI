package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * DangerousGoodsAllOf
 */

@JsonTypeName("DangerousGoods_allOf")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:42:29.708+02:00[Europe/Amsterdam]")
public class DangerousGoodsAllOf {

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

  public DangerousGoodsAllOf grossMass(String grossMass) {
    this.grossMass = grossMass;
    return this;
  }

  /**
   * Get grossMass
   * @return grossMass
  */
  
  @Schema(name = "grossMass", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grossMass")
  public String getGrossMass() {
    return grossMass;
  }

  public void setGrossMass(String grossMass) {
    this.grossMass = grossMass;
  }

  public DangerousGoodsAllOf grossVolume(String grossVolume) {
    this.grossVolume = grossVolume;
    return this;
  }

  /**
   * Get grossVolume
   * @return grossVolume
  */
  
  @Schema(name = "grossVolume", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("grossVolume")
  public String getGrossVolume() {
    return grossVolume;
  }

  public void setGrossVolume(String grossVolume) {
    this.grossVolume = grossVolume;
  }

  public DangerousGoodsAllOf unDGCode(String unDGCode) {
    this.unDGCode = unDGCode;
    return this;
  }

  /**
   * Get unDGCode
   * @return unDGCode
  */
  
  @Schema(name = "UNDGCode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("UNDGCode")
  public String getUnDGCode() {
    return unDGCode;
  }

  public void setUnDGCode(String unDGCode) {
    this.unDGCode = unDGCode;
  }

  public DangerousGoodsAllOf dangerousGoodsRegulationCode(String dangerousGoodsRegulationCode) {
    this.dangerousGoodsRegulationCode = dangerousGoodsRegulationCode;
    return this;
  }

  /**
   * Get dangerousGoodsRegulationCode
   * @return dangerousGoodsRegulationCode
  */
  
  @Schema(name = "dangerousGoodsRegulationCode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsRegulationCode")
  public String getDangerousGoodsRegulationCode() {
    return dangerousGoodsRegulationCode;
  }

  public void setDangerousGoodsRegulationCode(String dangerousGoodsRegulationCode) {
    this.dangerousGoodsRegulationCode = dangerousGoodsRegulationCode;
  }

  public DangerousGoodsAllOf dangerousGoodsTechnicalName(String dangerousGoodsTechnicalName) {
    this.dangerousGoodsTechnicalName = dangerousGoodsTechnicalName;
    return this;
  }

  /**
   * Get dangerousGoodsTechnicalName
   * @return dangerousGoodsTechnicalName
  */
  
  @Schema(name = "dangerousGoodsTechnicalName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsTechnicalName")
  public String getDangerousGoodsTechnicalName() {
    return dangerousGoodsTechnicalName;
  }

  public void setDangerousGoodsTechnicalName(String dangerousGoodsTechnicalName) {
    this.dangerousGoodsTechnicalName = dangerousGoodsTechnicalName;
  }

  public DangerousGoodsAllOf dangerousGoodsEMSID(String dangerousGoodsEMSID) {
    this.dangerousGoodsEMSID = dangerousGoodsEMSID;
    return this;
  }

  /**
   * Get dangerousGoodsEMSID
   * @return dangerousGoodsEMSID
  */
  
  @Schema(name = "dangerousGoodsEMSID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsEMSID")
  public String getDangerousGoodsEMSID() {
    return dangerousGoodsEMSID;
  }

  public void setDangerousGoodsEMSID(String dangerousGoodsEMSID) {
    this.dangerousGoodsEMSID = dangerousGoodsEMSID;
  }

  public DangerousGoodsAllOf dangerousGoodsPackagingDangerLevelCode(String dangerousGoodsPackagingDangerLevelCode) {
    this.dangerousGoodsPackagingDangerLevelCode = dangerousGoodsPackagingDangerLevelCode;
    return this;
  }

  /**
   * Get dangerousGoodsPackagingDangerLevelCode
   * @return dangerousGoodsPackagingDangerLevelCode
  */
  
  @Schema(name = "dangerousGoodsPackagingDangerLevelCode", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsPackagingDangerLevelCode")
  public String getDangerousGoodsPackagingDangerLevelCode() {
    return dangerousGoodsPackagingDangerLevelCode;
  }

  public void setDangerousGoodsPackagingDangerLevelCode(String dangerousGoodsPackagingDangerLevelCode) {
    this.dangerousGoodsPackagingDangerLevelCode = dangerousGoodsPackagingDangerLevelCode;
  }

  public DangerousGoodsAllOf dangerousGoodsHazardClassificationID(String dangerousGoodsHazardClassificationID) {
    this.dangerousGoodsHazardClassificationID = dangerousGoodsHazardClassificationID;
    return this;
  }

  /**
   * Get dangerousGoodsHazardClassificationID
   * @return dangerousGoodsHazardClassificationID
  */
  
  @Schema(name = "dangerousGoodsHazardClassificationID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsHazardClassificationID")
  public String getDangerousGoodsHazardClassificationID() {
    return dangerousGoodsHazardClassificationID;
  }

  public void setDangerousGoodsHazardClassificationID(String dangerousGoodsHazardClassificationID) {
    this.dangerousGoodsHazardClassificationID = dangerousGoodsHazardClassificationID;
  }

  public DangerousGoodsAllOf dangerousGoodsProperShippingName(String dangerousGoodsProperShippingName) {
    this.dangerousGoodsProperShippingName = dangerousGoodsProperShippingName;
    return this;
  }

  /**
   * Get dangerousGoodsProperShippingName
   * @return dangerousGoodsProperShippingName
  */
  
  @Schema(name = "dangerousGoodsProperShippingName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsProperShippingName")
  public String getDangerousGoodsProperShippingName() {
    return dangerousGoodsProperShippingName;
  }

  public void setDangerousGoodsProperShippingName(String dangerousGoodsProperShippingName) {
    this.dangerousGoodsProperShippingName = dangerousGoodsProperShippingName;
  }

  public DangerousGoodsAllOf dangerousGoodsSupplementaryInformation(String dangerousGoodsSupplementaryInformation) {
    this.dangerousGoodsSupplementaryInformation = dangerousGoodsSupplementaryInformation;
    return this;
  }

  /**
   * Get dangerousGoodsSupplementaryInformation
   * @return dangerousGoodsSupplementaryInformation
  */
  
  @Schema(name = "dangerousGoodsSupplementaryInformation", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("dangerousGoodsSupplementaryInformation")
  public String getDangerousGoodsSupplementaryInformation() {
    return dangerousGoodsSupplementaryInformation;
  }

  public void setDangerousGoodsSupplementaryInformation(String dangerousGoodsSupplementaryInformation) {
    this.dangerousGoodsSupplementaryInformation = dangerousGoodsSupplementaryInformation;
  }

  public DangerousGoodsAllOf dangerousGoodsFlashpointTemperature(String dangerousGoodsFlashpointTemperature) {
    this.dangerousGoodsFlashpointTemperature = dangerousGoodsFlashpointTemperature;
    return this;
  }

  /**
   * Get dangerousGoodsFlashpointTemperature
   * @return dangerousGoodsFlashpointTemperature
  */
  
  @Schema(name = "dangerousGoodsFlashpointTemperature", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
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
    DangerousGoodsAllOf dangerousGoodsAllOf = (DangerousGoodsAllOf) o;
    return Objects.equals(this.grossMass, dangerousGoodsAllOf.grossMass) &&
        Objects.equals(this.grossVolume, dangerousGoodsAllOf.grossVolume) &&
        Objects.equals(this.unDGCode, dangerousGoodsAllOf.unDGCode) &&
        Objects.equals(this.dangerousGoodsRegulationCode, dangerousGoodsAllOf.dangerousGoodsRegulationCode) &&
        Objects.equals(this.dangerousGoodsTechnicalName, dangerousGoodsAllOf.dangerousGoodsTechnicalName) &&
        Objects.equals(this.dangerousGoodsEMSID, dangerousGoodsAllOf.dangerousGoodsEMSID) &&
        Objects.equals(this.dangerousGoodsPackagingDangerLevelCode, dangerousGoodsAllOf.dangerousGoodsPackagingDangerLevelCode) &&
        Objects.equals(this.dangerousGoodsHazardClassificationID, dangerousGoodsAllOf.dangerousGoodsHazardClassificationID) &&
        Objects.equals(this.dangerousGoodsProperShippingName, dangerousGoodsAllOf.dangerousGoodsProperShippingName) &&
        Objects.equals(this.dangerousGoodsSupplementaryInformation, dangerousGoodsAllOf.dangerousGoodsSupplementaryInformation) &&
        Objects.equals(this.dangerousGoodsFlashpointTemperature, dangerousGoodsAllOf.dangerousGoodsFlashpointTemperature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(grossMass, grossVolume, unDGCode, dangerousGoodsRegulationCode, dangerousGoodsTechnicalName, dangerousGoodsEMSID, dangerousGoodsPackagingDangerLevelCode, dangerousGoodsHazardClassificationID, dangerousGoodsProperShippingName, dangerousGoodsSupplementaryInformation, dangerousGoodsFlashpointTemperature);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DangerousGoodsAllOf {\n");
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

