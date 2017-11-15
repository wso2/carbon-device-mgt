/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.api.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * DeviceType bean.
 */
public class DeviceType {
    @JsonProperty("id")
    private Integer id = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("deviceTypeMetaDefinition")
    private DeviceTypeMetaDefinition deviceTypeMetaDefinition = null;

    public DeviceType id(Integer id) {
        this.id = id;
        return this;
    }

    /**
     * Id of the device type.
     *
     * @return id
     **/
    @ApiModelProperty(value = "Id of the device type.")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeviceType name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Name of the device type.
     *
     * @return name
     **/
    @ApiModelProperty(value = "Name of the device type.")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceType deviceTypeMetaDefinition(
            DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
        return this;
    }

    /**
     * Get deviceTypeMetaDefinition.
     *
     * @return deviceTypeMetaDefinition
     **/
    @ApiModelProperty(value = "")
    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        return deviceTypeMetaDefinition;
    }

    public void setDeviceTypeMetaDefinition(
            DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceType deviceType = (DeviceType) o;
        return Objects.equals(this.id, deviceType.id) &&
                Objects.equals(this.name, deviceType.name) &&
                Objects.equals(this.deviceTypeMetaDefinition, deviceType.deviceTypeMetaDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, deviceTypeMetaDefinition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceType {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    deviceTypeMetaDefinition: ").append(toIndentedString(deviceTypeMetaDefinition)).append("\n");
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

