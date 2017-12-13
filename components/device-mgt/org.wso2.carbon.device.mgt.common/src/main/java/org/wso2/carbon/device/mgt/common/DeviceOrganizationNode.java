package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@ApiModel(value = "DeviceOrganizationNode", description = "This class contains details used to generate a node in " +
        "the hierarchy")
public class DeviceOrganizationNode implements Serializable {

    @ApiModelProperty(name = "id", value = "ID of the device. Same as Device ID",
            required = true)
    private String id;

    @ApiModelProperty(name = "children", value = "list of children devices connected",
            required = true)
    private Map<String, DeviceOrganizationNode> children;

    public DeviceOrganizationNode(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, DeviceOrganizationNode> getChildren() {
        return children;
    }

    public void setChildren(Map<String, DeviceOrganizationNode> children) {
        this.children = children;
    }

    public void setChild(DeviceOrganizationNode child) {
        children.put(child.id, child);
    }
}
