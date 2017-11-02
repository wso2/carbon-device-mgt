package org.wso2.carbon.device.mgt.core.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.device.mgt.core.dto.Device;
import java.util.Objects;

/**
 * DeviceList
 */
public class DeviceList   {
  @JsonProperty("devices")
  private List<Device> devices = new ArrayList<Device>();

  public DeviceList devices(List<Device> devices) {
    this.devices = devices;
    return this;
  }

  public DeviceList addDevicesItem(Device devicesItem) {
    this.devices.add(devicesItem);
    return this;
  }

   /**
   * Devices list.
   * @return devices
  **/
  @ApiModelProperty(value = "Devices list.")
  public List<Device> getDevices() {
    return devices;
  }

  public void setDevices(List<Device> devices) {
    this.devices = devices;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceList deviceList = (DeviceList) o;
    return Objects.equals(this.devices, deviceList.devices);
  }

  @Override
  public int hashCode() {
    return Objects.hash(devices);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceList {\n");
    
    sb.append("    devices: ").append(toIndentedString(devices)).append("\n");
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

