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
import nl.tno.federated.api.model.Location;
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

@Schema(name = "ArrivalEvent", description = "Generated by TNO FEDeRATED")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:42:29.708+02:00[Europe/Amsterdam]")
public class ArrivalEvent {

  @Valid
  private List<@Valid Actor> involvedActors = new ArrayList<>();

  @Valid
  private List<@Valid TransportMeans> transportMeans = new ArrayList<>();

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

  private Location location;

  /**
   * Default constructor
   * @deprecated Use {@link ArrivalEvent#ArrivalEvent(List<@Valid Actor>, List<@Valid TransportMeans>, OffsetDateTime, TimeClassificationEnum)}
   */
  @Deprecated
  public ArrivalEvent() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ArrivalEvent(List<@Valid Actor> involvedActors, List<@Valid TransportMeans> transportMeans, OffsetDateTime timestamp, TimeClassificationEnum timeClassification) {
    this.involvedActors = involvedActors;
    this.transportMeans = transportMeans;
    this.timestamp = timestamp;
    this.timeClassification = timeClassification;
  }

  public ArrivalEvent involvedActors(List<@Valid Actor> involvedActors) {
    this.involvedActors = involvedActors;
    return this;
  }

  public ArrivalEvent addInvolvedActorsItem(Actor involvedActorsItem) {
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

  public ArrivalEvent transportMeans(List<@Valid TransportMeans> transportMeans) {
    this.transportMeans = transportMeans;
    return this;
  }

  public ArrivalEvent addTransportMeansItem(TransportMeans transportMeansItem) {
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

  public ArrivalEvent timestamp(OffsetDateTime timestamp) {
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

  public ArrivalEvent timeClassification(TimeClassificationEnum timeClassification) {
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

  public ArrivalEvent location(Location location) {
    this.location = location;
    return this;
  }

  /**
   * Get location
   * @return location
  */
  @Valid 
  @Schema(name = "location", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("location")
  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArrivalEvent arrivalEvent = (ArrivalEvent) o;
    return Objects.equals(this.involvedActors, arrivalEvent.involvedActors) &&
        Objects.equals(this.transportMeans, arrivalEvent.transportMeans) &&
        Objects.equals(this.timestamp, arrivalEvent.timestamp) &&
        Objects.equals(this.timeClassification, arrivalEvent.timeClassification) &&
        Objects.equals(this.location, arrivalEvent.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(involvedActors, transportMeans, timestamp, timeClassification, location);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArrivalEvent {\n");
    sb.append("    involvedActors: ").append(toIndentedString(involvedActors)).append("\n");
    sb.append("    transportMeans: ").append(toIndentedString(transportMeans)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
    sb.append("    timeClassification: ").append(toIndentedString(timeClassification)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
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

