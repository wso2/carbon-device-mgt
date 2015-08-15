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
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.device.mgt.group.core.internal.GroupBroker;
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
        GroupBroker groupBroker = new GroupBroker(group);
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            groupBroker.setTenantId(tenantId);
            int sqlReturn = this.groupDAO.addGroup(groupBroker);
            if (sqlReturn == -1) {
                return false;
            }
            List<Group> groups = this.groupDAO.getGroupsByName(groupBroker.getName(), groupBroker.getOwner(), groupBroker.getTenantId());
            if (groups.size() == 0) {
                return false;
            }
            int groupId = groups.get(groups.size() - 1).getId();
            groupBroker.setId(groupId);
            addNewSharingRoleForGroup(groupBroker.getOwner(), groupBroker.getId(), "admin", null);
            addNewSharingRoleForGroup(groupBroker.getOwner(), groupBroker.getId(), "monitor", null);
            addNewSharingRoleForGroup(groupBroker.getOwner(), groupBroker.getId(), "operator", null);
            if (log.isDebugEnabled()) {
                log.debug("Group added: " + groupBroker.getName());
            }
            return true;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while adding group " +
                    "'" + group.getName() + "' to database", e);
        } catch (GroupManagementException e) {
            throw new GroupManagementException("Error occurred while adding group " +
                    "'" + group.getName() + "' role to user " + group.getOwner(), e);
        }
    }

    @Override
    public boolean updateGroup(Group group) throws GroupManagementException {
        try {
            int sqlReturn = this.groupDAO.updateGroup(group);
            return (sqlReturn != -1);
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
            for (Device device : groupDevices) {
                device.setGroupId(0);
                DeviceMgtGroupDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
            }
            int sqlReturn = this.groupDAO.deleteGroup(groupId);
            if (log.isDebugEnabled()) {
                log.debug("Group " + group.getName() + " removed: " + (sqlReturn != -1));
            }
            return (sqlReturn != -1);
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
                GroupBroker groupBroker = new GroupBroker(group);
                groupBroker.setDevices(this.getAllDevicesInGroup(groupId));
                groupBroker.setUsers(this.getUsersForGroup(groupId));
                groupBroker.setRoles(this.getAllRolesForGroup(groupId));
                return groupBroker.getGroup();
            } else {
                return null;
            }
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupId, e);
        }
    }

    @Override
    public List<Group> getGroupByName(String groupName, String owner) throws GroupManagementException {
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            List<Group> groups = this.groupDAO.getGroupsByName(groupName, owner, tenantId);
            List<Group> groupsWithData = new ArrayList<>();
            for (Group group : groups) {
                GroupBroker groupBroker = new GroupBroker(group);
                groupBroker.setDevices(this.getAllDevicesInGroup(group.getId()));
                groupBroker.setUsers(this.getUsersForGroup(group.getId()));
                groupBroker.setRoles(this.getAllRolesForGroup(group.getId()));
                groupsWithData.add(groupBroker.getGroup());
            }
            return groupsWithData;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupName, e);
        }
    }

    @Override
    public List<Group> getGroupsOfUser(String username) throws GroupManagementException {
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
        return this.getGroupsOfUser(username).size();
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
    public List<GroupUser> getUsersForGroup(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        Map<String, GroupUser> groupUserHashMap = new HashMap<>();
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = DeviceMgtGroupDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getAllRolesForGroup(groupId);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/groups/" + groupId + "/" + role);
                for (String user : users) {
                    GroupUser groupUser;
                    if (groupUserHashMap.containsKey(user)) {
                        groupUser = groupUserHashMap.get(user);
                        groupUser.getRoles().add(role);
                    } else {
                        groupUser = new GroupUser();
                        groupUser.setUsername(user);
                        groupUser.setRoles(new ArrayList<String>());
                        groupUser.getRoles().add(role);
                        groupUserHashMap.put(user, groupUser);
                    }
                }
            }
            return new ArrayList<>(groupUserHashMap.values());
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
