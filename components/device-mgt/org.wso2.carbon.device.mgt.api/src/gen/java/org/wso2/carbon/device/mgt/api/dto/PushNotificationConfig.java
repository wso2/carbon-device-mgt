package org.wso2.carbon.device.mgt.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * PushNotificationConfig
 */
public class PushNotificationConfig   {
  @JsonProperty("type")
  private String type = null;

  @JsonProperty("properties")
  private Map<String, String> properties = new HashMap<String, String>();

  @JsonProperty("isScheduled")
  private Boolean isScheduled = false;

  public PushNotificationConfig type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Type of the push notification provider.
   * @return type
  **/
  @ApiModelProperty(required = true, value = "Type of the push notification provider.")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public PushNotificationConfig properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  public PushNotificationConfig putPropertiesItem(String key, String propertiesItem) {
    this.properties.put(key, propertiesItem);
    return this;
  }

   /**
   * Get properties
   * @return properties
  **/
  @ApiModelProperty(value = "")
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public PushNotificationConfig isScheduled(Boolean isScheduled) {
    this.isScheduled = isScheduled;
    return this;
  }

   /**
   * if the operations are delivered to the devices on a scheduled manner or imediately
   * @return isScheduled
  **/
  @ApiModelProperty(value = "if the operations are delivered to the devices on a scheduled manner or imediately")
  public Boolean getIsScheduled() {
    return isScheduled;
  }

  public void setIsScheduled(Boolean isScheduled) {
    this.isScheduled = isScheduled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PushNotificationConfig pushNotificationConfig = (PushNotificationConfig) o;
    return Objects.equals(this.type, pushNotificationConfig.type) &&
        Objects.equals(this.properties, pushNotificationConfig.properties) &&
        Objects.equals(this.isScheduled, pushNotificationConfig.isScheduled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, properties, isScheduled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PushNotificationConfig {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("    isScheduled: ").append(toIndentedString(isScheduled)).append("\n");
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

