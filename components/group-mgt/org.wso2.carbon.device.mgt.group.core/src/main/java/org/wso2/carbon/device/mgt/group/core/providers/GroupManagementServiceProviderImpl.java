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
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.common.GroupManagementException;
import org.wso2.carbon.device.mgt.group.common.GroupUser;
import org.wso2.carbon.device.mgt.group.core.internal.DeviceGroupBroker;
import org.wso2.carbon.device.mgt.group.core.dao.GroupDAO;
import org.wso2.carbon.device.mgt.group.core.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.group.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.group.core.internal.GroupManagementDataHolder;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.util.UserCoreUtil;

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
    public int createGroup(DeviceGroup deviceGroup, String defaultRole, String[] defaultPermissions) throws GroupManagementException {
        DeviceGroupBroker groupBroker = new DeviceGroupBroker(deviceGroup);
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            groupBroker.setTenantId(tenantId);
            int sqlReturn = this.groupDAO.addGroup(groupBroker);
            if (sqlReturn == -1) {
                return -1;
            }
            List<DeviceGroup> deviceGroups = this.groupDAO.getGroupsByName(groupBroker.getName(), groupBroker.getOwner(), groupBroker.getTenantId());
            if (deviceGroups.size() == 0) {
                return -1;
            }
            int groupId = deviceGroups.get(deviceGroups.size() - 1).getId();
            groupBroker.setId(groupId);
            addNewSharingRoleForGroup(groupBroker.getOwner(), groupBroker.getId(), defaultRole, defaultPermissions);
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup added: " + groupBroker.getName());
            }
            return groupId;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while adding deviceGroup " +
                    "'" + deviceGroup.getName() + "' to database", e);
        } catch (GroupManagementException e) {
            throw new GroupManagementException("Error occurred while adding deviceGroup " +
                    "'" + deviceGroup.getName() + "' role to user " + deviceGroup.getOwner(), e);
        }
    }

    @Override
    public boolean updateGroup(DeviceGroup deviceGroup) throws GroupManagementException {
        try {
            int sqlReturn = this.groupDAO.updateGroup(deviceGroup);
            return (sqlReturn != -1);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while modifying deviceGroup " +
                    "'" + deviceGroup.getName() + "'", e);
        }
    }

    @Override
    public boolean deleteGroup(int groupId) throws GroupManagementException {
        String roleName;
        try {
            DeviceGroup deviceGroup = getGroupById(groupId);
            if (deviceGroup == null) {
                return false;
            }
            List<String> groupRoles = getAllRolesForGroup(groupId);
            for (String role : groupRoles) {
                if (role != null) {
                    roleName = role.replace("Internal/group-" + groupId + "-", "");
                    removeSharingRoleForGroup(groupId, roleName);
                }
            }
            List<Device> groupDevices = getAllDevicesInGroup(groupId);
            for (Device device : groupDevices) {
                device.setGroupId(0);
                GroupManagementDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
            }
            int sqlReturn = this.groupDAO.deleteGroup(groupId);
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup " + deviceGroup.getName() + " removed: " + (sqlReturn != -1));
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
    public DeviceGroup getGroupById(int groupId) throws GroupManagementException {
        try {
            DeviceGroup deviceGroup = this.groupDAO.getGroupById(groupId);
            if (deviceGroup != null) {
                DeviceGroupBroker groupBroker = new DeviceGroupBroker(deviceGroup);
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
    public List<DeviceGroup> getGroupByName(String groupName, String owner) throws GroupManagementException {
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            List<DeviceGroup> deviceGroups = this.groupDAO.getGroupsByName(groupName, owner, tenantId);
            List<DeviceGroup> groupsWithData = new ArrayList<>();
            for (DeviceGroup deviceGroup : deviceGroups) {
                DeviceGroupBroker groupBroker = new DeviceGroupBroker(deviceGroup);
                groupBroker.setDevices(this.getAllDevicesInGroup(deviceGroup.getId()));
                groupBroker.setUsers(this.getUsersForGroup(deviceGroup.getId()));
                groupBroker.setRoles(this.getAllRolesForGroup(deviceGroup.getId()));
                groupsWithData.add(groupBroker.getGroup());
            }
            return groupsWithData;
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupName, e);
        }
    }

    @Override
    public List<DeviceGroup> getGroupsOfUser(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        try {
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            Map<Integer, DeviceGroup> groups = new HashMap<>();
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-")) {
                    int groupId = Integer.parseInt(role.split("-")[1]);
                    if (!groups.containsKey(groupId)) {
                        DeviceGroup deviceGroup = getGroupById(groupId);
                        groups.put(groupId, deviceGroup);
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
            DeviceGroup deviceGroup = getGroupById(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles[0] = "Internal/group-" + groupId + "-" + sharingRole;
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
            DeviceGroup deviceGroup = getGroupById(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles[0] = "Internal/group-" + groupId + "-" + sharingRole;
            userStoreManager.updateRoleListOfUser(username, roles, null);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding user " + username + " to group id:" + groupId;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    @Override
    public boolean addNewSharingRoleForGroup(String username, int groupId, String roleName, String[] permissions) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        String[] userNames = new String[1];
        try {
            DeviceGroup deviceGroup = getGroupById(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupId + "-" + roleName;
            userNames[0] = username;
            Permission[] carbonPermissions = new Permission[permissions.length];
            for (int i = 0; i < permissions.length; i++){
                carbonPermissions[i] = new Permission(permissions[i], CarbonConstants.UI_PERMISSION_ACTION);
            }
            userStoreManager.addRole(role, userNames, carbonPermissions);
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
            DeviceGroup deviceGroup = getGroupById(groupId);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = DeviceManagerUtil.getTenantId();
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupId + "-" + roleName;
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
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles = userStoreManager.getRoleNames();
            groupRoles = new ArrayList<>();
            for (String r : roles) {
                if (r != null && r.contains("Internal/group-" + groupId + "-")) {
                    groupRoles.add(r.replace("Internal/group-" + groupId + "-", ""));
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
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-" + groupId)) {
                    String roleName = role.replace("Internal/group-" + groupId + "-", "");
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
            userStoreManager = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getAllRolesForGroup(groupId);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/group-" + groupId + "-" + role);
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
            devicesInGroup = GroupManagementDataHolder.getInstance().getDeviceManagementService().getDevicesOfGroup(groupId);
            return devicesInGroup;
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while getting devices in group", e);
        }
    }

    @Override
    public boolean addDeviceToGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = GroupManagementDataHolder.getInstance().getDeviceManagementService().getDevice(deviceId);
            deviceGroup = this.getGroupById(groupId);
            if (device == null || deviceGroup == null) {
                return false;
            }
            device.setGroupId(deviceGroup.getId());
            GroupManagementDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while adding device in to deviceGroup", e);
        }
        return true;
    }

    @Override
    public boolean removeDeviceFromGroup(DeviceIdentifier deviceId, int groupId) throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = GroupManagementDataHolder.getInstance().getDeviceManagementService().getDevice(deviceId);
            deviceGroup = this.getGroupById(groupId);
            if (device == null || deviceGroup == null) {
                return false;
            }
            device.setGroupId(0);
            GroupManagementDataHolder.getInstance().getDeviceManagementService().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while removing device from deviceGroup", e);
        }
        return true;
    }

    @Override
    public String[] getGroupPermissionsOfUser(String username, int groupId) throws GroupManagementException {
        UserRealm userRealm;
        List<String> roles = getGroupRolesForUser(username, groupId);
        int tenantId = DeviceManagerUtil.getTenantId();
        try {
            userRealm = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> lstPermissions = new ArrayList<>();
            String[] resourceIds = userRealm.getAuthorizationManager().getAllowedUIResourcesForUser(username, "/");
            if (resourceIds != null) {
                for (String resourceId : resourceIds) {
                    for (String roleName : roles) {
                        if (userRealm.getAuthorizationManager().
                                isRoleAuthorized("Internal/group-" + groupId + "-" + roleName, resourceId,
                                        CarbonConstants.UI_PERMISSION_ACTION)) {
                            lstPermissions.add(resourceId);
                        }
                    }
                }
            }
            String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
            String[] optimizedList = UserCoreUtil.optimizePermissions(permissions);
            return optimizedList;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm", e);
        }
    }

    @Override
    public List<DeviceGroup> getUserGroupsForPermission(String username, String permission) throws GroupManagementException {
        UserRealm userRealm;
        int tenantId = DeviceManagerUtil.getTenantId();
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        try {
            userRealm = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(username);
            for (String role : roles) {
                if (role != null && role.contains("Internal/group-") && userRealm.getAuthorizationManager().isRoleAuthorized(role, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    int groupId = Integer.parseInt(role.split("-")[1]);
                    if (!groups.containsKey(groupId)) {
                        DeviceGroup deviceGroup = getGroupById(groupId);
                        groups.put(groupId, deviceGroup);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm", e);
        }
    }

    @Override
    public boolean isAuthorized(String username, int groupId, String permission) throws GroupManagementException{
        UserRealm userRealm;
        int tenantId = DeviceManagerUtil.getTenantId();
        try {
            userRealm = GroupManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> roles = this.getGroupRolesForUser(username, groupId);
            for (String role : roles) {
                if (userRealm.getAuthorizationManager().isRoleAuthorized("Internal/group-"+ groupId + "-" + role, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    return true;
                }
            }
            return false;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm", e);
        }
    }
}
