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

import org.wso2.carbon.device.mgt.group.common.Group;

import java.util.List;

/**
 * This class represents the key operations associated with persisting group related information.
 */
public interface GroupDAO {

    /**
     * Add new group
     *
     * @param group new group
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int addGroup(Group group) throws GroupManagementDAOException;

    /**
     * Update an existing group
     *
     * @param group updated group
     * @return sql execution result
     * @throws GroupManagementDAOException
     */
    int updateGroup(Group group) throws GroupManagementDAOException;

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
     * @return Group
     * @throws GroupManagementDAOException
     */
    Group getGroupById(int groupId) throws GroupManagementDAOException;

    /**
     * Get the list of Groups belongs to a user.
     *
     * @return List of all Groups.
     * @throws GroupManagementDAOException
     */
    List<Group> getAllGroups() throws GroupManagementDAOException;

    /**
     * Get the list of Groups that matches with the given Group name.
     *
     * @param groupName of the group.
     * @param owner  of the group.
     * @param tenantId  of user's tenant
     * @return List of Groups that matches with the given Group name.
     * @throws GroupManagementDAOException
     */
    List<Group> getGroupsByName(String groupName, String owner, int tenantId) throws GroupManagementDAOException;
}
