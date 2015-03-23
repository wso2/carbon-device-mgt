/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;

import java.util.List;

public class DeviceManagementServiceImpl implements DeviceManagementService {

    @Override
    public String getProviderType() {
        return null;
    }

    @Override
    public FeatureManager getFeatureManager() {
        return null;
    }

    @Override
    public boolean enrollDevice(Device device) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().enrollDevice(device);
    }

    @Override
    public boolean modifyEnrollment(Device device) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().modifyEnrollment(device);
    }

    @Override
    public boolean disenrollDevice(DeviceIdentifier deviceId) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().disenrollDevice(deviceId);
    }

    @Override
    public boolean isEnrolled(DeviceIdentifier deviceId) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().isEnrolled(deviceId);
    }

    @Override
    public boolean isActive(DeviceIdentifier deviceId) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().isActive(deviceId);
    }

    @Override
    public boolean setActive(DeviceIdentifier deviceId, boolean status) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().setActive(deviceId, status);
    }

    @Override
    public List<Device> getAllDevices() throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getAllDevices();
    }

    @Override
    public List<Device> getAllDevices(String type) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getAllDevices(type);
    }

    public List<Device> getDeviceListOfUser(String username) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDeviceListOfUser(username);
    }

    public FeatureManager getFeatureManager(String type) throws DeviceManagementException {
        return null;
    }

    @Override
    public org.wso2.carbon.device.mgt.common.Device getDevice(DeviceIdentifier deviceId)
            throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId);
    }

    @Override
    public boolean updateDeviceInfo(Device device) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().updateDeviceInfo(device);
    }

    @Override
    public boolean setOwnership(DeviceIdentifier deviceId, String ownershipType) throws DeviceManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().setOwnership(deviceId,
                ownershipType);
    }

    @Override
    public License getLicense(String deviceType, String languageCode) throws LicenseManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getLicense(deviceType,
                languageCode);
    }

    @Override
    public boolean addLicense(String type, License license) throws LicenseManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().addLicense(type, license);
    }

    @Override
    public boolean addOperation(Operation operation,
            List<DeviceIdentifier> devices) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().addOperation(operation, devices);
    }

    @Override
    public List<Operation> getOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getOperations(deviceId);
    }

    @Override
    public List<Operation> getPendingOperations(DeviceIdentifier deviceId) throws OperationManagementException {
        return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getPendingOperations(deviceId);
    }

    @Override
    public Operation getPendingOperation(DeviceIdentifier deviceId) throws OperationManagementException {
        return null;
    }

    @Override
    public void sendEnrollInvitation(EmailMessageProperties emailMessageProperties) throws DeviceManagementException {
        DeviceManagementDataHolder.getInstance().getDeviceManagementProvider()
                .sendEnrollInvitation(emailMessageProperties);
    }

}
