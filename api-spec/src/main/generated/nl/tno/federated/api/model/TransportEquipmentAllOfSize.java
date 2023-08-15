package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * TransportEquipmentAllOfSize
 */

@JsonTypeName("TransportEquipment_allOf_size")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-14T16:15:39.185+02:00[Europe/Amsterdam]")
public class TransportEquipmentAllOfSize {

  private Double height;

  private Double length;

  private Double width;

  /**
   * Default constructor
   * @deprecated Use {@link TransportEquipmentAllOfSize#TransportEquipmentAllOfSize(Double, Double, Double)}
   */
  @Deprecated
  public TransportEquipmentAllOfSize() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransportEquipmentAllOfSize(Double height, Double length, Double width) {
    this.height = height;
    this.length = length;
    this.width = width;
  }

  public TransportEquipmentAllOfSize height(Double height) {
    this.height = height;
    return this;
  }

  /**
   * Get height
   * @return height
  */
  @NotNull 
  @Schema(name = "height", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("height")
  public Double getHeight() {
    return height;
  }

  public void setHeight(Double height) {
    this.height = height;
  }

  public TransportEquipmentAllOfSize length(Double length) {
    this.length = length;
    return this;
  }

  /**
   * Get length
   * @return length
  */
  @NotNull 
  @Schema(name = "length", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("length")
  public Double getLength() {
    return length;
  }

  public void setLength(Double length) {
    this.length = length;
  }

  public TransportEquipmentAllOfSize width(Double width) {
    this.width = width;
    return this;
  }

  /**
   * Get width
   * @return width
  */
  @NotNull 
  @Schema(name = "width", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("width")
  public Double getWidth() {
    return width;
  }

  public void setWidth(Double width) {
    this.width = width;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransportEquipmentAllOfSize transportEquipmentAllOfSize = (TransportEquipmentAllOfSize) o;
    return Objects.equals(this.height, transportEquipmentAllOfSize.height) &&
        Objects.equals(this.length, transportEquipmentAllOfSize.length) &&
        Objects.equals(this.width, transportEquipmentAllOfSize.width);
  }

  @Override
  public int hashCode() {
    return Objects.hash(height, length, width);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransportEquipmentAllOfSize {\n");
    sb.append("    height: ").append(toIndentedString(height)).append("\n");
    sb.append("    length: ").append(toIndentedString(length)).append("\n");
    sb.append("    width: ").append(toIndentedString(width)).append("\n");
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

