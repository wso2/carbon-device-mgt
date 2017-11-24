package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * Representation of a device enrollment details
 */
@ApiModel(description = "Representation of a device enrollment details")
public class EnrolmentInfo   {
  @JsonProperty("id")
  private Integer id = null;

  /**
   * Current status of the device, such as whether the device is active, removed etc.
   */
  public enum StatusEnum {
    CREATED("CREATED"),
    
    ACTIVE("ACTIVE"),
    
    INACTIVE("INACTIVE"),
    
    UNREACHABLE("UNREACHABLE"),
    
    UNCLAIMED("UNCLAIMED"),
    
    SUSPENDED("SUSPENDED"),
    
    BLOCKED("BLOCKED"),
    
    REMOVED("REMOVED"),
    
    DISENROLLMENT_REQUESTED("DISENROLLMENT_REQUESTED");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("status")
  private StatusEnum status = null;

  /**
   * Provide the ownership status of the device.(BYOD, COPE) 
   */
  public enum OwnershipEnum {
    BYOD("BYOD"),
    
    COPE("COPE");

    private String value;

    OwnershipEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static OwnershipEnum fromValue(String text) {
      for (OwnershipEnum b : OwnershipEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("ownership")
  private OwnershipEnum ownership = null;

  @JsonProperty("owner")
  private String owner = null;

  @JsonProperty("dateOfEnrolment")
  private Long dateOfEnrolment = null;

  @JsonProperty("dateOfLastUpdate")
  private Long dateOfLastUpdate = null;

  public EnrolmentInfo id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * Id of the device in the database
   * @return id
  **/
  @ApiModelProperty(value = "Id of the device in the database")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public EnrolmentInfo status(StatusEnum status) {
    this.status = status;
    return this;
  }

   /**
   * Current status of the device, such as whether the device is active, removed etc.
   * @return status
  **/
  @ApiModelProperty(value = "Current status of the device, such as whether the device is active, removed etc.")
  public StatusEnum getStatus() {
    return status;
  }

  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  public EnrolmentInfo ownership(OwnershipEnum ownership) {
    this.ownership = ownership;
    return this;
  }

   /**
   * Provide the ownership status of the device.(BYOD, COPE) 
   * @return ownership
  **/
  @ApiModelProperty(value = "Provide the ownership status of the device.(BYOD, COPE) ")
  public OwnershipEnum getOwnership() {
    return ownership;
  }

  public void setOwnership(OwnershipEnum ownership) {
    this.ownership = ownership;
  }

  public EnrolmentInfo owner(String owner) {
    this.owner = owner;
    return this;
  }

   /**
   * The device owner's name.
   * @return owner
  **/
  @ApiModelProperty(example = "john", value = "The device owner's name.")
  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public EnrolmentInfo dateOfEnrolment(Long dateOfEnrolment) {
    this.dateOfEnrolment = dateOfEnrolment;
    return this;
  }

   /**
   * Date of the device enrollment. This value is not necessary.
   * @return dateOfEnrolment
  **/
  @ApiModelProperty(example = "1511371603", value = "Date of the device enrollment. This value is not necessary.")
  public Long getDateOfEnrolment() {
    return dateOfEnrolment;
  }

  public void setDateOfEnrolment(Long dateOfEnrolment) {
    this.dateOfEnrolment = dateOfEnrolment;
  }

  public EnrolmentInfo dateOfLastUpdate(Long dateOfLastUpdate) {
    this.dateOfLastUpdate = dateOfLastUpdate;
    return this;
  }

   /**
   * Date of the device's last update. This value is not necessary.
   * @return dateOfLastUpdate
  **/
  @ApiModelProperty(example = "1511371603", value = "Date of the device's last update. This value is not necessary.")
  public Long getDateOfLastUpdate() {
    return dateOfLastUpdate;
  }

  public void setDateOfLastUpdate(Long dateOfLastUpdate) {
    this.dateOfLastUpdate = dateOfLastUpdate;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EnrolmentInfo enrolmentInfo = (EnrolmentInfo) o;
    return Objects.equals(this.id, enrolmentInfo.id) &&
        Objects.equals(this.status, enrolmentInfo.status) &&
        Objects.equals(this.ownership, enrolmentInfo.ownership) &&
        Objects.equals(this.owner, enrolmentInfo.owner) &&
        Objects.equals(this.dateOfEnrolment, enrolmentInfo.dateOfEnrolment) &&
        Objects.equals(this.dateOfLastUpdate, enrolmentInfo.dateOfLastUpdate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, ownership, owner, dateOfEnrolment, dateOfLastUpdate);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnrolmentInfo {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    ownership: ").append(toIndentedString(ownership)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    dateOfEnrolment: ").append(toIndentedString(dateOfEnrolment)).append("\n");
    sb.append("    dateOfLastUpdate: ").append(toIndentedString(dateOfLastUpdate)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

