/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.device.mgt.core;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.TenantConfiguration;

import java.util.List;
import java.util.Map;

public class TestDeviceManager implements DeviceManager {

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean saveConfiguration(TenantConfiguration configuration)
            throws DeviceManagementException {
        return false;
    }

    @Override public TenantConfiguration getConfiguration() throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        return true;
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException {
        return false;
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return null;
    }

    @Override
    public boolean updateDeviceInfo(DeviceIdentifier deviceIdentifier, Device device)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType)
            throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean isClaimable(DeviceIdentifier deviceId) throws DeviceManagementException {
        return false;
    }

    @Override
    public boolean setStatus(DeviceIdentifier deviceId, String currentOwner, EnrolmentInfo.Status status)
            throws DeviceManagementException {
        return false;
    }
}
