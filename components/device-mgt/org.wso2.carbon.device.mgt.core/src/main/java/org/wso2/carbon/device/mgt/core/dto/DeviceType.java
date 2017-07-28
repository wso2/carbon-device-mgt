/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;

import java.io.Serializable;

@ApiModel(value = "DeviceType", description = "This class carries all information related device types")
public class DeviceType implements Serializable {

    private static final long serialVersionUID = 7927802716452548282L;

    @ApiModelProperty(name = "id", value = "Device type id", required = true)
    private int id;
    @ApiModelProperty(name = "name", value = "Device type name", required = true)
    private String name;

    @ApiModelProperty(name = "metaDefinition", value = "Device type definition", required = true)
    private DeviceTypeMetaDefinition deviceTypeMetaDefinition;

    public DeviceType() {
    }

    public DeviceType(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        return deviceTypeMetaDefinition;
    }

    public void setDeviceTypeMetaDefinition(
            DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
    }

}
