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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.Application;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

import java.util.List;

/**
 * This interface provides the list of operations that are supported with subscription database.
 *
 */
public interface SubscriptionDAO {

    /**
     * Adds a mapping between devices and the application which the application is installed on.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param deviceList List of {@link Device} which the application is installed on
     * @param appId id of the {@link Application} which installs
     * @param releaseId id of the {@link org.wso2.carbon.device.application.mgt.common.ApplicationRelease}
     * @throws ApplicationManagementDAOException If unable to add a mapping between device and application
     */
    void subscribeDeviceToApplication(int tenantId, String subscribedBy, List<Device> deviceList, int appId,
            int releaseId) throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between user and the application which the application is installed on. This mapping will be
     * added when an enterprise installation triggered to the user.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param userList list of user names of the users whose devices are subscribed to the application
     * @param appId id of the {@link Application} which installs
     * @param releaseId id of the {@link org.wso2.carbon.device.application.mgt.common.ApplicationRelease}
     * @throws ApplicationManagementDAOException If unable to add a mapping between device and application
     */
    void subscribeUserToApplication(int tenantId, String subscribedBy, List<String> userList, int appId, int releaseId)
            throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between user and the application which the application is installed on. This mapping will be
     * added when an enterprise installation triggered to the role.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param roleList list of roles which belongs devices are subscribed to the application
     * @param appId id of the {@link Application} which installs
     * @param releaseId id of the {@link org.wso2.carbon.device.application.mgt.common.ApplicationRelease}
     * @throws ApplicationManagementDAOException If unable to add a mapping between device and application
     */
    void subscribeRoleToApplication(int tenantId, String subscribedBy, List<String> roleList, int appId, int releaseId)
            throws ApplicationManagementDAOException;

    /**
     * Adds a mapping between user and the application which the application is installed on. This mapping will be
     * added when an enterprise installation triggered to the role.
     *
     * @param tenantId id of the tenant
     * @param subscribedBy username of the user who subscribe the application
     * @param groupList list of {@link DeviceGroup} which belongs the devices that are subscribed to the application
     * @param appId id of the {@link Application} which installs
     * @param releaseId id of the {@link org.wso2.carbon.device.application.mgt.common.ApplicationRelease}
     * @throws ApplicationManagementDAOException If unable to add a mapping between device and application
     */
    void subscribeGroupToApplication(int tenantId, String subscribedBy, List<DeviceGroup> groupList, int appId,
            int releaseId) throws ApplicationManagementDAOException;
}
