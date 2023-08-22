package nl.tno.federated.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Generated;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Specialized digital twin, mostly truck
 */

@Schema(name = "TransportMeans", description = "Specialized digital twin, mostly truck")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2023-08-22T10:58:21.998+02:00[Europe/Amsterdam]")
public class TransportMeans {

  private String digitalTwinID;

  /**
   * Gets or Sets transportMeansMode
   */
  public enum TransportMeansModeEnum {
    SEA("Sea"),
    
    RAIL("Rail"),
    
    ROAD("Road"),
    
    AIR("Air"),
    
    MAIL("Mail"),
    
    MULTIMODAL("Multimodal"),
    
    PIPELINES("Pipelines"),
    
    INLANDWATERWAYS("InlandWaterways");

    private String value;

    TransportMeansModeEnum(String value) {
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
    public static TransportMeansModeEnum fromValue(String value) {
      for (TransportMeansModeEnum b : TransportMeansModeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private TransportMeansModeEnum transportMeansMode;

  /**
   * Gets or Sets hasTransportMeansNationality
   */
  public enum HasTransportMeansNationalityEnum {
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

    HasTransportMeansNationalityEnum(String value) {
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
    public static HasTransportMeansNationalityEnum fromValue(String value) {
      for (HasTransportMeansNationalityEnum b : HasTransportMeansNationalityEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private HasTransportMeansNationalityEnum hasTransportMeansNationality;

  /**
   * Default constructor
   * @deprecated Use {@link TransportMeans#TransportMeans(TransportMeansModeEnum)}
   */
  @Deprecated
  public TransportMeans() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public TransportMeans(TransportMeansModeEnum transportMeansMode) {
    this.transportMeansMode = transportMeansMode;
  }

  public TransportMeans digitalTwinID(String digitalTwinID) {
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

  public TransportMeans transportMeansMode(TransportMeansModeEnum transportMeansMode) {
    this.transportMeansMode = transportMeansMode;
    return this;
  }

  /**
   * Get transportMeansMode
   * @return transportMeansMode
  */
  @NotNull 
  @Schema(name = "transportMeansMode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("transportMeansMode")
  public TransportMeansModeEnum getTransportMeansMode() {
    return transportMeansMode;
  }

  public void setTransportMeansMode(TransportMeansModeEnum transportMeansMode) {
    this.transportMeansMode = transportMeansMode;
  }

  public TransportMeans hasTransportMeansNationality(HasTransportMeansNationalityEnum hasTransportMeansNationality) {
    this.hasTransportMeansNationality = hasTransportMeansNationality;
    return this;
  }

  /**
   * Get hasTransportMeansNationality
   * @return hasTransportMeansNationality
  */
  
  @Schema(name = "hasTransportMeansNationality", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("hasTransportMeansNationality")
  public HasTransportMeansNationalityEnum getHasTransportMeansNationality() {
    return hasTransportMeansNationality;
  }

  public void setHasTransportMeansNationality(HasTransportMeansNationalityEnum hasTransportMeansNationality) {
    this.hasTransportMeansNationality = hasTransportMeansNationality;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransportMeans transportMeans = (TransportMeans) o;
    return Objects.equals(this.digitalTwinID, transportMeans.digitalTwinID) &&
        Objects.equals(this.transportMeansMode, transportMeans.transportMeansMode) &&
        Objects.equals(this.hasTransportMeansNationality, transportMeans.hasTransportMeansNationality);
  }

  @Override
  public int hashCode() {
    return Objects.hash(digitalTwinID, transportMeansMode, hasTransportMeansNationality);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransportMeans {\n");
    sb.append("    digitalTwinID: ").append(toIndentedString(digitalTwinID)).append("\n");
    sb.append("    transportMeansMode: ").append(toIndentedString(transportMeansMode)).append("\n");
    sb.append("    hasTransportMeansNationality: ").append(toIndentedString(hasTransportMeansNationality)).append("\n");
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

