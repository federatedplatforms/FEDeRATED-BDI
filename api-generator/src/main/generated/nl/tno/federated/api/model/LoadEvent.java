package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import nl.tno.federated.api.model.Actor;
import nl.tno.federated.api.model.LoadEventInvolvedCargoInner;
import nl.tno.federated.api.model.TransportMeans;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Generated by TNO FEDeRATED
 */

@Schema(name = "LoadEvent", description = "Generated by TNO FEDeRATED")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:42:29.708+02:00[Europe/Amsterdam]")
public class LoadEvent {

  private String UUID;

  @Valid
  private List<@Valid Actor> involvedActors = new ArrayList<>();

  @Valid
  private List<@Valid TransportMeans> transportMeans = new ArrayList<>();

  @Valid
  private List<@Valid LoadEventInvolvedCargoInner> involvedCargo;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime timestamp;

  /**
   * Gets or Sets timeClassification
   */
  public enum TimeClassificationEnum {
    ACTUAL("Actual"),
    
    ESTIMATED("Estimated"),
    
    EXPECTED("Expected"),
    
    PLANNED("Planned"),
    
    REQUESTED("Requested");

    private String value;

    TimeClassificationEnum(String value) {
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
    public static TimeClassificationEnum fromValue(String value) {
      for (TimeClassificationEnum b : TimeClassificationEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private TimeClassificationEnum timeClassification;

  /**
   * Default constructor
   * @deprecated Use {@link LoadEvent#LoadEvent(List<@Valid Actor>, List<@Valid TransportMeans>, OffsetDateTime, TimeClassificationEnum)}
   */
  @Deprecated
  public LoadEvent() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public LoadEvent(List<@Valid Actor> involvedActors, List<@Valid TransportMeans> transportMeans, OffsetDateTime timestamp, TimeClassificationEnum timeClassification) {
    this.involvedActors = involvedActors;
    this.transportMeans = transportMeans;
    this.timestamp = timestamp;
    this.timeClassification = timeClassification;
  }

  public LoadEvent UUID(String UUID) {
    this.UUID = UUID;
    return this;
  }

  /**
   * Get UUID
   * @return UUID
  */
  
  @Schema(name = "UUID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("UUID")
  public String getUUID() {
    return UUID;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  public LoadEvent involvedActors(List<@Valid Actor> involvedActors) {
    this.involvedActors = involvedActors;
    return this;
  }

  public LoadEvent addInvolvedActorsItem(Actor involvedActorsItem) {
    if (this.involvedActors == null) {
      this.involvedActors = new ArrayList<>();
    }
    this.involvedActors.add(involvedActorsItem);
    return this;
  }

  /**
   * Get involvedActors
   * @return involvedActors
  */
  @NotNull @Valid 
  @Schema(name = "involvedActors", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("involvedActors")
  public List<@Valid Actor> getInvolvedActors() {
    return involvedActors;
  }

  public void setInvolvedActors(List<@Valid Actor> involvedActors) {
    this.involvedActors = involvedActors;
  }

  public LoadEvent transportMeans(List<@Valid TransportMeans> transportMeans) {
    this.transportMeans = transportMeans;
    return this;
  }

  public LoadEvent addTransportMeansItem(TransportMeans transportMeansItem) {
    if (this.transportMeans == null) {
      this.transportMeans = new ArrayList<>();
    }
    this.transportMeans.add(transportMeansItem);
    return this;
  }

  /**
   * Get transportMeans
   * @return transportMeans
  */
  @NotNull @Valid 
  @Schema(name = "transportMeans", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transportMeans")
  public List<@Valid TransportMeans> getTransportMeans() {
    return transportMeans;
  }

  public void setTransportMeans(List<@Valid TransportMeans> transportMeans) {
    this.transportMeans = transportMeans;
  }

  public LoadEvent involvedCargo(List<@Valid LoadEventInvolvedCargoInner> involvedCargo) {
    this.involvedCargo = involvedCargo;
    return this;
  }

  public LoadEvent addInvolvedCargoItem(LoadEventInvolvedCargoInner involvedCargoItem) {
    if (this.involvedCargo == null) {
      this.involvedCargo = new ArrayList<>();
    }
    this.involvedCargo.add(involvedCargoItem);
    return this;
  }

  /**
   * Get involvedCargo
   * @return involvedCargo
  */
  @Valid 
  @Schema(name = "involvedCargo", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("involvedCargo")
  public List<@Valid LoadEventInvolvedCargoInner> getInvolvedCargo() {
    return involvedCargo;
  }

  public void setInvolvedCargo(List<@Valid LoadEventInvolvedCargoInner> involvedCargo) {
    this.involvedCargo = involvedCargo;
  }

  public LoadEvent timestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  /**
   * Get timestamp
   * @return timestamp
  */
  @NotNull @Valid 
  @Schema(name = "timestamp", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("timestamp")
  public OffsetDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(OffsetDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public LoadEvent timeClassification(TimeClassificationEnum timeClassification) {
    this.timeClassification = timeClassification;
    return this;
  }

  /**
   * Get timeClassification
   * @return timeClassification
  */
  @NotNull 
  @Schema(name = "timeClassification", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("timeClassification")
  public TimeClassificationEnum getTimeClassification() {
    return timeClassification;
  }

  public void setTimeClassification(TimeClassificationEnum timeClassification) {
    this.timeClassification = timeClassification;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoadEvent loadEvent = (LoadEvent) o;
    return Objects.equals(this.UUID, loadEvent.UUID) &&
        Objects.equals(this.involvedActors, loadEvent.involvedActors) &&
        Objects.equals(this.transportMeans, loadEvent.transportMeans) &&
        Objects.equals(this.involvedCargo, loadEvent.involvedCargo) &&
        Objects.equals(this.timestamp, loadEvent.timestamp) &&
        Objects.equals(this.timeClassification, loadEvent.timeClassification);
  }

  @Override
  public int hashCode() {
    return Objects.hash(UUID, involvedActors, transportMeans, involvedCargo, timestamp, timeClassification);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoadEvent {\n");
    sb.append("    UUID: ").append(toIndentedString(UUID)).append("\n");
    sb.append("    involvedActors: ").append(toIndentedString(involvedActors)).append("\n");
    sb.append("    transportMeans: ").append(toIndentedString(transportMeans)).append("\n");
    sb.append("    involvedCargo: ").append(toIndentedString(involvedCargo)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    timeClassification: ").append(toIndentedString(timeClassification)).append("\n");
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

