/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

@ApiModel(value = "DeviceHierarchyMetadataHolder", description = "This class carries all metadata related to " +
        "device hierarchical arrangement")
public class DeviceHierarchyMetadataHolder implements Serializable {

    @ApiModelProperty(name = "deviceId", value = "ID of the device as shown in the device hierarchy.",
            required = true)
    private String deviceId;

    @ApiModelProperty(name = "parent", value = "Parent of the device as it is located in the hierarchy.",
            required = true)
    private String deviceParent;

    @ApiModelProperty(name = "isParent", value = "Can identify if device is a gateway or not.",
            required = true)
    private int isParent;

    @ApiModelProperty(name = "tenantId", value = "Unique identifier of tenant.",
            required = true)
    private int tenantId;

    public DeviceHierarchyMetadataHolder() {
    }

    public DeviceHierarchyMetadataHolder(String deviceId, String deviceParent, int isParent, int tenantId) {
        this.deviceId = deviceId;
        this.deviceParent = deviceParent;
        this.isParent = isParent;
        this.tenantId = tenantId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceParent() {
        return deviceParent;
    }

    public void setDeviceParent(String deviceParent) {
        this.deviceParent = deviceParent;
    }

    public int getIsParent() {
        return isParent;
    }

    public void setIsParent(int isParent) {
        this.isParent = isParent;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "DeviceHierarchyMetadataHolder{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceParent='" + deviceParent + '\'' +
                ", isParent=" + isParent +
                ", tenantId=" + tenantId +
                '}';
    }
}
