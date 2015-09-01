/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.group.core.dao;

import org.wso2.carbon.device.mgt.group.common.DeviceGroup;

import java.util.List;

/**
 * This class represents the key operations associated with persisting group related information.
 */
public interface GroupDAO {

    /**
     * Add new Device Group
     *
     * @param deviceGroup to be added
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int addGroup(DeviceGroup deviceGroup) throws GroupManagementDAOException;

    /**
     * Update an existing Device Group
     *
     * @param deviceGroup group to update
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int updateGroup(DeviceGroup deviceGroup) throws GroupManagementDAOException;

    /**
     * Delete an existing Device Group
     *
     * @param groupId group Id to delete
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int deleteGroup(int groupId) throws GroupManagementDAOException;

    /**
     * Get device group by Id
     *
     * @param groupId id of Device Group
     * @return Device Group
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroup(int groupId) throws GroupManagementDAOException;

    /**
     * Get the list of Device Groups in tenant.
     *
     * @param tenantId of user's tenant
     * @return List of all Device Groups in tenant.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException;

    /**
     * Get the list of Groups that matches with the given DeviceGroup name.
     *
     * @param groupName name of the Device Group.
     * @param tenantId  of user's tenant
     * @return List of DeviceGroup that matches with the given DeviceGroup name.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroups(String groupName, int tenantId) throws GroupManagementDAOException;

    /**
     * Get last created group of user
     *
     * @param owner    of the Device Group
     * @param tenantId of the Device Group
     * @return last created group
     * @throws GroupManagementDAOException
     */
    DeviceGroup getLastCreatedGroup(String owner, int tenantId) throws GroupManagementDAOException;
}
