package org.wso2.carbon.device.mgt.deviceagent.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class carries all information related application
 */
@ApiModel(description = "This class carries all information related application")
public class Application   {
  @JsonProperty("id")
  private Integer id = null;

  @JsonProperty("platform")
  private String platform = null;

  @JsonProperty("category")
  private String category = null;

  @JsonProperty("name")
  private String name = null;

  @JsonProperty("locationUrl")
  private String locationUrl = null;

  @JsonProperty("imageUrl")
  private String imageUrl = null;

  @JsonProperty("version")
  private String version = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("appProperties")
  private Map<String, Object> appProperties = new HashMap<String, Object>();

  @JsonProperty("applicationIdentifier")
  private String applicationIdentifier = null;

  @JsonProperty("memoryUsage")
  private Integer memoryUsage = null;

  @JsonProperty("active")
  private Boolean active = false;

  public Application id(Integer id) {
    this.id = id;
    return this;
  }

   /**
   * The ID given to the application when it is stored in the database
   * @return id
  **/
  @ApiModelProperty(value = "The ID given to the application when it is stored in the database")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Application platform(String platform) {
    this.platform = platform;
    return this;
  }

   /**
   * The mobile device platform. It can be android, ios or windows
   * @return platform
  **/
  @ApiModelProperty(value = "The mobile device platform. It can be android, ios or windows")
  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public Application category(String category) {
    this.category = category;
    return this;
  }

   /**
   * The application category
   * @return category
  **/
  @ApiModelProperty(value = "The application category")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Application name(String name) {
    this.name = name;
    return this;
  }

   /**
   * The application's name
   * @return name
  **/
  @ApiModelProperty(value = "The application's name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Application locationUrl(String locationUrl) {
    this.locationUrl = locationUrl;
    return this;
  }

   /**
   * The icon url of the application
   * @return locationUrl
  **/
  @ApiModelProperty(value = "The icon url of the application")
  public String getLocationUrl() {
    return locationUrl;
  }

  public void setLocationUrl(String locationUrl) {
    this.locationUrl = locationUrl;
  }

  public Application imageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

   /**
   * The image url of the application
   * @return imageUrl
  **/
  @ApiModelProperty(value = "The image url of the application")
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public Application version(String version) {
    this.version = version;
    return this;
  }

   /**
   * The application's version
   * @return version
  **/
  @ApiModelProperty(value = "The application's version")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Application type(String type) {
    this.type = type;
    return this;
  }

   /**
   * The application type
   * @return type
  **/
  @ApiModelProperty(value = "The application type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Application appProperties(Map<String, Object> appProperties) {
    this.appProperties = appProperties;
    return this;
  }

  public Application putAppPropertiesItem(String key, Object appPropertiesItem) {
    this.appProperties.put(key, appPropertiesItem);
    return this;
  }

   /**
   * The properties of the application
   * @return appProperties
  **/
  @ApiModelProperty(value = "The properties of the application")
  public Map<String, Object> getAppProperties() {
    return appProperties;
  }

  public void setAppProperties(Map<String, Object> appProperties) {
    this.appProperties = appProperties;
  }

  public Application applicationIdentifier(String applicationIdentifier) {
    this.applicationIdentifier = applicationIdentifier;
    return this;
  }

   /**
   * The application identifier
   * @return applicationIdentifier
  **/
  @ApiModelProperty(value = "The application identifier")
  public String getApplicationIdentifier() {
    return applicationIdentifier;
  }

  public void setApplicationIdentifier(String applicationIdentifier) {
    this.applicationIdentifier = applicationIdentifier;
  }

  public Application memoryUsage(Integer memoryUsage) {
    this.memoryUsage = memoryUsage;
    return this;
  }

   /**
   * Amount of memory used by the application
   * @return memoryUsage
  **/
  @ApiModelProperty(value = "Amount of memory used by the application")
  public Integer getMemoryUsage() {
    return memoryUsage;
  }

  public void setMemoryUsage(Integer memoryUsage) {
    this.memoryUsage = memoryUsage;
  }

  public Application active(Boolean active) {
    this.active = active;
    return this;
  }

   /**
   * Is the application actively running or not
   * @return active
  **/
  @ApiModelProperty(value = "Is the application actively running or not")
  public Boolean getActive() {
    return active;
  }

  public void setActive(Boolean active) {
    this.active = active;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Application application = (Application) o;
    return Objects.equals(this.id, application.id) &&
        Objects.equals(this.platform, application.platform) &&
        Objects.equals(this.category, application.category) &&
        Objects.equals(this.name, application.name) &&
        Objects.equals(this.locationUrl, application.locationUrl) &&
        Objects.equals(this.imageUrl, application.imageUrl) &&
        Objects.equals(this.version, application.version) &&
        Objects.equals(this.type, application.type) &&
        Objects.equals(this.appProperties, application.appProperties) &&
        Objects.equals(this.applicationIdentifier, application.applicationIdentifier) &&
        Objects.equals(this.memoryUsage, application.memoryUsage) &&
        Objects.equals(this.active, application.active);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, platform, category, name, locationUrl, imageUrl, version, type, appProperties, applicationIdentifier, memoryUsage, active);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Application {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    platform: ").append(toIndentedString(platform)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    locationUrl: ").append(toIndentedString(locationUrl)).append("\n");
    sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    appProperties: ").append(toIndentedString(appProperties)).append("\n");
    sb.append("    applicationIdentifier: ").append(toIndentedString(applicationIdentifier)).append("\n");
    sb.append("    memoryUsage: ").append(toIndentedString(memoryUsage)).append("\n");
    sb.append("    active: ").append(toIndentedString(active)).append("\n");
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

