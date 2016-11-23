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
import org.wso2.carbon.device.mgt.common.DeviceNotFoundException;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.TransactionManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.RoleDoesNotExistException;
import org.wso2.carbon.device.mgt.core.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.sql.SQLException;
import java.util.ArrayList;
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
        try {
            GroupManagementDAOFactory.beginTransaction();
            DeviceGroup existingGroup = this.groupDAO.getGroup(deviceGroup.getName(), tenantId);
            if (existingGroup == null) {
                this.groupDAO.addGroup(deviceGroup, tenantId);
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

        if (log.isDebugEnabled()) {
            log.debug("DeviceGroup added: " + deviceGroup.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGroup(DeviceGroup deviceGroup, int groupId)
            throws GroupManagementException, GroupAlreadyExistException {
        if (deviceGroup == null) {
            throw new GroupManagementException("DeviceGroup cannot be null.", new NullPointerException());
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            DeviceGroup existingGroup = this.groupDAO.getGroup(deviceGroup.getName(), tenantId);
            if (existingGroup == null || existingGroup.getGroupId() == groupId) {
                this.groupDAO.updateGroup(deviceGroup, groupId, tenantId);
                GroupManagementDAOFactory.commitTransaction();
            } else {
                throw new GroupAlreadyExistException("Group exist with name " + deviceGroup.getName());
            }
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
        DeviceGroup deviceGroup = getGroup(groupId);
        if (deviceGroup == null) {
            return false;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(int groupId) throws GroupManagementException {
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
        return deviceGroup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(String groupName) throws GroupManagementException {
        DeviceGroup deviceGroup;
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupName, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while obtaining group with name: '" + groupName + "'", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroup;
    }

    @Override
    public List<DeviceGroup> getGroups() throws GroupManagementException {
        List<DeviceGroup> deviceGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroups;
    }

    @Override
    public PaginationResult getGroups(GroupPaginationRequest request) throws GroupManagementException {
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<DeviceGroup> deviceGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(request, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        PaginationResult groupResult = new PaginationResult();
        groupResult.setData(deviceGroups);
        groupResult.setRecordsTotal(getGroupCount(request));
        return groupResult;
    }

    @Override
    public List<DeviceGroup> getGroups(String username) throws GroupManagementException {
        Map<Integer, DeviceGroup> groups = new HashMap<>();
        UserStoreManager userStoreManager;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            List<DeviceGroup> deviceGroups = this.groupDAO.getOwnGroups(username, tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                groups.put(deviceGroup.getGroupId(), deviceGroup);
            }
            deviceGroups = this.groupDAO.getGroups(roleList, tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                groups.put(deviceGroup.getGroupId(), deviceGroup);
            }
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups accessible to user.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException(e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return new ArrayList<>(groups.values());
    }

    private List<Integer> getGroupIds(String username) throws GroupManagementException {
        UserStoreManager userStoreManager;
        List<Integer> deviceGroupIds = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            deviceGroupIds = this.groupDAO.getOwnGroupIds(username, tenantId);
            deviceGroupIds.addAll(this.groupDAO.getGroupIds(roleList, tenantId));
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups accessible to user.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException(e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroupIds;
    }

    @Override
    public PaginationResult getGroups(String currentUser, GroupPaginationRequest request)
            throws GroupManagementException {
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<Integer> allDeviceGroupIdsOfUser = getGroupIds(currentUser);
        List<DeviceGroup> allMatchingGroups = new ArrayList<>();
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            allMatchingGroups = this.groupDAO.getGroups(request, allDeviceGroupIdsOfUser, tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving all groups in tenant", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred while opening a connection to the data source.", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        PaginationResult groupResult = new PaginationResult();
        groupResult.setData(allMatchingGroups);
        groupResult.setRecordsTotal(getGroupCount(currentUser));
        return groupResult;
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

    private int getGroupCount(GroupPaginationRequest request) throws GroupManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(request, tenantId);
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
        int count;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            count = groupDAO.getOwnGroupsCount(username, tenantId);
            count += groupDAO.getGroupsCount(roleList, tenantId);
            return count;
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user store manager.", e);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred while retrieving group count of user '" + username + "'", e);
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
    public void manageGroupSharing(int groupId, List<String> newRoles)
            throws GroupManagementException, RoleDoesNotExistException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStoreManager userStoreManager;
        try {
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
            List<String> currentUserRoles = getRoles(groupId);
            GroupManagementDAOFactory.beginTransaction();
            for (String role : newRoles) {
                if (!userStoreManager.isExistingRole(role)) {
                    throw new RoleDoesNotExistException("Role '" + role + "' does not exists in the user store.");
                }
                // Removing role from current user roles of the group will return true if role exist.
                // So we don't need to add it to the db again.
                if (!currentUserRoles.remove(role)) {
                    // If group doesn't have the role, it is adding to the db.
                    groupDAO.addRole(groupId, role, tenantId);
                }
            }
            for (String role : currentUserRoles) {
                // Removing old roles from db which are not available in the new roles list.
                groupDAO.removeRole(groupId, role, tenantId);
            }
            GroupManagementDAOFactory.commitTransaction();
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            throw new GroupManagementException(e);
        } catch (UserStoreException e) {
            throw new GroupManagementException("User store error in updating sharing roles.", e);
        } catch (TransactionManagementException e) {
            throw new GroupManagementException(e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(int groupId) throws GroupManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getRoles(groupId, tenantId);
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
    public void addDevices(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException, DeviceNotFoundException {
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers){
                device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
                if (device == null) {
                    throw new DeviceNotFoundException("Device not found for id '" + deviceIdentifier.getId() + "'");
                }
                if (!this.groupDAO.isDeviceMappedToGroup(groupId, device.getId(), tenantId)){
                    this.groupDAO.addDevice(groupId, device.getId(), tenantId);
                }
            }
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDevice(int groupId, List<DeviceIdentifier> deviceIdentifiers)
            throws GroupManagementException, DeviceNotFoundException {
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers){
                device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().getDevice(deviceIdentifier);
                if (device == null) {
                    throw new DeviceNotFoundException("Device not found for id '" + deviceIdentifier.getId() + "'");
                }
                this.groupDAO.removeDevice(groupId, device.getId(), tenantId);
            }
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username, String permission) throws GroupManagementException {
        List<DeviceGroup> deviceGroups = getGroups(username);
        Map<Integer, DeviceGroup> permittedDeviceGroups = new HashMap<>();
        UserRealm userRealm;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            userRealm = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId);
            for (DeviceGroup deviceGroup : deviceGroups) {
                List<String> roles = getRoles(deviceGroup.getGroupId());
                for (String roleName : roles) {
                    if (userRealm.getAuthorizationManager().
                            isRoleAuthorized(roleName, permission, CarbonConstants.UI_PERMISSION_ACTION)) {
                        permittedDeviceGroups.put(deviceGroup.getGroupId(), deviceGroup);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new GroupManagementException("Error occurred while getting user realm.", e);
        }
        return new ArrayList<>(permittedDeviceGroups.values());
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
}
