package org.wso2.carbon.device.mgt.core.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * MetadataEntry
 */
public class MetadataEntry {
    @JsonProperty("id")
    private Integer id = null;

    @JsonProperty("value")
    private Object value = null;

    public MetadataEntry id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Meta data ID
     *
     * @return id
     **/
    @ApiModelProperty(value = "Meta data ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public MetadataEntry value(Object value) {
        this.value = value;
        return this;
    }

    /**
     * Any value to be stored in the entry.
     *
     * @return value
     **/
    @ApiModelProperty(value = "Any value to be stored in the entry.")
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataEntry metadataEntry = (MetadataEntry) o;
        return Objects.equals(this.id, metadataEntry.id) &&
                Objects.equals(this.value, metadataEntry.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetadataEntry {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

