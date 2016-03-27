/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.*;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.SQLException;
import java.util.*;

public class GroupManagementServiceProviderImpl implements GroupManagementServiceProvider {

    private static Log log = LogFactory.getLog(GroupManagementServiceProviderImpl.class);

    private GroupDAO groupDAO;

    /**
     * Set groupDAO from GroupManagementDAOFactory when class instantiate.
     */
    public GroupManagementServiceProviderImpl() {
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int createGroup(DeviceGroup deviceGroup, String defaultRole, String[] defaultPermissions)
            throws GroupManagementException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        DeviceGroupBuilder groupBroker = new DeviceGroupBuilder(deviceGroup);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int groupId = -1;
        try {
            GroupManagementDAOFactory.beginTransaction();
            boolean nameIsExists = this.groupDAO.isGroupExist(deviceGroup.getName(), tenantId);
            if (!nameIsExists) {
                groupId = this.groupDAO.addGroup(groupBroker, tenantId);
                GroupManagementDAOFactory.commitTransaction();
            } else {
                return -2;
            }
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while adding deviceGroup " +
                    "'" + deviceGroup.getName() + "' to database.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        if (groupId == -1) {
            return -1;
        }
        addGroupSharingRole(groupBroker.getOwner(), deviceGroup.getName(), defaultRole, defaultPermissions);
        if (log.isDebugEnabled()) {
            log.debug("DeviceGroup added: " + groupBroker.getName());
        }
        return groupId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(DeviceGroup deviceGroup) throws GroupManagementException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        try {
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.updateGroup(deviceGroup, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while modifying deviceGroup " +
                    "'" + deviceGroup.getName() + "'.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteGroup(String groupName) throws GroupManagementException {
        String roleName;
        DeviceGroup deviceGroup = getGroup(groupName);
        if (deviceGroup == null) {
            return false;
        }
        List<String> groupRoles = getRoles(groupName);
        for (String role : groupRoles) {
            if (role != null) {
                roleName = role.replace("Internal/group-" + groupName + "-", "");
                removeGroupSharingRole(groupName, roleName);
            }
        }
        List<Device> groupDevices = getDevices(groupName);
        try {
            for (Device device : groupDevices) {
                DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().modifyEnrollment(device);
            }
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while removing device from group.", e);
        }
        try {
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.deleteGroup(groupName, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup " + deviceGroup.getName() + " removed.");
            }
            return true;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while removing group " +
                    "'" + groupName + "' data.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(String groupName) throws GroupManagementException {
        DeviceGroupBuilder groupBroker;
        try {
            GroupManagementDAOFactory.openConnection();
            groupBroker = this.groupDAO.getGroup(groupName, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group " + groupName, e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        if (groupBroker != null) {
            groupBroker.setUsers(this.getUsers(groupName));
            groupBroker.setRoles(this.getRoles(groupName));
            return groupBroker.getGroup();
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> findGroups(String groupName, String owner) throws GroupManagementException {
        List<DeviceGroupBuilder> deviceGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(groupName, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while finding group " + groupName, e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        List<DeviceGroup> groupsWithData = new ArrayList<>();
        for (DeviceGroupBuilder groupBroker : deviceGroups) {
            groupBroker.setUsers(this.getUsers(groupBroker.getName()));
            groupBroker.setRoles(this.getRoles(groupBroker.getName()));
            groupsWithData.add(groupBroker.getGroup());
        }
        return groupsWithData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            Map<String, DeviceGroup> groups = new HashMap<>();
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-")) {
                    String groupName = role.split("-")[1];
                    if (!groups.containsKey(groupName)) {
                        DeviceGroup deviceGroup = getGroup(groupName);
                        groups.put(groupName, deviceGroup);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount(String username) throws GroupManagementException {
        return this.getGroups(username).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shareGroup(String username, String groupName, String sharingRole)
            throws GroupManagementException {
        return modifyGroupShare(username, groupName, sharingRole, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unshareGroup(String username, String groupName, String sharingRole)
            throws GroupManagementException {
        return modifyGroupShare(username, groupName, sharingRole, false);
    }

    private boolean modifyGroupShare(String username, String groupName, String sharingRole,
                                     boolean isAddNew)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles = new String[1];
        try {
            DeviceGroup deviceGroup = getGroup(groupName);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
            roles[0] = "Internal/group-" + groupName + "-" + sharingRole;
            if (isAddNew) {
                userStoreManager.updateRoleListOfUser(username, null, roles);
            } else {
                userStoreManager.updateRoleListOfUser(username, roles, null);
            }
            return true;
        } catch (UserStoreException e) {
            throw new GroupManagementException("User store error in adding user " + username + " to group name:" +
                    groupName, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addGroupSharingRole(String username, String groupName, String roleName,
                                       String[] permissions)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        String[] userNames = new String[1];
        try {
            DeviceGroup deviceGroup = getGroup(groupName);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupName + "-" + roleName;
            userNames[0] = username;
            Permission[] carbonPermissions = new Permission[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                carbonPermissions[i] = new Permission(permissions[i], CarbonConstants.UI_PERMISSION_ACTION);
            }
            userStoreManager.addRole(role, userNames, carbonPermissions);
            return true;
        } catch (UserStoreException e) {
            String errorMsg = "User store error in adding role to group id:" + groupName;
            log.error(errorMsg, e);
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeGroupSharingRole(String groupName, String roleName)
            throws GroupManagementException {
        UserStoreManager userStoreManager;
        String role;
        try {
            DeviceGroup deviceGroup = getGroup(groupName);
            if (deviceGroup == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupName + "-" + roleName;
            userStoreManager.deleteRole(role);
            return true;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupName;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(String groupName) throws GroupManagementException {
        UserStoreManager userStoreManager;
        String[] roles;
        List<String> groupRoles;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            roles = userStoreManager.getRoleNames();
            groupRoles = new ArrayList<>();
            for (String r : roles) {
                if (r != null && r.contains("Internal/group-" + groupName + "-")) {
                    groupRoles.add(r.replace("Internal/group-" + groupName + "-", ""));
                }
            }
            return groupRoles;
        } catch (UserStoreException userStoreEx) {
            String errorMsg = "User store error in adding role to group id:" + groupName;
            log.error(errorMsg, userStoreEx);
            throw new GroupManagementException(errorMsg, userStoreEx);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(String username, String groupName) throws GroupManagementException {
        UserStoreManager userStoreManager;
        List<String> groupRoleList = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-" + groupName)) {
                    String roleName = role.replace("Internal/group-" + groupName + "-", "");
                    groupRoleList.add(roleName);
                }
            }
            return groupRoleList;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupUser> getUsers(String groupName) throws GroupManagementException {
        UserStoreManager userStoreManager;
        Map<String, GroupUser> groupUserHashMap = new HashMap<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getRoles(groupName);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/group-" + groupName + "-" + role);
                for (String user : users) {
                    GroupUser groupUser;
                    if (groupUserHashMap.containsKey(user)) {
                        groupUser = groupUserHashMap.get(user);
                        groupUser.getGroupRoles().add(role);
                    } else {
                        groupUser = new GroupUser();
                        groupUser.setUsername(user);
                        groupUser.setGroupRoles(new ArrayList<String>());
                        groupUser.getGroupRoles().add(role);
                        groupUserHashMap.put(user, groupUser);
                    }
                }
            }
            return new ArrayList<>(groupUserHashMap.values());
        } catch (UserStoreException e) {
            String errorMsg = "User store error in fetching user list for group id:" + groupName;
            log.error(errorMsg, e);
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Device> getDevices(String groupName) throws GroupManagementException {
        return Collections.emptyList();
        //TODO: Add a method that returns a collection of devices in a particular group to GroupDAO
//        try {
//            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevices(groupName);
//        } catch (DeviceManagementException e) {
//            throw new GroupManagementException("Error occurred while getting devices in group.", e);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginationResult getDevices(String groupName, PaginationRequest request)
            throws GroupManagementException {
        return null;
        //TODO: Add a method that returns a collection of devices in a particular group to GroupDAO
//        try {
//            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevices(groupName);
//        } catch (DeviceManagementException e) {
//            throw new GroupManagementException("Error occurred while getting devices in group.", e);
//        }
//        try {
//            return DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevices(groupName,
//                    request);
//        } catch (DeviceManagementException e) {
//            throw new GroupManagementException("Error occurred while getting devices in group.", e);
//        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeviceCount(String groupName) throws GroupManagementException {
        try {
            int count;
            GroupManagementDAOFactory.beginTransaction();
            count = groupDAO.getDeviceCount(groupName,
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
            return count;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while retrieving device count of group " +
                    "'" + groupName + "'.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addDevice(DeviceIdentifier deviceId, String groupName)
            throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId);
            deviceGroup = this.getGroup(groupName);
            if (device == null || deviceGroup == null) {
                return false;
            }
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while adding device in to deviceGroup.", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeDevice(DeviceIdentifier deviceId, String groupName)
            throws GroupManagementException {
        Device device;
        DeviceGroup deviceGroup;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceId);
            deviceGroup = this.getGroup(groupName);
            if (device == null || deviceGroup == null) {
                return false;
            }
            DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().modifyEnrollment(device);
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while removing device from deviceGroup.", e);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPermissions(String username, String groupName) throws GroupManagementException {
        UserRealm userRealm;
        List<String> roles = getRoles(username, groupName);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> lstPermissions = new ArrayList<>();
            String[] resourceIds = userRealm.getAuthorizationManager().getAllowedUIResourcesForUser(username, "/");
            if (resourceIds != null) {
                for (String resourceId : resourceIds) {
                    for (String roleName : roles) {
                        if (userRealm.getAuthorizationManager().
                                isRoleAuthorized("Internal/group-" + groupName + "-" + roleName, resourceId,
                                        CarbonConstants.UI_PERMISSION_ACTION)) {
                            lstPermissions.add(resourceId);
                        }
                    }
                }
            }
            String[] permissions = lstPermissions.toArray(new String[lstPermissions.size()]);
            return UserCoreUtil.optimizePermissions(permissions);
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username, String permission)
            throws GroupManagementException {
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, DeviceGroup> groups = new HashMap<>();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(username);
            for (String role : roles) {
                if (role != null && role.contains("Internal/group-") && userRealm.getAuthorizationManager()
                        .isRoleAuthorized(role, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    String groupName = role.split("-")[1];
                    if (!groups.containsKey(groupName)) {
                        DeviceGroup deviceGroup = getGroup(groupName);
                        groups.put(groupName, deviceGroup);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(String username, String groupName, String permission)
            throws GroupManagementException {
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            List<String> roles = this.getRoles(username, groupName);
            for (String role : roles) {
                if (userRealm.getAuthorizationManager()
                        .isRoleAuthorized("Internal/group-" + groupName + "-" + role, permission,
                                CarbonConstants.UI_PERMISSION_ACTION)) {
                    return true;
                }
            }
            return false;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

}
