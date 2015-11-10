/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common.authorization;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a DeviceAuthorizationResult including a list of authorized devices and a list of unauthorized devices.
 */
public class DeviceAuthorizationResult {

    private List<DeviceIdentifier> authorizedDevices = new ArrayList<>();
    private List<DeviceIdentifier> unauthorizedDevices = new ArrayList<>();

    public List<DeviceIdentifier> getAuthorizedDevices() {
        return authorizedDevices;
    }

    public void setAuthorizedDevices(List<DeviceIdentifier> authorizedDevices) {
        this.authorizedDevices = authorizedDevices;
    }

    public void setUnauthorizedDevices(
            List<DeviceIdentifier> unauthorizedDevices) {
        this.unauthorizedDevices = unauthorizedDevices;
    }

    public void addAuthorizedDevice(DeviceIdentifier deviceIdentifier) {
        authorizedDevices.add(deviceIdentifier);
    }

    public List<DeviceIdentifier> getUnauthorizedDevices() {
        return unauthorizedDevices;
    }

    public void addUnauthorizedDevice(DeviceIdentifier deviceIdentifier) {
        unauthorizedDevices.add(deviceIdentifier);
    }
}
