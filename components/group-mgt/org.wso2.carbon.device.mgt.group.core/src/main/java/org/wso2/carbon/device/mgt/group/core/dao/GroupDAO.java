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
     * Add new deviceGroup
     *
     * @param deviceGroup to be added
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int addGroup(DeviceGroup deviceGroup) throws GroupManagementDAOException;

    /**
     * Update an existing deviceGroup
     *
     * @param deviceGroup to update
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int updateGroup(DeviceGroup deviceGroup) throws GroupManagementDAOException;

    /**
     * Delete an existing group
     *
     * @param groupId group Id to delete
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int deleteGroup(int groupId) throws GroupManagementDAOException;

    /**
     * Get group by Id
     *
     * @param groupId id of required group
     * @return DeviceGroup
     * @throws GroupManagementDAOException
     */
    DeviceGroup getGroupById(int groupId) throws GroupManagementDAOException;

    /**
     * Get the list of DeviceGroups belongs to a user.
     *
     * @return List of all DeviceGroups.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getAllGroups() throws GroupManagementDAOException;

    /**
     * Get the list of Groups that matches with the given DeviceGroup name.
     *
     * @param groupName of the DeviceGroup.
     * @param owner  of the DeviceGroup.
     * @param tenantId  of user's tenant
     * @return List of DeviceGroup that matches with the given DeviceGroup name.
     * @throws GroupManagementDAOException
     */
    List<DeviceGroup> getGroupsByName(String groupName, String owner, int tenantId) throws GroupManagementDAOException;
}
