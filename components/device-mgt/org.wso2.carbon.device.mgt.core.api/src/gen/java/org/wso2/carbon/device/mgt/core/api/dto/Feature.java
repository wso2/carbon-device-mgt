package org.wso2.carbon.device.mgt.core.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

/**
 * Feature class.
 */
public class Feature {
    @JsonProperty("id")
    private Integer id = null;

    @JsonProperty("code")
    private String code = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("deviceType")
    private String deviceType = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("metadataEntries")
    private List<MetadataEntry> metadataEntries = new ArrayList<MetadataEntry>();

    public Feature id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Returns feature id.
     *
     * @return id
     **/
    @ApiModelProperty(value = "Feature id")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Feature code(String code) {
        this.code = code;
        return this;
    }

    /**
     * A name that describes a feature.
     *
     * @return code
     **/
    @ApiModelProperty(value = "A name that describes a feature.")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Feature description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Provides a description of the features.
     *
     * @return description
     **/
    @ApiModelProperty(value = "Provides a description of the features.")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Feature deviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    /**
     * Provide the device type for the respective feature. Features allow you to perform operations on any device type,
     * such as android, iOS or windows.
     *
     * @return deviceType
     **/
    @ApiModelProperty(value = "Provide the device type for the respective feature. Features allow you to " +
            "perform operations on any device type, such as android, iOS or windows. ")
    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Feature name(String name) {
        this.name = name;
        return this;
    }

    /**
     * A name that describes a feature.
     *
     * @return name
     **/
    @ApiModelProperty(value = "A name that describes a feature.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Feature metadataEntries(List<MetadataEntry> metadataEntries) {
        this.metadataEntries = metadataEntries;
        return this;
    }

    public Feature addMetadataEntriesItem(MetadataEntry metadataEntriesItem) {
        this.metadataEntries.add(metadataEntriesItem);
        return this;
    }

    /**
     * Get metadataEntries.
     *
     * @return metadataEntries
     **/
    @ApiModelProperty(value = "")
    public List<MetadataEntry> getMetadataEntries() {
        return metadataEntries;
    }

    public void setMetadataEntries(List<MetadataEntry> metadataEntries) {
        this.metadataEntries = metadataEntries;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Feature feature = (Feature) o;
        return Objects.equals(this.id, feature.id) &&
                Objects.equals(this.code, feature.code) &&
                Objects.equals(this.description, feature.description) &&
                Objects.equals(this.deviceType, feature.deviceType) &&
                Objects.equals(this.name, feature.name) &&
                Objects.equals(this.metadataEntries, feature.metadataEntries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, description, deviceType, name, metadataEntries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Feature {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    code: ").append(toIndentedString(code)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    deviceType: ").append(toIndentedString(deviceType)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    metadataEntries: ").append(toIndentedString(metadataEntries)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces.
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

