package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceOrganizationVisNode", description = "This class contains details used to generate a node in " +
        "the visualization")
public class DeviceOrganizationVisNode implements Serializable {

    @ApiModelProperty(name = "id", value = "ID of the node generated. Same as Device ID",
            required = true)
    private String id;

    @ApiModelProperty(name = "label", value = "Device name as suggested by the user.",
            required = true)
    private String label;

    @ApiModelProperty(name = "size", value = "Size of the node in the Visualization.",
            required = true)
    private int size;

    @ApiModelProperty(name = "color", value = "Color of the node in the Visualization.",
            required = true)
    private String color;

    public DeviceOrganizationVisNode(String id, String label, int size, String color) {
        this.id = id;
        this.label = label;
        this.size = size;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
