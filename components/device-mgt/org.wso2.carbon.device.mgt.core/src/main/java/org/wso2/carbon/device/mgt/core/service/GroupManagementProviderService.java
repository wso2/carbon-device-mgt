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
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyEixistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;

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
                     String[] defaultPermissions) throws GroupManagementException, GroupAlreadyEixistException;

    /**
     * Update existing device group.
     *
     * @param deviceGroup  to update.
     * @param oldGroupName of the group.
     * @param oldOwner     of the group.
     * @throws GroupManagementException
     */
    void updateGroup(DeviceGroup deviceGroup, String oldGroupName, String oldOwner) throws GroupManagementException;

    /**
     * Delete existing device group.
     *
     * @param groupName to be deleted.
     * @param owner     of the group.
     * @throws GroupManagementException
     */
    boolean deleteGroup(String groupName, String owner) throws GroupManagementException;

    /**
     * Get device group specified by group name.
     *
     * @param groupName of the group.
     * @param owner     of the group.
     * @return group
     * @throws GroupManagementException
     */
    DeviceGroup getGroup(String groupName, String owner) throws GroupManagementException;

    /**
     * Get list of device groups matched with %groupName%
     *
     * @param groupName of the groups.
     * @param username  of user
     * @return List of Groups that matches with the given DeviceGroup name.
     * @throws GroupManagementException
     */
    List<DeviceGroup> findInGroups(String groupName, String username) throws GroupManagementException;

    /**
     * Get paginated device groups in tenant
     *
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @return paginated list of groups
     * @throws GroupManagementException
     */
    PaginationResult getGroups(int startIndex, int rowCount) throws GroupManagementException;

    /**
     * Get all device group count in tenant
     *
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCount() throws GroupManagementException;

    /**
     * Get device groups of user
     *
     * @param username of the user
     * @return list of groups
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username) throws GroupManagementException;

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
     * @param groupName   of the group
     * @param owner       of the group
     * @param sharingRole to be shared
     * @return is group shared
     * @throws GroupManagementException
     */
    boolean shareGroup(String username, String groupName, String owner, String sharingRole)
            throws GroupManagementException;

    /**
     * Un share existing group sharing with user specified by role
     *
     * @param userName    of the user
     * @param groupName   of the group
     * @param owner       of the group
     * @param sharingRole to be un shared
     * @return is group un shared
     * @throws GroupManagementException
     */
    boolean unshareGroup(String userName, String groupName, String owner, String sharingRole)
            throws GroupManagementException;

    /**
     * Add new sharing role for device group
     *
     * @param userName    of the user
     * @param groupName   of the group
     * @param owner       of the group
     * @param roleName    to add
     * @param permissions to bind with role
     * @return is role added
     * @throws GroupManagementException
     */
    boolean addGroupSharingRole(String userName, String groupName, String owner, String roleName, String[] permissions)
            throws GroupManagementException;

    /**
     * Remove existing sharing role for device group
     *
     * @param groupName   of the group
     * @param owner       of the group
     * @param roleName    to remove
     * @return is role removed
     * @throws GroupManagementException
     */
    boolean removeGroupSharingRole(String groupName, String owner, String roleName) throws GroupManagementException;

    /**
     * Get all sharing roles for device group
     *
     * @param groupName   of the group
     * @param owner       of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getRoles(String groupName, String owner) throws GroupManagementException;

    /**
     * Get specific device group sharing roles for user
     *
     * @param userName    of the user
     * @param groupName   of the group
     * @param owner       of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getRoles(String userName, String groupName, String owner) throws GroupManagementException;

    /**
     * Get device group users
     *
     * @param groupName   of the group
     * @param owner       of the group
     * @return list of group users
     * @throws GroupManagementException
     */
    List<GroupUser> getUsers(String groupName, String owner) throws GroupManagementException;

    /**
     * Get all devices in device group.
     *
     * @param groupName   of the group.
     * @param owner       of the group.
     * @return list of group devices.
     * @throws GroupManagementException
     */
    List<Device> getDevices(String groupName, String owner) throws GroupManagementException;

    /**
     * Get all devices in device group as paginated result.
     *
     * @param groupName   of the group.
     * @param owner       of the group.
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @return Paginated list of devices.
     * @throws GroupManagementException
     */
    PaginationResult getDevices(String groupName, String owner, int startIndex, int rowCount)
            throws GroupManagementException;

    /**
     * This method is used to retrieve the device count of a given group.
     *
     * @param groupName   of the group.
     * @param owner       of the group.
     * @return returns the device count.
     * @throws GroupManagementException
     */
    int getDeviceCount(String groupName, String owner) throws GroupManagementException;

    /**
     * Add device to device group.
     *
     * @param deviceId of the device.
     * @param groupName   of the group.
     * @param owner       of the group.
     * @return is device added.
     * @throws GroupManagementException
     */
    boolean addDevice(DeviceIdentifier deviceId, String groupName, String owner) throws GroupManagementException;

    /**
     * Remove device from device group.
     *
     * @param deviceId of the device.
     * @param groupName   of the group.
     * @param owner       of the group.
     * @return is device removed.
     * @throws GroupManagementException
     */
    boolean removeDevice(DeviceIdentifier deviceId, String groupName, String owner) throws GroupManagementException;

    /**
     * Get device group permissions of user.
     *
     * @param username of the user.
     * @param groupName   of the group.
     * @param owner       of the group.
     * @return array of permissions.
     * @throws GroupManagementException
     */
    String[] getPermissions(String username, String groupName, String owner) throws GroupManagementException;

    /**
     * Get device groups of user with permission.
     *
     * @param username   of the user.
     * @param permission to filter.
     * @return group list with specified permissions.
     * @throws GroupManagementException
     */
    List<DeviceGroup> getGroups(String username, String permission) throws GroupManagementException;

}
