package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.device.mgt.deviceagent.api.dto.DeviceLocation;
import java.util.Objects;

/**
 * This class carries all information related to the device information provided by a device.
 */
@ApiModel(description = "This class carries all information related to the device information provided by a device.")
public class DeviceInfo   {
  @JsonProperty("deviceModel")
  private String deviceModel = null;

  @JsonProperty("vendor")
  private String vendor = null;

  @JsonProperty("osVersion")
  private String osVersion = null;

  @JsonProperty("osBuildDate")
  private String osBuildDate = null;

  @JsonProperty("batteryLevel")
  private Double batteryLevel = null;

  @JsonProperty("internalTotalMemory")
  private Double internalTotalMemory = null;

  @JsonProperty("internalAvailableMemory")
  private Double internalAvailableMemory = null;

  @JsonProperty("externalTotalMemory")
  private Double externalTotalMemory = null;

  @JsonProperty("externalAvailableMemory")
  private Double externalAvailableMemory = null;

  @JsonProperty("operator")
  private String operator = null;

  @JsonProperty("connectionType")
  private String connectionType = null;

  @JsonProperty("mobileSignalStrength")
  private Double mobileSignalStrength = null;

  @JsonProperty("ssid")
  private String ssid = null;

  @JsonProperty("cpuUsage")
  private Double cpuUsage = null;

  @JsonProperty("totalRAMMemory")
  private Double totalRAMMemory = null;

  @JsonProperty("availableRAMMemory")
  private Double availableRAMMemory = null;

  @JsonProperty("pluggedIn")
  private Boolean pluggedIn = false;

  @JsonProperty("updatedTime")
  private Date updatedTime = null;

  @JsonProperty("location")
  private DeviceLocation location = null;

  @JsonProperty("deviceDetailsMap")
  private Map<String, String> deviceDetailsMap = new HashMap<String, String>();

  @JsonProperty("imei")
  private String imei = null;

  @JsonProperty("imsi")
  private String imsi = null;

  public DeviceInfo deviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
    return this;
  }

   /**
   * Model of the device.
   * @return deviceModel
  **/
  @ApiModelProperty(example = "HTC HTC_M9pw", value = "Model of the device.")
  public String getDeviceModel() {
    return deviceModel;
  }

  public void setDeviceModel(String deviceModel) {
    this.deviceModel = deviceModel;
  }

  public DeviceInfo vendor(String vendor) {
    this.vendor = vendor;
    return this;
  }

   /**
   * Vendor of the device.
   * @return vendor
  **/
  @ApiModelProperty(example = "HTC", value = "Vendor of the device.")
  public String getVendor() {
    return vendor;
  }

  public void setVendor(String vendor) {
    this.vendor = vendor;
  }

  public DeviceInfo osVersion(String osVersion) {
    this.osVersion = osVersion;
    return this;
  }

   /**
   * Operating system version.
   * @return osVersion
  **/
  @ApiModelProperty(value = "Operating system version.")
  public String getOsVersion() {
    return osVersion;
  }

  public void setOsVersion(String osVersion) {
    this.osVersion = osVersion;
  }

  public DeviceInfo osBuildDate(String osBuildDate) {
    this.osBuildDate = osBuildDate;
    return this;
  }

   /**
   * Operating system build date.
   * @return osBuildDate
  **/
  @ApiModelProperty(example = "Thu, 9 Mar 2017 15:46:46", value = "Operating system build date.")
  public String getOsBuildDate() {
    return osBuildDate;
  }

  public void setOsBuildDate(String osBuildDate) {
    this.osBuildDate = osBuildDate;
  }

  public DeviceInfo batteryLevel(Double batteryLevel) {
    this.batteryLevel = batteryLevel;
    return this;
  }

   /**
   * Battery level of the device.
   * @return batteryLevel
  **/
  @ApiModelProperty(value = "Battery level of the device.")
  public Double getBatteryLevel() {
    return batteryLevel;
  }

  public void setBatteryLevel(Double batteryLevel) {
    this.batteryLevel = batteryLevel;
  }

  public DeviceInfo internalTotalMemory(Double internalTotalMemory) {
    this.internalTotalMemory = internalTotalMemory;
    return this;
  }

   /**
   * Total internal memory of the device.
   * @return internalTotalMemory
  **/
  @ApiModelProperty(value = "Total internal memory of the device.")
  public Double getInternalTotalMemory() {
    return internalTotalMemory;
  }

  public void setInternalTotalMemory(Double internalTotalMemory) {
    this.internalTotalMemory = internalTotalMemory;
  }

  public DeviceInfo internalAvailableMemory(Double internalAvailableMemory) {
    this.internalAvailableMemory = internalAvailableMemory;
    return this;
  }

   /**
   * Total available memory of the device.
   * @return internalAvailableMemory
  **/
  @ApiModelProperty(value = "Total available memory of the device.")
  public Double getInternalAvailableMemory() {
    return internalAvailableMemory;
  }

  public void setInternalAvailableMemory(Double internalAvailableMemory) {
    this.internalAvailableMemory = internalAvailableMemory;
  }

  public DeviceInfo externalTotalMemory(Double externalTotalMemory) {
    this.externalTotalMemory = externalTotalMemory;
    return this;
  }

   /**
   * Total external memory of the device.
   * @return externalTotalMemory
  **/
  @ApiModelProperty(value = "Total external memory of the device.")
  public Double getExternalTotalMemory() {
    return externalTotalMemory;
  }

  public void setExternalTotalMemory(Double externalTotalMemory) {
    this.externalTotalMemory = externalTotalMemory;
  }

  public DeviceInfo externalAvailableMemory(Double externalAvailableMemory) {
    this.externalAvailableMemory = externalAvailableMemory;
    return this;
  }

   /**
   * Total external memory avilable of the device.
   * @return externalAvailableMemory
  **/
  @ApiModelProperty(value = "Total external memory avilable of the device.")
  public Double getExternalAvailableMemory() {
    return externalAvailableMemory;
  }

  public void setExternalAvailableMemory(Double externalAvailableMemory) {
    this.externalAvailableMemory = externalAvailableMemory;
  }

  public DeviceInfo operator(String operator) {
    this.operator = operator;
    return this;
  }

   /**
   * Mobile operator of the device.(carrier)
   * @return operator
  **/
  @ApiModelProperty(value = "Mobile operator of the device.(carrier)")
  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public DeviceInfo connectionType(String connectionType) {
    this.connectionType = connectionType;
    return this;
  }

   /**
   * How the device is connected to the network.
   * @return connectionType
  **/
  @ApiModelProperty(value = "How the device is connected to the network.")
  public String getConnectionType() {
    return connectionType;
  }

  public void setConnectionType(String connectionType) {
    this.connectionType = connectionType;
  }

  public DeviceInfo mobileSignalStrength(Double mobileSignalStrength) {
    this.mobileSignalStrength = mobileSignalStrength;
    return this;
  }

   /**
   * Current mobile signal strength.
   * @return mobileSignalStrength
  **/
  @ApiModelProperty(value = "Current mobile signal strength.")
  public Double getMobileSignalStrength() {
    return mobileSignalStrength;
  }

  public void setMobileSignalStrength(Double mobileSignalStrength) {
    this.mobileSignalStrength = mobileSignalStrength;
  }

  public DeviceInfo ssid(String ssid) {
    this.ssid = ssid;
    return this;
  }

   /**
   * ssid of the connected WiFi.
   * @return ssid
  **/
  @ApiModelProperty(value = "ssid of the connected WiFi.")
  public String getSsid() {
    return ssid;
  }

  public void setSsid(String ssid) {
    this.ssid = ssid;
  }

  public DeviceInfo cpuUsage(Double cpuUsage) {
    this.cpuUsage = cpuUsage;
    return this;
  }

   /**
   * Current total cpu usage.
   * @return cpuUsage
  **/
  @ApiModelProperty(value = "Current total cpu usage.")
  public Double getCpuUsage() {
    return cpuUsage;
  }

  public void setCpuUsage(Double cpuUsage) {
    this.cpuUsage = cpuUsage;
  }

  public DeviceInfo totalRAMMemory(Double totalRAMMemory) {
    this.totalRAMMemory = totalRAMMemory;
    return this;
  }

   /**
   * Total Ram memory size.
   * @return totalRAMMemory
  **/
  @ApiModelProperty(value = "Total Ram memory size.")
  public Double getTotalRAMMemory() {
    return totalRAMMemory;
  }

  public void setTotalRAMMemory(Double totalRAMMemory) {
    this.totalRAMMemory = totalRAMMemory;
  }

  public DeviceInfo availableRAMMemory(Double availableRAMMemory) {
    this.availableRAMMemory = availableRAMMemory;
    return this;
  }

   /**
   * Available total memory of RAM.
   * @return availableRAMMemory
  **/
  @ApiModelProperty(value = "Available total memory of RAM.")
  public Double getAvailableRAMMemory() {
    return availableRAMMemory;
  }

  public void setAvailableRAMMemory(Double availableRAMMemory) {
    this.availableRAMMemory = availableRAMMemory;
  }

  public DeviceInfo pluggedIn(Boolean pluggedIn) {
    this.pluggedIn = pluggedIn;
    return this;
  }

   /**
   * Whether the device is plugged into power or not.
   * @return pluggedIn
  **/
  @ApiModelProperty(value = "Whether the device is plugged into power or not.")
  public Boolean getPluggedIn() {
    return pluggedIn;
  }

  public void setPluggedIn(Boolean pluggedIn) {
    this.pluggedIn = pluggedIn;
  }

  public DeviceInfo updatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

   /**
   * Device updated time.
   * @return updatedTime
  **/
  @ApiModelProperty(value = "Device updated time.")
  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }

  public DeviceInfo location(DeviceLocation location) {
    this.location = location;
    return this;
  }

   /**
   * Get location
   * @return location
  **/
  @ApiModelProperty(value = "")
  public DeviceLocation getLocation() {
    return location;
  }

  public void setLocation(DeviceLocation location) {
    this.location = location;
  }

  public DeviceInfo deviceDetailsMap(Map<String, String> deviceDetailsMap) {
    this.deviceDetailsMap = deviceDetailsMap;
    return this;
  }

  public DeviceInfo putDeviceDetailsMapItem(String key, String deviceDetailsMapItem) {
    this.deviceDetailsMap.put(key, deviceDetailsMapItem);
    return this;
  }

   /**
   * Any adddition datata to be kept
   * @return deviceDetailsMap
  **/
  @ApiModelProperty(value = "Any adddition datata to be kept")
  public Map<String, String> getDeviceDetailsMap() {
    return deviceDetailsMap;
  }

  public void setDeviceDetailsMap(Map<String, String> deviceDetailsMap) {
    this.deviceDetailsMap = deviceDetailsMap;
  }

  public DeviceInfo imei(String imei) {
    this.imei = imei;
    return this;
  }

   /**
   * IMEI number of the device.
   * @return imei
  **/
  @ApiModelProperty(example = "358812061121105", value = "IMEI number of the device.")
  public String getImei() {
    return imei;
  }

  public void setImei(String imei) {
    this.imei = imei;
  }

  public DeviceInfo imsi(String imsi) {
    this.imsi = imsi;
    return this;
  }

   /**
   * IMSI number of the device.
   * @return imsi
  **/
  @ApiModelProperty(example = "084931108239011593", value = "IMSI number of the device.")
  public String getImsi() {
    return imsi;
  }

  public void setImsi(String imsi) {
    this.imsi = imsi;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceInfo deviceInfo = (DeviceInfo) o;
    return Objects.equals(this.deviceModel, deviceInfo.deviceModel) &&
        Objects.equals(this.vendor, deviceInfo.vendor) &&
        Objects.equals(this.osVersion, deviceInfo.osVersion) &&
        Objects.equals(this.osBuildDate, deviceInfo.osBuildDate) &&
        Objects.equals(this.batteryLevel, deviceInfo.batteryLevel) &&
        Objects.equals(this.internalTotalMemory, deviceInfo.internalTotalMemory) &&
        Objects.equals(this.internalAvailableMemory, deviceInfo.internalAvailableMemory) &&
        Objects.equals(this.externalTotalMemory, deviceInfo.externalTotalMemory) &&
        Objects.equals(this.externalAvailableMemory, deviceInfo.externalAvailableMemory) &&
        Objects.equals(this.operator, deviceInfo.operator) &&
        Objects.equals(this.connectionType, deviceInfo.connectionType) &&
        Objects.equals(this.mobileSignalStrength, deviceInfo.mobileSignalStrength) &&
        Objects.equals(this.ssid, deviceInfo.ssid) &&
        Objects.equals(this.cpuUsage, deviceInfo.cpuUsage) &&
        Objects.equals(this.totalRAMMemory, deviceInfo.totalRAMMemory) &&
        Objects.equals(this.availableRAMMemory, deviceInfo.availableRAMMemory) &&
        Objects.equals(this.pluggedIn, deviceInfo.pluggedIn) &&
        Objects.equals(this.updatedTime, deviceInfo.updatedTime) &&
        Objects.equals(this.location, deviceInfo.location) &&
        Objects.equals(this.deviceDetailsMap, deviceInfo.deviceDetailsMap) &&
        Objects.equals(this.imei, deviceInfo.imei) &&
        Objects.equals(this.imsi, deviceInfo.imsi);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceModel, vendor, osVersion, osBuildDate, batteryLevel, internalTotalMemory, internalAvailableMemory, externalTotalMemory, externalAvailableMemory, operator, connectionType, mobileSignalStrength, ssid, cpuUsage, totalRAMMemory, availableRAMMemory, pluggedIn, updatedTime, location, deviceDetailsMap, imei, imsi);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceInfo {\n");
    
    sb.append("    deviceModel: ").append(toIndentedString(deviceModel)).append("\n");
    sb.append("    vendor: ").append(toIndentedString(vendor)).append("\n");
    sb.append("    osVersion: ").append(toIndentedString(osVersion)).append("\n");
    sb.append("    osBuildDate: ").append(toIndentedString(osBuildDate)).append("\n");
    sb.append("    batteryLevel: ").append(toIndentedString(batteryLevel)).append("\n");
    sb.append("    internalTotalMemory: ").append(toIndentedString(internalTotalMemory)).append("\n");
    sb.append("    internalAvailableMemory: ").append(toIndentedString(internalAvailableMemory)).append("\n");
    sb.append("    externalTotalMemory: ").append(toIndentedString(externalTotalMemory)).append("\n");
    sb.append("    externalAvailableMemory: ").append(toIndentedString(externalAvailableMemory)).append("\n");
    sb.append("    operator: ").append(toIndentedString(operator)).append("\n");
    sb.append("    connectionType: ").append(toIndentedString(connectionType)).append("\n");
    sb.append("    mobileSignalStrength: ").append(toIndentedString(mobileSignalStrength)).append("\n");
    sb.append("    ssid: ").append(toIndentedString(ssid)).append("\n");
    sb.append("    cpuUsage: ").append(toIndentedString(cpuUsage)).append("\n");
    sb.append("    totalRAMMemory: ").append(toIndentedString(totalRAMMemory)).append("\n");
    sb.append("    availableRAMMemory: ").append(toIndentedString(availableRAMMemory)).append("\n");
    sb.append("    pluggedIn: ").append(toIndentedString(pluggedIn)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
    sb.append("    deviceDetailsMap: ").append(toIndentedString(deviceDetailsMap)).append("\n");
    sb.append("    imei: ").append(toIndentedString(imei)).append("\n");
    sb.append("    imsi: ").append(toIndentedString(imsi)).append("\n");
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

