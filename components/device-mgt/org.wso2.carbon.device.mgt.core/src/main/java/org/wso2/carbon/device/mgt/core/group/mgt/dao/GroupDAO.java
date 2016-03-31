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

package org.wso2.carbon.device.mgt.core.group.mgt.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;

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
     * @param deviceGroup   group to update.
     * @param oldGroupName  of the group.
     * @param tenantId      of the group.
     * @throws GroupManagementDAOException
     */
    void updateGroup(DeviceGroup deviceGroup, String oldGroupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete an existing Device Group.
     *
     * @param groupName to be deleted.
     * @param owner     of the group.
     * @param tenantId  of the group.
     * @throws GroupManagementDAOException
     */
    void deleteGroup(String groupName, String owner, int tenantId) throws GroupManagementDAOException;

    /**
     * Get device group by id.
     *
     * @param groupId of Device Group.
     * @param tenantId  of the group.
     * @return Device Group in tenant with specified name.
     * @throws GroupManagementDAOException
     */
    DeviceGroupBuilder getGroup(int groupId, int tenantId) throws GroupManagementDAOException;


    /**
     * Get device group by name.
     *
     * @param groupName of Device Group.
     * @param owner of the group.
     * @param tenantId  of the group.
     * @return Device Group in tenant with specified name.
     * @throws GroupManagementDAOException
     */
    DeviceGroupBuilder getGroup(String groupName, String owner, int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroupBuilder> getGroups(int startIndex, int rowCount, int tenantId) throws GroupManagementDAOException;


    /**
     * Get count of Device Groups in tenant.
     *
     * @param tenantId of user's tenant.
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    int getGroupCount(int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Groups that matches with the given DeviceGroup name.
     *
     * @param groupName of the Device Group.
     * @param tenantId  of user's tenant.
     * @return List of DeviceGroup that matches with the given DeviceGroup name.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroupBuilder> findInGroups(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Check group already existed with given name.
     *
     * @param groupName of the Device Group.
     * @param owner of the Device Group.
     * @param tenantId of user's tenant.
     * @return existence of group with name
     * @throws GroupManagementDAOException
     */
    boolean isGroupExist(String groupName, String owner, int tenantId) throws GroupManagementDAOException;

    /**
     * Add device to a given Device Group.
     *
     * @param groupName of the Device Group.
     * @param owner of the Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void addDevice(String groupName, String owner, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Remove device from the Device Group.
     *
     * @param groupName of the Device Group.
     * @param owner of the Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    void removeDevice(String groupName, String owner, int deviceId, int tenantId) throws GroupManagementDAOException;

    /**
     * Check device is belonging to a Device Group.
     *
     * @param groupName of the Device Group.
     * @param owner of the Device Group.
     * @param deviceId of the device.
     * @param tenantId of user's tenant.
     * @throws GroupManagementDAOException
     */
    boolean isDeviceMappedToGroup(String groupName, String owner, int deviceId, int tenantId)
            throws GroupManagementDAOException;

    /**
     * Get count of devices in a Device Group.
     *
     * @param groupName of the Device Group.
     * @param owner of the Device Group.
     * @param tenantId of user's tenant.
     * @return device count.
     * @throws GroupManagementDAOException
     */
    int getDeviceCount(String groupName, String owner, int tenantId) throws GroupManagementDAOException;

    /**
     * Get all devices of a given tenant and device group.
     *
     * @param groupName of the group.
     * @param owner of the Device Group.
     * @param tenantId of user's tenant.
     * @return list of device in group
     * @throws GroupManagementDAOException
     */
    List<Device> getDevices(String groupName, String owner, int tenantId) throws GroupManagementDAOException;

    /**
     * Get paginated result of devices of a given tenant and device group.
     *
     * @param groupName of the group.
     * @param owner of the Device Group.
     * @param startIndex for pagination.
     * @param rowCount for pagination.
     * @param tenantId of user's tenant.
     * @return list of device in group
     * @throws GroupManagementDAOException
     */
    List<Device> getDevices(String groupName, String owner, int startIndex, int rowCount, int tenantId)
            throws GroupManagementDAOException;

}
