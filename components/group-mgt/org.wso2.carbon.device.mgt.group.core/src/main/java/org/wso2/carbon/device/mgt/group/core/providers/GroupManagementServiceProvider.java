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
import org.wso2.carbon.user.api.Permission;

import java.util.List;

public interface GroupManagementServiceProvider {

    /**
     * @param group to add
     * @param defaultRole of the group
     * @param defaultPermissions of the default role
     * @return is group created
     * @throws GroupManagementException
     */
    boolean createGroup(Group group, String defaultRole, String[] defaultPermissions) throws GroupManagementException;

    /**
     * @param group to update
     * @throws GroupManagementException
     * @return is group updated
     */
    boolean updateGroup(Group group) throws GroupManagementException;

    /**
     * @param groupId of the group to delete
     * @throws GroupManagementException
     * @return is group deleted
     */
    boolean deleteGroup(int groupId) throws GroupManagementException;

    /**
     * @param groupId of the group of the group
     * @return group
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
     * @param username of the user
     * @return list of groups
     * @throws GroupManagementException
     */
    List<Group> getGroupsOfUser(String username) throws GroupManagementException;

    /**
     * @param username of the user
     * @return group count
     * @throws GroupManagementException
     */
    int getGroupCountOfUser(String username) throws GroupManagementException;

    /**
     * @param username of the user
     * @param groupId of the group of the group
     * @param sharingRole to be shared
     * @return is group shared
     * @throws GroupManagementException
     */
    boolean shareGroup(String username, int groupId, String sharingRole) throws GroupManagementException;

    /**
     * @param userName of the user
     * @param groupId of the group of the group
     * @param sharingRole to be un shared
     * @return is group un shared
     * @throws GroupManagementException
     */
    boolean unShareGroup(String userName, int groupId, String sharingRole) throws GroupManagementException;

    /**
     * @param userName of the user
     * @param groupId of the group of the group
     * @param roleName to add
     * @param permissions to bind with role
     * @return
     * @throws GroupManagementException
     */
    boolean addNewSharingRoleForGroup(String userName, int groupId, String roleName, String[] permissions) throws GroupManagementException;

    /**
     * @param groupId of the group of the group
     * @param roleName to remove
     * @return is role removed
     * @throws GroupManagementException
     */
    boolean removeSharingRoleForGroup(int groupId, String roleName) throws GroupManagementException;

    /**
     * @param groupId of the group of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getAllRolesForGroup(int groupId) throws GroupManagementException;

    /**
     * @param userName of the user
     * @param groupId of the group of the group
     * @return list of roles
     * @throws GroupManagementException
     */
    List<String> getGroupRolesForUser(String userName, int groupId) throws GroupManagementException;

    /**
     * @param groupId of the group
     * @return list of group users
     * @throws GroupManagementException
     */
    List<GroupUser> getUsersForGroup(int groupId) throws GroupManagementException;

    /**
     * @param groupId of the group
     * @return list of group devices
     * @throws GroupManagementException
     */
    List<Device> getAllDevicesInGroup(int groupId) throws GroupManagementException;

    /**
     * @param deviceId of the device
     * @param groupId of the group
     * @return is device added
     * @throws GroupManagementException
     */
    boolean addDeviceToGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;

    /**
     * @param deviceId of the device
     * @param groupId of the group
     * @return is device removed
     * @throws GroupManagementException
     */
    boolean removeDeviceFromGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException;

    /**
     * @param username of the user
     * @param groupId of the group
     * @return array of permissions
     * @throws GroupManagementException
     */
    String[] getGroupPermissionsOfUser(String username, int groupId) throws GroupManagementException;

    /**
     * @param username of the user
     * @param permission to filter
     * @return group list with specified permissions
     * @throws GroupManagementException
     */
    List<Group> getUserGroupsForPermission(String username, String permission) throws GroupManagementException;
}
