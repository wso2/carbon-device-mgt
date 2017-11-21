package org.wso2.carbon.device.mgt.devicetype.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.devicetype.api.dto.DeviceTypeMetaDefinition;
import java.util.Objects;

/**
 * DeviceType
 */
public class DeviceType   {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("deviceTypeMetaDefinition")
  private DeviceTypeMetaDefinition deviceTypeMetaDefinition = null;

  public DeviceType id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * Id of the device type.
   * @return id
  **/
  @ApiModelProperty(value = "Id of the device type.")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public DeviceType name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the device type.
   * @return name
  **/
  @ApiModelProperty(value = "Name of the device type.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DeviceType deviceTypeMetaDefinition(DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
    this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
    return this;
  }

   /**
   * Get deviceTypeMetaDefinition
   * @return deviceTypeMetaDefinition
  **/
  @ApiModelProperty(value = "")
  public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
    return deviceTypeMetaDefinition;
  }

  public void setDeviceTypeMetaDefinition(DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
    this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceType deviceType = (DeviceType) o;
    return Objects.equals(this.id, deviceType.id) &&
        Objects.equals(this.name, deviceType.name) &&
        Objects.equals(this.deviceTypeMetaDefinition, deviceType.deviceTypeMetaDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, deviceTypeMetaDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceType {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    deviceTypeMetaDefinition: ").append(toIndentedString(deviceTypeMetaDefinition)).append("\n");
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

