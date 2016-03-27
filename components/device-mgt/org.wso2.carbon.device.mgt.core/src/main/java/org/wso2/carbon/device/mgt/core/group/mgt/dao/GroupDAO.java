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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;

import java.util.List;
import java.util.Set;

/**
 * This interface represents the key operations associated with persisting group related information.
 */
public interface GroupDAO {
    /**
     * Add new Device Group
     *
     * @param deviceGroup to be added
     * @param tenantId    of the group
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException;

    /**
     * Update an existing Device Group
     *
     * @param deviceGroup group to update
     * @throws GroupManagementDAOException
     */
    void updateGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException;

    /**
     * Delete an existing Device Group
     *
     * @param groupName group Id to delete
     * @throws GroupManagementDAOException
     */
    void deleteGroup(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Get device group by Id
     *
     * @param groupName id of Device Group
     * @return Device Group
     * @throws GroupManagementDAOException
     */
    DeviceGroupBuilder getGroup(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param tenantId of user's tenant
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroupBuilder> getGroups(int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Groups that matches with the given DeviceGroup name.
     *
     * @param groupName name of the Device Group.
     * @param tenantId  of user's tenant
     * @return List of DeviceGroup that matches with the given DeviceGroup name.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroupBuilder> getGroups(String groupName, int tenantId) throws GroupManagementDAOException;

    boolean isGroupExist(String groupName, int tenantId) throws GroupManagementDAOException;

    void addDeviceToGroup(String groupName, Device device, int tenantId) throws GroupManagementDAOException;

    void addDevicesToGroup(String groupName, Set<Device> devices, int tenantId) throws GroupManagementDAOException;

    void removeDeviceFromGroup(String groupName, DeviceIdentifier id,
                               int tenantId) throws GroupManagementDAOException;

    boolean isDeviceMappedToGroup(String groupName, DeviceIdentifier id,
                                  int tenantId) throws GroupManagementDAOException;

    int getDeviceCount(String groupName, int tenantId) throws GroupManagementDAOException;

}
