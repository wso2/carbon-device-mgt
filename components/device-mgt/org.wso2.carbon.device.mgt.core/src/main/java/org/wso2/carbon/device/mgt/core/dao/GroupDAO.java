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

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;

import java.util.List;

/**
 * This interface represents the key operations associated with persisting group related information.
 */
public interface GroupDAO {
    /**
     * Add new Device Group.
     *
     * @param deviceGroup to be added.
     * @param tenantId    of the group.
     * @return sql execution result.
     * @throws GroupManagementDAOException
     */
    int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException;

    /**
     * Update an existing Device Group.
     *
     * @param deviceGroup group to update.
     * @param groupId of Device Group.
     * @param tenantId of the group.
     * @throws GroupManagementDAOException
     */
    void updateGroup(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Delete an existing Device Group.
     *
     * @param groupId of Device Group.
     * @param tenantId  of the group.
     * @throws GroupManagementDAOException
     */
    void deleteGroup(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get device group by id.
     *
     * @param groupId of Device Group.
     * @param tenantId  of the group.
     * @return Device Group in tenant with specified name.
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroup(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get the groups of device with device id provided
     * @param deviceId
     * @return groups which has the device.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated list of Device Groups in tenant.
     *
     * @param paginationRequest to filter results.
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(GroupPaginationRequest paginationRequest, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated list of Device Groups in tenant with specified device group ids.
     *
     * @param paginationRequest to filter results.
     * @param deviceGroupIds    of groups required.
     * @param tenantId          of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(GroupPaginationRequest paginationRequest, List<Integer> deviceGroupIds,
                                int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    int getGroupCount(int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated count of Device Groups in tenant.
     *
     * @param paginationRequest to filter results.
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    int getGroupCount(GroupPaginationRequest paginationRequest, int tenantId) throws GroupManagementDAOException;

    /**
     * Check group already existed with given name.
     *
     * @param groupName of the Device Group.
     * @param tenantId of user's tenant.
     * @return existence of group with name
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroup(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Add device to a given Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void addDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Remove device from the Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void removeDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Check device is belonging to a Device Group.
     *
     * @param groupId of Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    boolean isDeviceMappedToGroup(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Get count of devices in a Device Group.
     *
     * @param groupId of Device Group.
     * @param tenantId of user's tenant.
     * @return device count.
     * @throws GroupManagementDAOException
     */
    int getDeviceCount(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated result of devices of a given tenant and device group.
     *
     * @param groupId of Device Group.
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @param tenantId of user's tenant.
     * @return list of device in group
     * @throws GroupManagementDAOException
     */
    List<Device> getDevices(int groupId, int startIndex, int rowCount, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Get all user roles for device group.
     *
     * @param groupId  of the group
     * @param tenantId of user's tenant.
     * @return list of roles
     * @throws GroupManagementDAOException
     */
    List<String> getRoles(int groupId, int tenantId) throws GroupManagementDAOException;

    /**
     * Add user role for device group.
     *
     * @param groupId  of the group.
     * @param role     to be added.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void addRole(int groupId, String role, int tenantId) throws GroupManagementDAOException;

    /**
     * Add user role for device group.
     *
     * @param groupId  of the group.
     * @param role     to be removed.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void removeRole(int groupId, String role, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device groups which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @return list of device groups.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(String[] roles, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device group ids which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @return list of device group ids.
     * @throws GroupManagementDAOException
     */
    List<Integer> getGroupIds(String[] roles, int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of all device groups which shared with a user role.
     *
     * @param roles    of the group.
     * @param tenantId of user's tenant.
     * @return count of device groups.
     * @throws GroupManagementDAOException
     */
    int getGroupsCount(String[] roles, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device groups which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @return list of device groups.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getOwnGroups(String username, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all device group ids which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @return list of device group ids.
     * @throws GroupManagementDAOException
     */
    List<Integer> getOwnGroupIds(String username, int tenantId) throws GroupManagementDAOException;

    /**
     * Get count of device groups which owned by user.
     *
     * @param username of the owner.
     * @param tenantId of user's tenant.
     * @return count of device groups.
     * @throws GroupManagementDAOException
     */
    int getOwnGroupsCount(String username, int tenantId) throws GroupManagementDAOException;

}
