package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import java.util.Objects;

/**
 * This class carries all information related to the device location details provided by a device.
 */
@ApiModel(description = "This class carries all information related to the device location details provided by a device.")
public class DeviceLocation   {
  @JsonProperty("deviceId")
  private Integer deviceId = null;

  @JsonProperty("deviceType")
  private String deviceType = null;

  @JsonProperty("latitude")
  private Double latitude = null;

  @JsonProperty("longitude")
  private Double longitude = null;

  @JsonProperty("street1")
  private String street1 = null;

  @JsonProperty("street2")
  private String street2 = null;

  @JsonProperty("city")
  private String city = null;

  @JsonProperty("state")
  private String state = null;

  @JsonProperty("zip")
  private String zip = null;

  @JsonProperty("country")
  private String country = null;

  @JsonProperty("updatedTime")
  private Date updatedTime = null;

  public DeviceLocation deviceId(Integer deviceId) {
    this.deviceId = deviceId;
    return this;
  }

   /**
   * Id of the device in the database
   * @return deviceId
  **/
  @ApiModelProperty(value = "Id of the device in the database")
  public Integer getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(Integer deviceId) {
    this.deviceId = deviceId;
  }

  public DeviceLocation deviceType(String deviceType) {
    this.deviceType = deviceType;
    return this;
  }

   /**
   * The device type such as ios, android, windows or fire-alarm.
   * @return deviceType
  **/
  @ApiModelProperty(example = "android", value = "The device type such as ios, android, windows or fire-alarm.")
  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public DeviceLocation latitude(Double latitude) {
    this.latitude = latitude;
    return this;
  }

   /**
   * Device GPS latitude.
   * @return latitude
  **/
  @ApiModelProperty(example = "6.909857", value = "Device GPS latitude.")
  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public DeviceLocation longitude(Double longitude) {
    this.longitude = longitude;
    return this;
  }

   /**
   * Device GPS longitude.
   * @return longitude
  **/
  @ApiModelProperty(example = "79.852483", value = "Device GPS longitude.")
  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public DeviceLocation street1(String street1) {
    this.street1 = street1;
    return this;
  }

   /**
   * First line of the address.
   * @return street1
  **/
  @ApiModelProperty(example = "20, Palm Grove", value = "First line of the address.")
  public String getStreet1() {
    return street1;
  }

  public void setStreet1(String street1) {
    this.street1 = street1;
  }

  public DeviceLocation street2(String street2) {
    this.street2 = street2;
    return this;
  }

   /**
   * Second line of the address.
   * @return street2
  **/
  @ApiModelProperty(example = "Colombo", value = "Second line of the address.")
  public String getStreet2() {
    return street2;
  }

  public void setStreet2(String street2) {
    this.street2 = street2;
  }

  public DeviceLocation city(String city) {
    this.city = city;
    return this;
  }

   /**
   * City of the device location.
   * @return city
  **/
  @ApiModelProperty(example = "Colombo", value = "City of the device location.")
  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public DeviceLocation state(String state) {
    this.state = state;
    return this;
  }

   /**
   * State of the device address.
   * @return state
  **/
  @ApiModelProperty(example = "Western", value = "State of the device address.")
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public DeviceLocation zip(String zip) {
    this.zip = zip;
    return this;
  }

   /**
   * Zip code of the device address.
   * @return zip
  **/
  @ApiModelProperty(example = "192", value = "Zip code of the device address.")
  public String getZip() {
    return zip;
  }

  public void setZip(String zip) {
    this.zip = zip;
  }

  public DeviceLocation country(String country) {
    this.country = country;
    return this;
  }

   /**
   * Country of the device address.
   * @return country
  **/
  @ApiModelProperty(example = "Sri Lanka", value = "Country of the device address.")
  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public DeviceLocation updatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

   /**
   * Last updated location of the device
   * @return updatedTime
  **/
  @ApiModelProperty(value = "Last updated location of the device")
  public Date getUpdatedTime() {
    return updatedTime;
  }

  public void setUpdatedTime(Date updatedTime) {
    this.updatedTime = updatedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeviceLocation deviceLocation = (DeviceLocation) o;
    return Objects.equals(this.deviceId, deviceLocation.deviceId) &&
        Objects.equals(this.deviceType, deviceLocation.deviceType) &&
        Objects.equals(this.latitude, deviceLocation.latitude) &&
        Objects.equals(this.longitude, deviceLocation.longitude) &&
        Objects.equals(this.street1, deviceLocation.street1) &&
        Objects.equals(this.street2, deviceLocation.street2) &&
        Objects.equals(this.city, deviceLocation.city) &&
        Objects.equals(this.state, deviceLocation.state) &&
        Objects.equals(this.zip, deviceLocation.zip) &&
        Objects.equals(this.country, deviceLocation.country) &&
        Objects.equals(this.updatedTime, deviceLocation.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deviceId, deviceType, latitude, longitude, street1, street2, city, state, zip, country, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeviceLocation {\n");
    
    sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
    sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
    sb.append("    street1: ").append(toIndentedString(street1)).append("\n");
    sb.append("    street2: ").append(toIndentedString(street2)).append("\n");
    sb.append("    city: ").append(toIndentedString(city)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    zip: ").append(toIndentedString(zip)).append("\n");
    sb.append("    country: ").append(toIndentedString(country)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
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

