/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.device.mgt.group.core.providers;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.group.common.Group;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.user.core.Permission;

import java.util.List;

public interface GroupManagementServiceProvider {
    /**
     * @param group to add
     * @return group added or not
     * @throws GroupManagementException
     */
    boolean createGroup(Group group) throws GroupManagementException;

    /**
     * @param group to update
     * @throws GroupManagementException
     */
    boolean updateGroup(Group group) throws GroupManagementException;

    /**
     * @param groupId to delete
     * @return group deleted or not
     * @throws GroupManagementException
     */
    boolean deleteGroup(int groupId) throws GroupManagementException;

    /**
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    Group getGroupById(int groupId) throws GroupManagementException;

    /**
     * @param groupName of the group.
     * @param owner  of the group.
     * @return List of Groups that matches with the given Group name.
     * @throws GroupManagementException
     */
    List<Group> getGroupByName(String groupName, String owner) throws GroupManagementException;

    /**
     * @param username
     * @return
     * @throws GroupManagementException
     */
    List<Group> getGroupsOfUser(String username) throws GroupManagementException;

    /**
     * @param username
     * @return
     * @throws GroupManagementException
     */
    int getGroupCountOfUser(String username) throws GroupManagementException;

    /**
     * @param username
     * @param groupId
     * @param sharingRole
     * @return
     * @throws GroupManagementException
     */
    boolean shareGroup(String username, int groupId, String sharingRole) throws GroupManagementException;

    /**
     * @param userName
     * @param groupId
     * @param sharingRole
     * @return
     * @throws GroupManagementException
     */
    boolean unShareGroup(String userName, int groupId, String sharingRole) throws GroupManagementException;

    /**
     * @param userName
     * @param groupId
     * @param roleName
     * @param permissions
     * @return
     * @throws GroupManagementException
     */
    boolean addNewSharingRoleForGroup(String userName, int groupId, String roleName, Permission[] permissions) throws GroupManagementException;

    /**
     * @param groupId
     * @param roleName
     * @return
     * @throws GroupManagementException
     */
    boolean removeSharingRoleForGroup(int groupId, String roleName) throws GroupManagementException;

    /**
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    List<String> getAllRolesForGroup(int groupId) throws GroupManagementException;

    /**
     * @param userName
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    List<String> getGroupRolesForUser(String userName, int groupId) throws GroupManagementException;

    /**
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    List<GroupUser> getUsersForGroup(int groupId) throws GroupManagementException;

    /**
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    List<Device> getAllDevicesInGroup(int groupId) throws GroupManagementException;

    /**
     * @param deviceId
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    boolean addDeviceToGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;

    /**
     * @param deviceId
     * @param groupId
     * @return
     * @throws GroupManagementException
     */
    boolean removeDeviceFromGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;
}
