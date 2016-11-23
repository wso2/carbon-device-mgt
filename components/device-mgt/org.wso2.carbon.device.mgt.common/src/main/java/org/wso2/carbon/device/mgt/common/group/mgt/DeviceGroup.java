/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common.group.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * Holds Device Group details and expose to external access
 */
@ApiModel(value = "DeviceGroup", description = "This class carries all information related to a managed device group.")
public class DeviceGroup implements Serializable {

    private static final long serialVersionUID = 1998121711L;

    @ApiModelProperty(name = "id", value = "ID of the device group in the device group information database.")
    private int id;

    @ApiModelProperty(name = "description", value = "The device group description that can be set on the device group by the user.",
            required = true)
    private String description;

    @ApiModelProperty(name = "name", value = "The device group name that can be set on the device group by the user.",
            required = true)
    private String name;

    private String owner;

    public DeviceGroup() {}

    public DeviceGroup(String name) {
        this.name = name;
    }

    public int getGroupId() {
        return id;
    }

    public void setGroupId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
