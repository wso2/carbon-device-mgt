/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.service;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.user.core.multiplecredentials.UserDoesNotExistException;

import java.util.List;

/**
 * Interface for Group Management Services
 */
public interface GroupManagementProviderService {

    /**
     * Add new device group and create default role with default permissions.
     *
     * @param deviceGroup        to add
     * @param defaultRole        of the deviceGroup
     * @param defaultPermissions of the default role
     * @throws GroupManagementException
     */
    void createGroup(DeviceGroup deviceGroup, String defaultRole,
                     String[] defaultPermissions) throws GroupManagementException, GroupAlreadyExistException;

    /**
     * Update existing device group.
     *
     * @param deviceGroup  to update.
     * @param groupId of the group.
     * @throws GroupManagementException
     */
    void updateGroup(DeviceGroup deviceGroup, int groupId) throws GroupManagementException;

    /**
     * Delete existing device group.
     *
     * @param groupId to be deleted.
     * @return status of the delete operation.
     * @throws GroupManagementException
     */
    boolean deleteGroup(int groupId) throws GroupManagementException;

    /**
     * Get the device group provided the device group id.
     *
     * @param groupId of the group.
     * @return group with details.
     * @throws GroupManagementException
     */
    DeviceGroup getGroup(int groupId) throws GroupManagementException;

    /**
     * Get paginated device groups in tenant.
     *
     * @param startIndex for pagination.
     * @param rowCount   for pagination.
     * @return list of groups.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(int startIndex, int rowCount) throws GroupManagementException;

    /**
     * Get paginated device groups for user.
     *
     * @param username   of the user.
     * @param startIndex for pagination.
     * @param rowCount   for pagination.
     * @return list of groups
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username, int startIndex, int rowCount) throws GroupManagementException;

    /**
     * Get all device group count in tenant
     *
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCount() throws GroupManagementException;

    /**
     * Get device group count of user
     *
     * @param username of the user
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCount(String username) throws GroupManagementException;

    /**
     * Share device group with user specified by role
     *
     * @param username    of the user
     * @param groupId   of the group
     * @param sharingRole to be shared
     * @return is group shared
     * @throws GroupManagementException UserDoesNotExistException
     */
    boolean shareGroup(String username, int groupId, String sharingRole)
            throws GroupManagementException, UserDoesNotExistException;

    /**
     * Un share existing group sharing with user specified by role
     *
     * @param userName    of the user
     * @param groupId   of the group
     * @param sharingRole to be un shared
     * @return is group un shared
     * @throws GroupManagementException UserDoesNotExistException
     */
    boolean unshareGroup(String userName, int groupId, String sharingRole)
            throws GroupManagementException, UserDoesNotExistException;

    /**
     * Add new sharing role for device group
     *
     * @param userName    of the user
     * @param groupId   of the group
     * @param roleName    to add
     * @param permissions to bind with role
     * @return is role added
     * @throws GroupManagementException
     */
    boolean addGroupSharingRole(String userName, int groupId, String roleName, String[] permissions)
            throws GroupManagementException;

    /**
     * Remove existing sharing role for device group
     *
     * @param groupId   of the group
     * @param roleName  to remove
     * @return is role removed
     * @throws GroupManagementException
     */
    boolean removeGroupSharingRole(int groupId, String roleName) throws GroupManagementException;

    /**
     * Get all sharing roles for device group
     *
     * @param groupId   of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getRoles(int groupId) throws GroupManagementException;

    /**
     * Get specific device group sharing roles for user
     *
     * @param userName  of the user
     * @param groupId   of the group
     * @return list of roles
     * @throws GroupManagementException UserDoesNotExistException
     */
    List<String> getRoles(String userName, int groupId) throws GroupManagementException, UserDoesNotExistException;

    /**
     * Get device group users
     *
     * @param groupId   of the group
     * @return list of group users
     * @throws GroupManagementException
     */
    List<GroupUser> getUsers(int groupId) throws GroupManagementException;

    /**
     * Get all devices in device group as paginated result.
     *
     * @param groupId   of the group
     * @param startIndex for pagination.
     * @param rowCount   for pagination.
     * @return list of devices in group.
     * @throws GroupManagementException
     */
    List<Device> getDevices(int groupId, int startIndex, int rowCount) throws GroupManagementException;

    /**
     * This method is used to retrieve the device count of a given group.
     *
     * @param groupId   of the group
     * @return returns the device count.
     * @throws GroupManagementException
     */
    int getDeviceCount(int groupId) throws GroupManagementException;

    /**
     * Add device to device group.
     *
     * @param deviceId  of the device.
     * @param groupId   of the group
     * @return is device added.
     * @throws GroupManagementException
     */
    boolean addDevice(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;

    /**
     * Remove device from device group.
     *
     * @param deviceId  of the device.
     * @param groupId   of the group
     * @return is device removed.
     * @throws GroupManagementException
     */
    boolean removeDevice(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;

    /**
     * Get device group permissions of user.
     *
     * @param username  of the user.
     * @param groupId   of the group
     * @return array of permissions.
     * @throws GroupManagementException UserDoesNotExistException
     */
    String[] getPermissions(String username, int groupId) throws GroupManagementException, UserDoesNotExistException;

    /**
     * Get device groups of user with permission.
     *
     * @param username   of the user.
     * @param permission to filter.
     * @return group list with specified permissions.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username, String permission) throws GroupManagementException;

    /**
     * Get the group of device.
     *
     * @param deviceIdentifier
     * @return
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(DeviceIdentifier deviceIdentifier) throws GroupManagementException;

}
