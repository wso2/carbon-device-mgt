/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;

/**
 * This holds the information of the registered device management service against the device type
 * definition loaded timestamp. This is used to handle device type update scenario.
 */
public class DeviceManagementServiceHolder {

    private DeviceManagementService deviceManagementService;
    private long timestamp;

    public DeviceManagementServiceHolder(DeviceManagementService deviceManagementService) {
        this.deviceManagementService = deviceManagementService;
        this.timestamp = System.currentTimeMillis();
    }

    public DeviceManagementService getDeviceManagementService() {
        return deviceManagementService;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
