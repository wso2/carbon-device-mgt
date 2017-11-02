package org.wso2.carbon.device.mgt.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.device.mgt.core.dto.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dto.Feature;
import java.util.Objects;

/**
 * Device
 */
public class Device   {
  @JsonProperty("id")
  private String id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("deviceIdentifier")
  private String deviceIdentifier = null;

  @JsonProperty("enrolmentInfo")
  private EnrolmentInfo enrolmentInfo = null;

  @JsonProperty("features")
  private List<Feature> features = new ArrayList<Feature>();

  public Device id(String id) {
    this.id = id;
    return this;
  }

   /**
   * ID of the device in the device information database. This is devices unique Id
   * @return id
  **/
  @ApiModelProperty(value = "ID of the device in the device information database. This is devices unique Id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Device name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The device name that can be set on the device by the device user.
   * @return name
  **/
  @ApiModelProperty(value = "The device name that can be set on the device by the device user.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Device type(String type) {
    this.type = type;
    return this;
  }

   /**
   * The device type, such as ios, android or windows.
   * @return type
  **/
  @ApiModelProperty(value = "The device type, such as ios, android or windows.")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Device description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Additional information on the device.
   * @return description
  **/
  @ApiModelProperty(value = "Additional information on the device.")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Device deviceIdentifier(String deviceIdentifier) {
    this.deviceIdentifier = deviceIdentifier;
    return this;
  }

   /**
   * This is a 64-bit number (as a hex string) that is randomly generated when the user  first sets up the device and should remain constant for the lifetime of the user's  device. The value may change if a factory reset is performed on the device. 
   * @return deviceIdentifier
  **/
  @ApiModelProperty(value = "This is a 64-bit number (as a hex string) that is randomly generated when the user  first sets up the device and should remain constant for the lifetime of the user's  device. The value may change if a factory reset is performed on the device. ")
  public String getDeviceIdentifier() {
    return deviceIdentifier;
  }

  public void setDeviceIdentifier(String deviceIdentifier) {
    this.deviceIdentifier = deviceIdentifier;
  }

  public Device enrolmentInfo(EnrolmentInfo enrolmentInfo) {
    this.enrolmentInfo = enrolmentInfo;
    return this;
  }

   /**
   * Get enrolmentInfo
   * @return enrolmentInfo
  **/
  @ApiModelProperty(value = "")
  public EnrolmentInfo getEnrolmentInfo() {
    return enrolmentInfo;
  }

  public void setEnrolmentInfo(EnrolmentInfo enrolmentInfo) {
    this.enrolmentInfo = enrolmentInfo;
  }

  public Device features(List<Feature> features) {
    this.features = features;
    return this;
  }

  public Device addFeaturesItem(Feature featuresItem) {
    this.features.add(featuresItem);
    return this;
  }

   /**
   * feature list
   * @return features
  **/
  @ApiModelProperty(value = "feature list")
  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(List<Feature> features) {
    this.features = features;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Device device = (Device) o;
    return Objects.equals(this.id, device.id) &&
        Objects.equals(this.name, device.name) &&
        Objects.equals(this.type, device.type) &&
        Objects.equals(this.description, device.description) &&
        Objects.equals(this.deviceIdentifier, device.deviceIdentifier) &&
        Objects.equals(this.enrolmentInfo, device.enrolmentInfo) &&
        Objects.equals(this.features, device.features);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, type, description, deviceIdentifier, enrolmentInfo, features);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Device {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    deviceIdentifier: ").append(toIndentedString(deviceIdentifier)).append("\n");
    sb.append("    enrolmentInfo: ").append(toIndentedString(enrolmentInfo)).append("\n");
    sb.append("    features: ").append(toIndentedString(features)).append("\n");
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

