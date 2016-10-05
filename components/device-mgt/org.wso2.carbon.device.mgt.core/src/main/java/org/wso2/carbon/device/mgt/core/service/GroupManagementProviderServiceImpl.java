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

package org.wso2.carbon.device.mgt.core.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupUser;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.group.mgt.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.multiplecredentials.UserDoesNotExistException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupManagementProviderServiceImpl implements GroupManagementProviderService {

    private static Log log = LogFactory.getLog(GroupManagementProviderServiceImpl.class);

    private GroupDAO groupDAO;

    /**
     * Set groupDAO from GroupManagementDAOFactory when class instantiate.
     */
    public GroupManagementProviderServiceImpl() {
        this.groupDAO = GroupManagementDAOFactory.getGroupDAO();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createGroup(DeviceGroup deviceGroup, String defaultRole, String[] defaultPermissions)
            throws GroupManagementException, GroupAlreadyExistException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int groupId = -1;
        try {
            GroupManagementDAOFactory.beginTransaction();
            boolean nameIsExists = this.groupDAO.isGroupExist(deviceGroup.getName(), tenantId);
            if (!nameIsExists) {
                groupId = this.groupDAO.addGroup(deviceGroup, tenantId);
                GroupManagementDAOFactory.commitTransaction();
            } else {
                throw new GroupAlreadyExistException("Group exist with name " + deviceGroup.getName());
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

        addGroupSharingRole(deviceGroup.getOwner(), groupId, defaultRole, defaultPermissions);
        if (log.isDebugEnabled()) {
            log.debug("DeviceGroup added: " + deviceGroup.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(DeviceGroup deviceGroup, int groupId)
            throws GroupManagementException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            deviceGroup.setDateOfLastUpdate(new Date().getTime());
            this.groupDAO.updateGroup(deviceGroup, groupId, tenantId);
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
    public boolean deleteGroup(int groupId) throws GroupManagementException {
        String roleName;
        DeviceGroup deviceGroup = buildDeviceGroup(groupId);
        if (deviceGroup == null) {
            return false;
        }
        List<String> groupRoles = getRoles(groupId);
        for (String role : groupRoles) {
            if (role != null) {
                roleName = role.replace("Internal/group-" + deviceGroup.getGroupId() + "-", "");
                removeGroupSharingRole(deviceGroup.getGroupId(), roleName);
            }
        }
        try {
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.deleteGroup(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
            GroupManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("DeviceGroup " + deviceGroup.getName() + " removed.");
            }
            return true;
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while removing group data.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    private DeviceGroup buildDeviceGroup(int groupId) throws GroupManagementException {
        DeviceGroup deviceGroup;
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group '" + groupId + "'", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        if (deviceGroup != null) {
            deviceGroup.setUsers(this.getUsers(groupId));
            deviceGroup.setRoles(this.getRoles(groupId));
        }
        return deviceGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(int groupId) throws GroupManagementException {
        DeviceGroup deviceGroup = this.buildDeviceGroup(groupId);
        if (deviceGroup != null) {
            deviceGroup.setUsers(this.getUsers(groupId));
            deviceGroup.setRoles(this.getRoles(groupId));
        }
        return deviceGroup;
    }

    @Override
    public List<DeviceGroup> getGroups(int startIndex, int rowCount) throws GroupManagementException {
        List<DeviceGroup> deviceGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(startIndex, rowCount, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        for (DeviceGroup group : deviceGroups) {
            group.setUsers(this.getUsers(group.getGroupId()));
            group.setRoles(this.getRoles(group.getGroupId()));
        }
        return deviceGroups;
    }

    @Override
    public List<DeviceGroup> getGroups(String username, int startIndex, int rowCount) throws GroupManagementException {
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        UserStoreManager userStoreManager;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            int index = 0;
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-")) {
                    DeviceGroup deviceGroupBuilder = extractNewGroupFromRole(groups, role);
                    if (deviceGroupBuilder != null && startIndex <= index++ && index <= rowCount) {
                        groups.put(deviceGroupBuilder.getGroupId(), deviceGroupBuilder);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
        return new ArrayList<>(groups.values());
    }

    @Override
    public int getGroupCount() throws GroupManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        int count = 0;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            List<Integer> groupIds = new ArrayList<>();
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-")) {
                    int groupId = Integer.parseInt(role.split("-")[1]);
                    if (!groupIds.contains(groupId)) {
                        groupIds.add(groupId);
                        count++;
                    }
                }
            }
            return count;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shareGroup(String username, int groupId, String sharingRole)
            throws GroupManagementException, UserDoesNotExistException {
        return modifyGroupShare(username, groupId, sharingRole, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean unshareGroup(String username, int groupId, String sharingRole)
            throws GroupManagementException, UserDoesNotExistException {
        return modifyGroupShare(username, groupId, sharingRole, false);
    }

    private boolean modifyGroupShare(String username, int groupId, String sharingRole,
                                     boolean isAddNew)
            throws GroupManagementException, UserDoesNotExistException {
        if (groupId == -1) {
            return false;
        }
        UserStoreManager userStoreManager;
        String[] roles = new String[1];
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                throw new UserDoesNotExistException("User not exists with name " + username);
            }
            roles[0] = "Internal/group-" + groupId + "-" + sharingRole;
            List<String> currentRoles = getRoles(username, groupId);
            if (isAddNew && !currentRoles.contains(sharingRole)) {
                userStoreManager.updateRoleListOfUser(username, null, roles);
            } else if (!isAddNew && currentRoles.contains(sharingRole)) {
                userStoreManager.updateRoleListOfUser(username, roles, null);
            }
            return true;
        } catch (UserStoreException e) {
            if (e instanceof UserDoesNotExistException) {
                throw (UserDoesNotExistException) e;
            }
            throw new GroupManagementException("User store error in adding user " + username + " to group name:" +
                                               groupId, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addGroupSharingRole(String username, int groupId, String roleName, String[] permissions)
            throws GroupManagementException {
        if (groupId == -1) {
            return false;
        }
        UserStoreManager userStoreManager;
        String role;
        String[] userNames = new String[1];
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            role = "Internal/group-" + groupId + "-" + roleName;
            userNames[0] = username;
            Permission[] carbonPermissions = new Permission[permissions.length];
            for (int i = 0; i < permissions.length; i++) {
                carbonPermissions[i] = new Permission(permissions[i], CarbonConstants.UI_PERMISSION_ACTION);
            }
            userStoreManager.addRole(role, userNames, carbonPermissions);
            return true;
        } catch (UserStoreException e) {
            String errorMsg = "User store error in adding role to group id:" + groupId;
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeGroupSharingRole(int groupId, String roleName) throws GroupManagementException {
        if (groupId == -1) {
            return false;
        }
        UserStoreManager userStoreManager;
        String role;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(int groupId) throws GroupManagementException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(String username, int groupId)
            throws GroupManagementException, UserDoesNotExistException {
        UserStoreManager userStoreManager;
        List<String> groupRoleList = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            if (!userStoreManager.isExistingUser(username)) {
                throw new UserDoesNotExistException("User not exists with name " + username);
            }
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            for (String role : roleList) {
                if (role != null && role.contains("Internal/group-" + groupId)) {
                    String roleName = role.replace("Internal/group-" + groupId + "-", "");
                    groupRoleList.add(roleName);
                }
            }
            return groupRoleList;
        } catch (UserStoreException e) {
            if (e instanceof UserDoesNotExistException) {
                throw (UserDoesNotExistException) e;
            }
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GroupUser> getUsers(int groupId) throws GroupManagementException {
        UserStoreManager userStoreManager;
        Map<String, GroupUser> groupUserHashMap = new HashMap<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            List<String> rolesForGroup = this.getRoles(groupId);
            for (String role : rolesForGroup) {
                String[] users = userStoreManager.getUserListOfRole("Internal/group-" + groupId + "-" + role);
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
            String errorMsg = "User store error in fetching user list for group id:" + groupId;
            log.error(errorMsg, e);
            throw new GroupManagementException(errorMsg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Device> getDevices(int groupId, int startIndex, int rowCount)
            throws GroupManagementException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices;
        try {
            GroupManagementDAOFactory.openConnection();
            devices = this.groupDAO.getDevices(groupId, startIndex, rowCount, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while getting devices in group.", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return devices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDeviceCount(int groupId) throws GroupManagementException {
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getDeviceCount(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addDevice(DeviceIdentifier deviceIdentifier, int groupId)
            throws GroupManagementException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
            if (device == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.addDevice(groupId, device.getId(), tenantId);
            GroupManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while retrieving device.", e);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while adding device to group.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeDevice(DeviceIdentifier deviceIdentifier, int groupId) throws GroupManagementException {
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
            if (device == null) {
                return false;
            }
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            this.groupDAO.removeDevice(groupId, device.getId(), tenantId);
            GroupManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while retrieving device.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException("Error occurred while initiating transaction.", e);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException("Error occurred while adding device to group.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getPermissions(String username, int groupId)
            throws GroupManagementException, UserDoesNotExistException {
        UserRealm userRealm;
        List<String> roles = getRoles(username, groupId);
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
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
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            String[] roles = userRealm.getUserStoreManager().getRoleListOfUser(username);
            for (String role : roles) {
                if (role != null && role.contains("Internal/group-") && userRealm.getAuthorizationManager()
                        .isRoleAuthorized(role, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                    DeviceGroup group = extractNewGroupFromRole(groups, role);
                    if (group != null) {
                        groups.put(group.getGroupId(), group);
                    }
                }
            }
            return new ArrayList<>(groups.values());
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(DeviceIdentifier deviceIdentifier) throws GroupManagementException {
        DeviceManagementProviderService managementProviderService = new DeviceManagementProviderServiceImpl();
        try {
            Device device = managementProviderService.getDevice(deviceIdentifier);
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroups(device.getId(),
                                      PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Error occurred while retrieving the device details.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving device groups.", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening database connection.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    private DeviceGroup extractNewGroupFromRole(Map<Integer, DeviceGroup> groups, String role)
            throws GroupManagementException {
        try {
            int groupId = Integer.parseInt(role.split("-")[1]);
            if (!groups.containsKey(groupId)) {
                return buildDeviceGroup(groupId);
            }
        } catch (NumberFormatException e) {
            log.error("Unable to extract groupId from role " + role, e);
        }
        return null;
    }

}
