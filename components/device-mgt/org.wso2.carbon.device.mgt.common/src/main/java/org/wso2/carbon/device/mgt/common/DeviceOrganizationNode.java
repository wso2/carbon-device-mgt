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

    @ApiModelProperty(name = "children", value = "list of children devices connected to device",
            required = true)
    private List<DeviceOrganizationNode> children;

    @ApiModelProperty(name = "parent", value = "immediate parent of device",
            required = true)
    private String parent;

    public DeviceOrganizationNode(String id) {
        this.id = id;
    }

    public DeviceOrganizationNode(String id, String parent) {
        this.id = id;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DeviceOrganizationNode> getChildren() {
        return children;
    }

    public void setChildren(List<DeviceOrganizationNode> children) {
        this.children = children;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public void setChild(DeviceOrganizationNode child) {
        children.add(child);
    }
}
