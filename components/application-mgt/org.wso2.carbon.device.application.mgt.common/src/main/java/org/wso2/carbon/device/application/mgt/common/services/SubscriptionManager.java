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
package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;

import java.util.List;

/**
 * This interface manages all the operations related with Application Subscription.
 */
public interface SubscriptionManager {
    /**
     * To install an application to given list of devices.
     * @param applicationUUID Application ID
     * @param deviceList Device list
     * @return Failed Device List which the application was unable to install
     * @throws ApplicationManagementException Application Management Exception
     */
    List<DeviceIdentifier> installApplicationForDevices(String applicationUUID, String versionName,
                                                        List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException;

    /**
     * To install an application to given list of users.
     * @param applicationUUID Application ID
     * @param userList User list
     * @return Failed Device List which the application was unable to install
     * @throws ApplicationManagementException Application Management Exception
     */
    List<DeviceIdentifier> installApplicationForUsers(String applicationUUID,
                                            List<String> userList)
            throws ApplicationManagementException;

    /**
     * To install an application to given list of users.
     * @param applicationUUID Application ID
     * @param roleList Role list
     * @return Failed Device List which the application was unable to install
     * @throws ApplicationManagementException Application Management Exception
     */
    List<DeviceIdentifier> installApplicationForRoles(String applicationUUID,
                                            List<String> roleList)
            throws ApplicationManagementException;

    /**
     * To uninstall an application from a given list of devices.
     * @param applicationUUID Application ID
     * @param deviceList Device list
     * @return Failed Device List which the application was unable to uninstall
     * @throws ApplicationManagementException Application Management Exception
     */
    List<DeviceIdentifier> uninstallApplication(String applicationUUID,
                                                List<DeviceIdentifier> deviceList)
            throws ApplicationManagementException;

}
