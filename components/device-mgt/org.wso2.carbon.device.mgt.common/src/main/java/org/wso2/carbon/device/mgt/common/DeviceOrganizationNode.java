/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

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
