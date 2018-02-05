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
