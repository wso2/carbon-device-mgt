/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.common.app.mgt;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;

import java.util.List;

/**
 * This will handle the Application management side of MDM by acting a bridge between
 * MDM and App manager product.
 */
public interface ApplicationManager {

    /**
     * This will communicate with App manager and retrieve the list of apps in the store, when
     * the domain is given. The list is broken down into pages and retrieved.
     *
     * @param domain     Tenant domain of the app list to be retrieved.
     * @param pageNumber Page number of the list.
     * @param size       Number of items in one page.
     * @return The list of applications belongs to a domain.
     * @throws ApplicationManagementException If something goes wrong
     */

    Application[] getApplications(String domain, int pageNumber, int size)
            throws ApplicationManagementException;


    /**
     * Updates the application, install/uninstall status of the a certain application, on a device.
     *
     * @param deviceId    Device id of the device that the status belongs to.
     * @param application Application details of the app being updated.
     * @param status      Installed/Uninstalled
     * @throws ApplicationManagementException If something goes wrong
     */
    void updateApplicationStatus(DeviceIdentifier deviceId, Application application, String status)
            throws ApplicationManagementException;

    /**
     * Retrieve the status of an application on a device. Whether it is installed or not.
     *
     * @param deviceId    Device id of the device that the status belongs to.
     * @param application Application details of the app being searched.
     * @return Status of the application on the device.
     * @throws ApplicationManagementException If something goes wrong
     */
    String getApplicationStatus(DeviceIdentifier deviceId, Application application)
            throws ApplicationManagementException;


    Activity installApplicationForDevices(Operation operation, List<DeviceIdentifier> deviceIdentifiers)
            throws ApplicationManagementException;

    Activity installApplicationForUsers(Operation operation, List<String> userNameList)
            throws ApplicationManagementException;

    Activity installApplicationForUserRoles(Operation operation, List<String> userRoleList)
            throws ApplicationManagementException;
}
