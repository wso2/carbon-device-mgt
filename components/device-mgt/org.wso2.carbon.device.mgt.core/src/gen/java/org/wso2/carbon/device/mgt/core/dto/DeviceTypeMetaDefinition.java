package org.wso2.carbon.device.mgt.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.device.mgt.core.dto.Feature;
import org.wso2.carbon.device.mgt.core.dto.InitialOperationConfig;
import org.wso2.carbon.device.mgt.core.dto.License;
import org.wso2.carbon.device.mgt.core.dto.PushNotificationConfig;
import java.util.Objects;

/**
 * DeviceTypeMetaDefinition
 */
public class DeviceTypeMetaDefinition   {
  @JsonProperty("properties")
  private List<String> properties = new ArrayList<String>();

  @JsonProperty("features")
  private List<Feature> features = new ArrayList<Feature>();

  @JsonProperty("claimable")
  private Boolean claimable = false;

  @JsonProperty("pushNotificationConfig")
  private PushNotificationConfig pushNotificationConfig = null;

  @JsonProperty("initialOperationConfig")
  private InitialOperationConfig initialOperationConfig = null;

  @JsonProperty("license")
  private License license = null;

  @JsonProperty("description")
  private String description = null;

  public DeviceTypeMetaDefinition properties(List<String> properties) {
    this.properties = properties;
    return this;
  }

  public DeviceTypeMetaDefinition addPropertiesItem(String propertiesItem) {
    this.properties.add(propertiesItem);
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public List<String> getProperties() {
    return properties;
  }

  public void setProperties(List<String> properties) {
    this.properties = properties;
  }

  public DeviceTypeMetaDefinition features(List<Feature> features) {
    this.features = features;
    return this;
  }

  public DeviceTypeMetaDefinition addFeaturesItem(Feature featuresItem) {
    this.features.add(featuresItem);
    return this;
  }

   /**
   * Get features
   * @return features
  **/
  @ApiModelProperty(value = "")
  public List<Feature> getFeatures() {
    return features;
  }

  public void setFeatures(List<Feature> features) {
    this.features = features;
  }

  public DeviceTypeMetaDefinition claimable(Boolean claimable) {
    this.claimable = claimable;
    return this;
  }

   /**
   * If the device is a claimable device or not.
   * @return claimable
  **/
  @ApiModelProperty(value = "If the device is a claimable device or not.")
  public Boolean getClaimable() {
    return claimable;
  }

  public void setClaimable(Boolean claimable) {
    this.claimable = claimable;
  }

  public DeviceTypeMetaDefinition pushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
    this.pushNotificationConfig = pushNotificationConfig;
    return this;
  }

   /**
   * Get pushNotificationConfig
   * @return pushNotificationConfig
  **/
  @ApiModelProperty(value = "")
  public PushNotificationConfig getPushNotificationConfig() {
    return pushNotificationConfig;
  }

  public void setPushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
    this.pushNotificationConfig = pushNotificationConfig;
  }

  public DeviceTypeMetaDefinition initialOperationConfig(InitialOperationConfig initialOperationConfig) {
    this.initialOperationConfig = initialOperationConfig;
    return this;
  }

   /**
   * Get initialOperationConfig
   * @return initialOperationConfig
  **/
  @ApiModelProperty(value = "")
  public InitialOperationConfig getInitialOperationConfig() {
    return initialOperationConfig;
  }

  public void setInitialOperationConfig(InitialOperationConfig initialOperationConfig) {
    this.initialOperationConfig = initialOperationConfig;
  }

  public DeviceTypeMetaDefinition license(License license) {
    this.license = license;
    return this;
  }

   /**
   * Get license
   * @return license
  **/
  @ApiModelProperty(value = "")
  public License getLicense() {
    return license;
  }

  public void setLicense(License license) {
    this.license = license;
  }

  public DeviceTypeMetaDefinition description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceTypeMetaDefinition deviceTypeMetaDefinition = (DeviceTypeMetaDefinition) o;
    return Objects.equals(this.properties, deviceTypeMetaDefinition.properties) &&
        Objects.equals(this.features, deviceTypeMetaDefinition.features) &&
        Objects.equals(this.claimable, deviceTypeMetaDefinition.claimable) &&
        Objects.equals(this.pushNotificationConfig, deviceTypeMetaDefinition.pushNotificationConfig) &&
        Objects.equals(this.initialOperationConfig, deviceTypeMetaDefinition.initialOperationConfig) &&
        Objects.equals(this.license, deviceTypeMetaDefinition.license) &&
        Objects.equals(this.description, deviceTypeMetaDefinition.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties, features, claimable, pushNotificationConfig, initialOperationConfig, license, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceTypeMetaDefinition {\n");
    
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    features: ").append(toIndentedString(features)).append("\n");
    sb.append("    claimable: ").append(toIndentedString(claimable)).append("\n");
    sb.append("    pushNotificationConfig: ").append(toIndentedString(pushNotificationConfig)).append("\n");
    sb.append("    initialOperationConfig: ").append(toIndentedString(initialOperationConfig)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

