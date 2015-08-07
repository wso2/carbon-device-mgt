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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.group.common.Group;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.core.dao.GroupDAO;
import org.wso2.carbon.device.mgt.group.core.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.group.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.group.core.internal.DeviceMgtGroupDataHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.Permission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManagementServiceProviderImpl implements GroupManagementServiceProvider {

    private static Log log = LogFactory.getLog(GroupManagementServiceProviderImpl.class);

    private GroupDAO groupDAO;

    public GroupManagementServiceProviderImpl() {
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
    }

    @Override
    public boolean createGroup(Group group) throws GroupManagementException {
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            group.setTenantId(tenantId);
            this.groupDAO.addGroup(group);
            int groupId = this.groupDAO.getGroupByName(group.getName(), group.getTenantId()).getId();
            if (groupId == 0) {
                return false;
            }
            group.setId(groupId);
            addNewSharingRoleForGroup(group.getOwnerId(), group.getId(), "admin", null);
            addNewSharingRoleForGroup(group.getOwnerId(), group.getId(), "monitor", null);
            addNewSharingRoleForGroup(group.getOwnerId(), group.getId(), "operator", null);
            log.info("Group added: " + group.getName());
            return true;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while adding group " +
                    "'" + group.getName() + "'", e);
        } catch (GroupManagementException e) {
            throw new GroupManagementException("Error occurred while adding group " +
                    "'" + group.getName() + "' role to user " + group.getOwnerId(), e);
        }
    }

    @Override
    public void updateGroup(Group group) throws GroupManagementException {
        try {
            this.groupDAO.updateGroup(group);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while modifying group " +
                    "'" + group.getName() + "'", e);
        }
    }

    @Override
    public boolean deleteGroup(int groupId) throws GroupManagementException {
        String roleName;
        try {
            Group group = getGroupById(groupId);
            if (group == null) {
                return false;
            }
            List<String> groupRoles = getAllRolesForGroup(groupId);
            for (String role : groupRoles) {
                if (role != null) {
                    roleName = role.replace("Internal/groups/" + groupId + "/", "");
                    removeSharingRoleForGroup(groupId, roleName);
                }
            }
            List<Device> groupDevices = getAllDevicesInGroup(groupId);
            for (Device device : groupDevices){
                device.setGroupId(0);
                DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
            }
            this.groupDAO.deleteGroup(groupId);
            log.info("Group removed: " + group.getName());
            return true;
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while removing device from group", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while removing group " +
                    "'" + groupId + "' data", e);
        } catch (GroupManagementException e) {
            throw new GroupManagementException("Error occurred while removing group " +
                    "'" + groupId + "' roles", e);
        }
    }

    @Override
    public Group getGroupById(int groupId) throws GroupManagementException {
        try {
            Group group = this.groupDAO.getGroupById(groupId);
            if (group != null) {
                group.setDeviceCount(getDeviceCountInGroup(group.getId()));
                group.setUsers(getUsersForGroup(group.getId()));
            }
            return group;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupId, e);
        }
    }

    @Override
    public Group getGroupByName(String groupName) throws GroupManagementException {
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            Group group = this.groupDAO.getGroupByName(groupName, tenantId);
            if (group != null) {
                group.setDeviceCount(getDeviceCountInGroup(group.getId()));
                group.setUsers(getUsersForGroup(group.getId()));
            }
            return group;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupName, e);
        }
    }

    @Override
    public List<Group> getGroupListOfUser(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            Map<Integer, Group> groups = new HashMap<>();
            for (String role : roleList) {
                if (role != null && role.contains("Internal/groups/")) {
                    int groupId = Integer.parseInt(role.split("/")[2]);
                    if (!groups.containsKey(groupId)) {
                        Group group = getGroupById(groupId);
                        groups.put(groupId, group);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager", e);
        }
    }

    @Override
    public int getGroupCountOfUser(String username) throws GroupManagementException {
        return this.getGroupListOfUser(username).size();
    }

    @Override
    public boolean shareGroup(String username, int groupId, String sharingRole) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles = new String[1];
        try {
            Group group = getGroupById(groupId);
            if (group == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles[0] = "Internal/groups/" + groupId + "/" + sharingRole;
            userStoreManager.updateRoleListOfUser(username, null, roles);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding user " + username + " to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public boolean unShareGroup(String username, int groupId, String sharingRole) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles = new String[1];
        try {
            Group group = getGroupById(groupId);
            if (group == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles[0] = "Internal/groups/" + groupId + "/" + sharingRole;
            userStoreManager.updateRoleListOfUser(username, roles, null);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding user " + username + " to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public boolean addNewSharingRoleForGroup(String username, int groupId, String roleName, Permission[] permissions) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        String[] userNames = new String[1];
        try {
            Group group = getGroupById(groupId);
            if (group == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal//groups/" + groupId + "/" + roleName;
            userNames[0] = username;
            userStoreManager.addRole(role, userNames, permissions);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public boolean removeSharingRoleForGroup(int groupId, String roleName) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        try {
            Group group = getGroupById(groupId);
            if (group == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/groups/" + groupId + "/" + roleName;
            userStoreManager.deleteRole(role);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public List<String> getAllRolesForGroup(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles;
        List<String> groupRoles;
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles = userStoreManager.getRoleNames();
            groupRoles = new ArrayList<>();
            for (String r : roles) {
                if (r != null && r.contains("Internal/groups/" + groupId + "/")) {
                    groupRoles.add(r.replace("Internal/groups/" + groupId + "/", ""));
                }
            }
            return groupRoles;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public List<String> getGroupRolesForUser(String username, int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        List<String> groupRoleList = new ArrayList<>();
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            for (String role : roleList) {
                if (role != null && role.contains("Internal/groups/" + groupId)) {
                    String roleName = role.replace("Internal/groups/" + groupId + "/", "");
                    groupRoleList.add(roleName);
                }
            }
            return groupRoleList;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager", e);
        }
    }

    @Override
    public List<String> getUsersForGroup(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        List<String> userNames;
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userNames = new ArrayList<>();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getAllRolesForGroup(groupId);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/groups/" + groupId + "/" + role);
                for (String user : users) {
                    if (!userNames.contains(user)) {
                        userNames.add(user);
                    }
                }
            }
            return userNames;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in fetching user list for group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public List<Device> getAllDevicesInGroup(int groupId) throws GroupManagementException {
        List<Device> devicesInGroup;
        try {
            devicesInGroup = DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().getDevicesOfGroup(groupId);
            return devicesInGroup;
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while getting devices in group", e);
        }
    }

    @Override
    public boolean addDeviceToGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException {
        Device device;
        Group group;
        try {
            device = DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().getDevice(deviceId);
            group = this.getGroupById(groupId);
            if (device == null || group == null) {
                return false;
            }
            device.setGroupId(group.getId());
            DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while adding device in to group", e);
        }
        return true;
    }

    @Override
    public boolean removeDeviceFromGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException {
        Device device;
        Group group;
        try {
            device = DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().getDevice(deviceId);
            group = this.getGroupById(groupId);
            if (device == null || group == null) {
                return false;
            }
            device.setGroupId(0);
            DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while removing device from group", e);
        }
        return true;
    }

    private int getDeviceCountInGroup(int groupId) throws GroupManagementException {
        return getAllDevicesInGroup(groupId).size();
    }
}
