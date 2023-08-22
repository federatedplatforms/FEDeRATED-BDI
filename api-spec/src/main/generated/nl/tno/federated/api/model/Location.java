package nl.tno.federated.api.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import javax.annotation.Generated;

/**
 * Location
 */

@Schema(name = "Location", description = "Location")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T11:42:29.708+02:00[Europe/Amsterdam]")
public class Location {

  /**
   * Gets or Sets locationRole
   */
  public enum LocationRoleEnum {
    PLACEOFACCEPTANCE("PlaceOfAcceptance"),
    
    PLACEOFARRIVAL("PlaceOfArrival"),
    
    PLACEOFBORDERCROSSING("PlaceOfBorderCrossing"),
    
    PLACEOFCALL("PlaceOfCall"),
    
    PLACEOFDELIVERY("PlaceOfDelivery"),
    
    PLACEOFDEPARTURE("PlaceOfDeparture"),
    
    PLACEOFDISCHARGE("PlaceOfDischarge"),
    
    PLACEOFLOADING("PlaceOfLoading"),
    
    PLACEOFRECEIPT("PlaceOfReceipt"),
    
    PLACEOFSTRIPPING("PlaceOfStripping"),
    
    PLACEOFSTUFFING("PlaceOfStuffing"),
    
    PLACEOFUNLOADING("PlaceOfUnloading"),
    
    PORTOFCALL("PortOfCall"),
    
    PORTOFDISCHARGE("PortOfDischarge"),
    
    PORTOFLOADING("PortOfLoading");

    private String value;

    LocationRoleEnum(String value) {
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
    public static LocationRoleEnum fromValue(String value) {
      for (LocationRoleEnum b : LocationRoleEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private LocationRoleEnum locationRole;

  private Boolean businessLocationIntermediaryRole;

  private String postalCode;

  private String locatedAtStreetName;

  private String postalAddress;

  /**
   * Gets or Sets locatedInCountry
   */
  public enum LocatedInCountryEnum {
    AD("AD"),
    
    AE("AE"),
    
    AF("AF"),
    
    AG("AG"),
    
    AI("AI"),
    
    AL("AL"),
    
    AM("AM"),
    
    AO("AO"),
    
    AQ("AQ"),
    
    AR("AR"),
    
    AS("AS"),
    
    AT("AT"),
    
    AU("AU"),
    
    AW("AW"),
    
    AX("AX"),
    
    AZ("AZ"),
    
    BA("BA"),
    
    BB("BB"),
    
    BD("BD"),
    
    BE("BE"),
    
    BF("BF"),
    
    BG("BG"),
    
    BH("BH"),
    
    BI("BI"),
    
    BJ("BJ"),
    
    BL("BL"),
    
    BM("BM"),
    
    BN("BN"),
    
    BO("BO"),
    
    BQ("BQ"),
    
    BR("BR"),
    
    BS("BS"),
    
    BT("BT"),
    
    BW("BW"),
    
    BY("BY"),
    
    BZ("BZ"),
    
    CA("CA"),
    
    CC("CC"),
    
    CD("CD"),
    
    CF("CF"),
    
    CG("CG"),
    
    CH("CH"),
    
    CI("CI"),
    
    CK("CK"),
    
    CL("CL"),
    
    CM("CM"),
    
    CN("CN"),
    
    CO("CO"),
    
    CR("CR"),
    
    CU("CU"),
    
    CV("CV"),
    
    CW("CW"),
    
    CX("CX"),
    
    CY("CY"),
    
    CZ("CZ"),
    
    DE("DE"),
    
    DJ("DJ"),
    
    DK("DK"),
    
    DM("DM"),
    
    DO("DO"),
    
    DZ("DZ"),
    
    EC("EC"),
    
    EE("EE"),
    
    EG("EG"),
    
    EH("EH"),
    
    ER("ER"),
    
    ES("ES"),
    
    ET("ET"),
    
    FI("FI"),
    
    FJ("FJ"),
    
    FK("FK"),
    
    FM("FM"),
    
    FO("FO"),
    
    FR("FR"),
    
    GA("GA"),
    
    GB("GB"),
    
    GD("GD"),
    
    GE("GE"),
    
    GF("GF"),
    
    GG("GG"),
    
    GH("GH"),
    
    GI("GI"),
    
    GL("GL"),
    
    GM("GM"),
    
    GN("GN"),
    
    GP("GP"),
    
    GQ("GQ"),
    
    GR("GR"),
    
    GS("GS"),
    
    GT("GT"),
    
    GU("GU"),
    
    GW("GW"),
    
    GY("GY"),
    
    HK("HK"),
    
    HM("HM"),
    
    HN("HN"),
    
    HR("HR"),
    
    HT("HT"),
    
    HU("HU"),
    
    ID("ID"),
    
    IE("IE"),
    
    IL("IL"),
    
    IM("IM"),
    
    IN("IN"),
    
    IO("IO"),
    
    IQ("IQ"),
    
    IR("IR"),
    
    IS("IS"),
    
    IT("IT"),
    
    JE("JE"),
    
    JM("JM"),
    
    JO("JO"),
    
    JP("JP"),
    
    KE("KE"),
    
    KG("KG"),
    
    KH("KH"),
    
    KI("KI"),
    
    KM("KM"),
    
    KN("KN"),
    
    KP("KP"),
    
    KR("KR"),
    
    KW("KW"),
    
    KY("KY"),
    
    KZ("KZ"),
    
    LA("LA"),
    
    LB("LB"),
    
    LC("LC"),
    
    LI("LI"),
    
    LK("LK"),
    
    LR("LR"),
    
    LS("LS"),
    
    LT("LT"),
    
    LU("LU"),
    
    LV("LV"),
    
    LY("LY"),
    
    MA("MA"),
    
    MC("MC"),
    
    MD("MD"),
    
    ME("ME"),
    
    MF("MF"),
    
    MG("MG"),
    
    MH("MH"),
    
    MK("MK"),
    
    ML("ML"),
    
    MM("MM"),
    
    MN("MN"),
    
    MO("MO"),
    
    MP("MP"),
    
    MQ("MQ"),
    
    MR("MR"),
    
    MS("MS"),
    
    MT("MT"),
    
    MU("MU"),
    
    MV("MV"),
    
    MW("MW"),
    
    MX("MX"),
    
    MY("MY"),
    
    MZ("MZ"),
    
    NC("NC"),
    
    NE("NE"),
    
    NF("NF"),
    
    NG("NG"),
    
    NI("NI"),
    
    NL("NL"),
    
    FALSE("false"),
    
    NP("NP"),
    
    NR("NR"),
    
    NU("NU"),
    
    NZ("NZ"),
    
    OM("OM"),
    
    PA("PA"),
    
    PE("PE"),
    
    PF("PF"),
    
    PG("PG"),
    
    PH("PH"),
    
    PK("PK"),
    
    PL("PL"),
    
    PM("PM"),
    
    PN("PN"),
    
    PR("PR"),
    
    PS("PS"),
    
    PT("PT"),
    
    PW("PW"),
    
    PY("PY"),
    
    QA("QA"),
    
    RE("RE"),
    
    RO("RO"),
    
    RS("RS"),
    
    RU("RU"),
    
    RW("RW"),
    
    SA("SA"),
    
    SB("SB"),
    
    SC("SC"),
    
    SD("SD"),
    
    SE("SE"),
    
    SG("SG"),
    
    SH("SH"),
    
    SI("SI"),
    
    SJ("SJ"),
    
    SK("SK"),
    
    SL("SL"),
    
    SM("SM"),
    
    SN("SN"),
    
    SO("SO"),
    
    SR("SR"),
    
    SS("SS"),
    
    ST("ST"),
    
    SV("SV"),
    
    SX("SX"),
    
    SY("SY"),
    
    SZ("SZ"),
    
    TC("TC"),
    
    TD("TD"),
    
    TF("TF"),
    
    TG("TG"),
    
    TH("TH"),
    
    TJ("TJ"),
    
    TK("TK"),
    
    TL("TL"),
    
    TM("TM"),
    
    TN("TN"),
    
    TO("TO"),
    
    TR("TR"),
    
    TT("TT"),
    
    TV("TV"),
    
    TW("TW"),
    
    TZ("TZ"),
    
    UA("UA"),
    
    UG("UG"),
    
    UM("UM"),
    
    US("US"),
    
    UY("UY"),
    
    UZ("UZ"),
    
    VA("VA"),
    
    VC("VC"),
    
    VE("VE"),
    
    VG("VG"),
    
    VI("VI"),
    
    VN("VN"),
    
    VU("VU"),
    
    WF("WF"),
    
    WS("WS"),
    
    XZ("XZ"),
    
    YE("YE"),
    
    YT("YT"),
    
    ZA("ZA"),
    
    ZM("ZM"),
    
    ZW("ZW");

    private String value;

    LocatedInCountryEnum(String value) {
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
    public static LocatedInCountryEnum fromValue(String value) {
      for (LocatedInCountryEnum b : LocatedInCountryEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private LocatedInCountryEnum locatedInCountry;

  private String locatedInCity;

  /**
   * Default constructor
   * @deprecated Use {@link Location#Location(LocationRoleEnum, String, String, String, LocatedInCountryEnum, String)}
   */
  @Deprecated
  public Location() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public Location(LocationRoleEnum locationRole, String postalCode, String locatedAtStreetName, String postalAddress, LocatedInCountryEnum locatedInCountry, String locatedInCity) {
    this.locationRole = locationRole;
    this.postalCode = postalCode;
    this.locatedAtStreetName = locatedAtStreetName;
    this.postalAddress = postalAddress;
    this.locatedInCountry = locatedInCountry;
    this.locatedInCity = locatedInCity;
  }

  public Location locationRole(LocationRoleEnum locationRole) {
    this.locationRole = locationRole;
    return this;
  }

  /**
   * Get locationRole
   * @return locationRole
  */
  @NotNull 
  @Schema(name = "locationRole", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locationRole")
  public LocationRoleEnum getLocationRole() {
    return locationRole;
  }

  public void setLocationRole(LocationRoleEnum locationRole) {
    this.locationRole = locationRole;
  }

  public Location businessLocationIntermediaryRole(Boolean businessLocationIntermediaryRole) {
    this.businessLocationIntermediaryRole = businessLocationIntermediaryRole;
    return this;
  }

  /**
   * Get businessLocationIntermediaryRole
   * @return businessLocationIntermediaryRole
  */
  
  @Schema(name = "businessLocationIntermediaryRole", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("businessLocationIntermediaryRole")
  public Boolean getBusinessLocationIntermediaryRole() {
    return businessLocationIntermediaryRole;
  }

  public void setBusinessLocationIntermediaryRole(Boolean businessLocationIntermediaryRole) {
    this.businessLocationIntermediaryRole = businessLocationIntermediaryRole;
  }

  public Location postalCode(String postalCode) {
    this.postalCode = postalCode;
    return this;
  }

  /**
   * Get postalCode
   * @return postalCode
  */
  @NotNull 
  @Schema(name = "postalCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalCode")
  public String getPostalCode() {
    return postalCode;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public Location locatedAtStreetName(String locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
    return this;
  }

  /**
   * Get locatedAtStreetName
   * @return locatedAtStreetName
  */
  @NotNull 
  @Schema(name = "locatedAtStreetName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedAtStreetName")
  public String getLocatedAtStreetName() {
    return locatedAtStreetName;
  }

  public void setLocatedAtStreetName(String locatedAtStreetName) {
    this.locatedAtStreetName = locatedAtStreetName;
  }

  public Location postalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
    return this;
  }

  /**
   * Get postalAddress
   * @return postalAddress
  */
  @NotNull 
  @Schema(name = "postalAddress", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("postalAddress")
  public String getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(String postalAddress) {
    this.postalAddress = postalAddress;
  }

  public Location locatedInCountry(LocatedInCountryEnum locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
    return this;
  }

  /**
   * Get locatedInCountry
   * @return locatedInCountry
  */
  @NotNull 
  @Schema(name = "locatedInCountry", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCountry")
  public LocatedInCountryEnum getLocatedInCountry() {
    return locatedInCountry;
  }

  public void setLocatedInCountry(LocatedInCountryEnum locatedInCountry) {
    this.locatedInCountry = locatedInCountry;
  }

  public Location locatedInCity(String locatedInCity) {
    this.locatedInCity = locatedInCity;
    return this;
  }

  /**
   * Get locatedInCity
   * @return locatedInCity
  */
  @NotNull 
  @Schema(name = "locatedInCity", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("locatedInCity")
  public String getLocatedInCity() {
    return locatedInCity;
  }

  public void setLocatedInCity(String locatedInCity) {
    this.locatedInCity = locatedInCity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location location = (Location) o;
    return Objects.equals(this.locationRole, location.locationRole) &&
        Objects.equals(this.businessLocationIntermediaryRole, location.businessLocationIntermediaryRole) &&
        Objects.equals(this.postalCode, location.postalCode) &&
        Objects.equals(this.locatedAtStreetName, location.locatedAtStreetName) &&
        Objects.equals(this.postalAddress, location.postalAddress) &&
        Objects.equals(this.locatedInCountry, location.locatedInCountry) &&
        Objects.equals(this.locatedInCity, location.locatedInCity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(locationRole, businessLocationIntermediaryRole, postalCode, locatedAtStreetName, postalAddress, locatedInCountry, locatedInCity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Location {\n");
    sb.append("    locationRole: ").append(toIndentedString(locationRole)).append("\n");
    sb.append("    businessLocationIntermediaryRole: ").append(toIndentedString(businessLocationIntermediaryRole)).append("\n");
    sb.append("    postalCode: ").append(toIndentedString(postalCode)).append("\n");
    sb.append("    locatedAtStreetName: ").append(toIndentedString(locatedAtStreetName)).append("\n");
    sb.append("    postalAddress: ").append(toIndentedString(postalAddress)).append("\n");
    sb.append("    locatedInCountry: ").append(toIndentedString(locatedInCountry)).append("\n");
    sb.append("    locatedInCity: ").append(toIndentedString(locatedInCity)).append("\n");
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

