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

    public DeviceOrganizationVisNode(String id, String label) {
        this.id = id;
        this.label = label;
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
}
