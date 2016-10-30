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

import java.util.List;


/**
 * This represents the contract of DeviceAccessAuthorization service which will be used to check the authorization when
 * accessing the device information and performing MDM operations on devices.
 */
public interface DeviceAccessAuthorizationService {
    /**
     * This method will check whether the currently logged-in user has the access to the device identified by the given
     * DeviceIdentifier.
     *
     * @param deviceIdentifier - DeviceIdentifier of the device to be checked.
     * @return Boolean authorization result.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    boolean isUserAuthorized(DeviceIdentifier deviceIdentifier) throws DeviceAccessAuthorizationException;

    /**
     * This method will check whether the currently logged-in user has the access to the device identified by the given
     * DeviceIdentifier.
     *
     * @param deviceIdentifier - DeviceIdentifier of the device to be checked.
     * @param groupPermissions - Group Permissions.
     * @return Boolean authorization result.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String[] groupPermissions)
            throws DeviceAccessAuthorizationException;

    /**
     * This method will check whether the currently logged-in user has the access to the devices identified by the given
     * DeviceIdentifier list.
     *
     * @param deviceIdentifiers - List of DeviceIdentifiers to be checked for authorization.
     * @return DeviceAuthorizationResult - Authorization result including the list of authorized devices & unauthorized
     * devices.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers) throws
                                                                                         DeviceAccessAuthorizationException;

    /**
     * This method will check whether the currently logged-in user has the access to the devices identified by the given
     * DeviceIdentifier list.
     *
     * @param deviceIdentifiers - List of DeviceIdentifiers to be checked for authorization.
     * @param groupPermissions  - Group Permissions
     * @return DeviceAuthorizationResult - Authorization result including the list of authorized devices & unauthorized
     * devices.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String[] groupPermissions)
            throws DeviceAccessAuthorizationException;

    /**
     * This method will check whether the given user has the access to the device identified by the given
     * DeviceIdentifier.
     *
     * @param deviceIdentifier - DeviceIdentifier of the device to be checked.
     * @param username         - Username of the user to be checked for authorization.
     * @param groupPermissions - Group Permissions
     * @return Boolean authorization result.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String username, String[] groupPermissions)
            throws DeviceAccessAuthorizationException;

    /**
     * This method will check whether the given user has the access to the devices identified by the given
     * DeviceIdentifier list.
     *
     * @param deviceIdentifiers - List of DeviceIdentifiers to be checked for authorization.
     * @param username          - User name
     * @param groupPermissions  - Group Permissions
     * @return DeviceAuthorizationResult - Authorization result including the list of authorized devices & unauthorized
     * devices.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String username,
                                               String[] groupPermissions) throws
                                                                          DeviceAccessAuthorizationException;

    /**
     * This method will check whether the given user has the access to the device identified by the given
     * DeviceIdentifier.
     *
     * @param deviceIdentifier - DeviceIdentifier of the device to be checked.
     * @param username         - Username of the user to be checked for authorization.
     * @return Boolean authorization result.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    boolean isUserAuthorized(DeviceIdentifier deviceIdentifier, String username) throws
                                                                                 DeviceAccessAuthorizationException;

    /**
     * This method will check whether the authenticated user has the admin permissions.
     *
     * @return Boolean authorization result.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    boolean isDeviceAdminUser() throws DeviceAccessAuthorizationException;

    /**
     * This method will check whether the given user has the access to the devices identified by the given
     * DeviceIdentifier list.
     *
     * @param deviceIdentifiers - List of DeviceIdentifiers to be checked for authorization.
     * @param username          - Username of the user to be checked for authorization.
     * @return DeviceAuthorizationResult - Authorization result including the list of authorized devices & unauthorized
     * devices.
     * @throws DeviceAccessAuthorizationException if something goes wrong when checking the authorization.
     */
    DeviceAuthorizationResult isUserAuthorized(List<DeviceIdentifier> deviceIdentifiers, String username) throws
                                                                                                          DeviceAccessAuthorizationException;
}