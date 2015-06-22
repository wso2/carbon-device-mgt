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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import java.util.List;

/**
 * Proxy class for all Device Management related operations that take the corresponding plugin type in
 * and resolve the appropriate plugin implementation
 */
public interface DeviceManagementProviderService extends DeviceManager, LicenseManager, OperationManager {

    List<Device> getAllDevices(String type) throws DeviceManagementException;

    List<Device> getAllDevices() throws DeviceManagementException;

    void sendEnrolmentInvitation(EmailMessageProperties config) throws DeviceManagementException;

    void sendRegistrationEmail(EmailMessageProperties config) throws DeviceManagementException;

    FeatureManager getFeatureManager(String type) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by an user.
     *
     * @param userName          Username of the user
     * @return List of devices owned by a particular user
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getDevicesOfUser(String userName) throws DeviceManagementException;

    /**
     * Method to get the list of devices owned by users of a particular user-role.
     *
     * @param roleName          Role name of the users
     * @return List of devices owned by users of a particular role
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getAllDevicesOfRole(String roleName) throws DeviceManagementException;

    /**
     * Method to get the count of all types of devices.
     * @return device count
     * @throws DeviceManagementException If some unusual behaviour is observed while counting
     * the devices
     */
    int getDeviceCount() throws DeviceManagementException;

    /**
     * Method to get the list of devices that matches with the given device name.
     *
     * @param deviceName    name of the device
     * @return List of devices that matches with the given device name.
     * @throws DeviceManagementException If some unusual behaviour is observed while fetching the
     * device list
     */
    List<Device> getDevicesByName(String deviceName) throws DeviceManagementException;

    /**
     * The method to get application list installed for the device.
     *
     * @param deviceIdentifier
     * @return
     * @throws DeviceManagementException
     */
    List<Application> getApplicationListForDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementException;

}
