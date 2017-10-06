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
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroupConstants;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupAlreadyExistException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupManagementException;
import org.wso2.carbon.device.mgt.common.group.mgt.GroupNotExistException;
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
            String msg = "Received incomplete data for createGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Creating group '" + deviceGroup.getName() + "'");
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
            String msg = "Error occurred while adding deviceGroup '" + deviceGroup.getName() + "' to database.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupAlreadyExistException ex) {
            throw ex;
        } catch (Exception e) {
            String msg = "Error occurred in creating group '" + deviceGroup.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
            throws GroupManagementException, GroupNotExistException {
        if (deviceGroup == null) {
            String msg = "Received incomplete data for updateGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("update group '" + deviceGroup.getName() + "'");
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            DeviceGroup existingGroup = this.groupDAO.getGroup(groupId, tenantId);
            if (existingGroup != null) {
                this.groupDAO.updateGroup(deviceGroup, groupId, tenantId);
                GroupManagementDAOFactory.commitTransaction();
            } else {
                throw new GroupNotExistException("Group with ID - '" + groupId + "' doesn't exists!");
            }
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while modifying device group with ID - '" + groupId + "'.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupNotExistException ex) {
            throw ex;
        } catch (Exception e) {
            String msg = "Error occurred in updating the device group with ID - '" + groupId + "'.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteGroup(int groupId) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Delete group: " + groupId);
        }
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
            String msg = "Error occurred while removing group data.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in deleting group: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup getGroup(int groupId) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get group by id: " + groupId);
        }
        DeviceGroup deviceGroup;
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while obtaining group '" + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroup for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (groupName == null) {
            String msg = "Received empty groupName for getGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get group by name '" + groupName + "'");
        }
        DeviceGroup deviceGroup;
        try {
            GroupManagementDAOFactory.openConnection();
            deviceGroup = this.groupDAO.getGroup(groupName, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while obtaining group with name: '" + groupName + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroup with name " + groupName;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroup;
    }

    @Override
    public List<DeviceGroup> getGroups() throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups");
        }
        List<DeviceGroup> deviceGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(tenantId);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroups;
    }

    @Override
    public PaginationResult getGroups(GroupPaginationRequest request) throws GroupManagementException {
        if (request == null) {
            String msg = "Received incomplete data for getGroup";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups with pagination " + request.toString());
        }
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<DeviceGroup> deviceGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            deviceGroups = this.groupDAO.getGroups(request, tenantId);
        } catch (GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (username == null || username.isEmpty()) {
            String msg = "Received null user name for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups of owner '" + username + "'");
        }
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
        } catch (UserStoreException | SQLException | GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups accessible to user.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for " + username;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return new ArrayList<>(groups.values());
    }

    private List<Integer> getGroupIds(String username) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups Ids of owner '" + username + "'");
        }
        UserStoreManager userStoreManager;
        List<Integer> deviceGroupIds;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            userStoreManager = DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager();
            String[] roleList = userStoreManager.getRoleListOfUser(username);
            GroupManagementDAOFactory.openConnection();
            deviceGroupIds = this.groupDAO.getOwnGroupIds(username, tenantId);
            deviceGroupIds.addAll(this.groupDAO.getGroupIds(roleList, tenantId));
        } catch (UserStoreException | SQLException | GroupManagementDAOException e) {
            String msg = "Error occurred while retrieving all groups accessible to user.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for username '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
        return deviceGroupIds;
    }

    @Override
    public PaginationResult getGroups(String currentUser, GroupPaginationRequest request)
            throws GroupManagementException {
        if (currentUser == null || request == null) {
            String msg = "Received incomplete date for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get all groups of user '" + currentUser + "' pagination request " + request.toString());
        }
        request = DeviceManagerUtil.validateGroupListPageSize(request);
        List<Integer> allDeviceGroupIdsOfUser = getGroupIds(currentUser);
        List<DeviceGroup> allMatchingGroups;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            allMatchingGroups = this.groupDAO.getGroups(request, allDeviceGroupIdsOfUser, tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Get groups count");
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    private int getGroupCount(GroupPaginationRequest request) throws GroupManagementException {
        if (request == null) {
            String msg = "Received empty request for getGroupCount";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups count, pagination request " + request.toString());
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroupCount(request, tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroupCount";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount(String username) throws GroupManagementException {
        if (username == null || username.isEmpty()) {
            String msg = "Received empty user name for getGroupCount";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups count of '" + username + "'");
        }
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
        } catch (UserStoreException | GroupManagementDAOException | SQLException  e) {
            String msg = "Error occurred while retrieving group count of user '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroupCount for username '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Manage group sharing for group: " + groupId);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserStoreManager userStoreManager;
        try {
            userStoreManager =
                    DeviceManagementDataHolder.getInstance().getRealmService().getTenantUserRealm(
                            tenantId).getUserStoreManager();
            List<String> currentUserRoles = getRoles(groupId);
            GroupManagementDAOFactory.beginTransaction();
            if (newRoles != null) {
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
            }
            for (String role : currentUserRoles) {
                // Removing old roles from db which are not available in the new roles list.
                groupDAO.removeRole(groupId, role, tenantId);
            }
            GroupManagementDAOFactory.commitTransaction();
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            log.error(e);
            throw new GroupManagementException(e);
        } catch (UserStoreException e) {
            String msg = "User store error in updating sharing roles.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            log.error(e);
            throw new GroupManagementException(e);
        } catch (Exception e) {
            String msg = "Error occurred in manageGroupSharing for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRoles(int groupId) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Group roles for group: " + groupId);
        }
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getRoles(groupId, tenantId);
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getRoles for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Group devices of group: " + groupId + " start index " + startIndex + " row count " + rowCount);
        }
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<Device> devices;
        try {
            rowCount = DeviceManagerUtil.validateDeviceListPageSize(rowCount);
            GroupManagementDAOFactory.openConnection();
            devices = this.groupDAO.getDevices(groupId, startIndex, rowCount, tenantId);
        } catch (GroupManagementDAOException | SQLException | DeviceManagementException e) {
            String msg = "Error occurred while getting devices in group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDevices for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Group devices count of group: " + groupId);
        }
        try {
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getDeviceCount(groupId, CarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving all groups in tenant";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getDeviceCount for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Group devices to the group: " + groupId);
        }
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevice(deviceIdentifier, false);
                if (device == null) {
                    throw new DeviceNotFoundException("Device not found for id '" + deviceIdentifier.getId() + "'");
                }
                if (!this.groupDAO.isDeviceMappedToGroup(groupId, device.getId(), tenantId)) {
                    this.groupDAO.addDevice(groupId, device.getId(), tenantId);
                }
            }
            GroupManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device to group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in addDevices for groupId " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
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
        if (log.isDebugEnabled()) {
            log.debug("Remove devices from the group: " + groupId);
        }
        Device device;
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            GroupManagementDAOFactory.beginTransaction();
            for (DeviceIdentifier deviceIdentifier : deviceIdentifiers) {
                device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                        getDevice(deviceIdentifier, false);
                if (device == null) {
                    throw new DeviceNotFoundException("Device not found for id '" + deviceIdentifier.getId() + "'");
                }
                this.groupDAO.removeDevice(groupId, device.getId(), tenantId);
            }
            GroupManagementDAOFactory.commitTransaction();
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while retrieving device.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while initiating transaction.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (GroupManagementDAOException e) {
            GroupManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while adding device to group.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in removeDevice for groupId: " + groupId;
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeviceGroup> getGroups(String username, String permission) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Get groups of user '" + username + "'");
        }
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
            String msg = "Error occurred while getting user realm.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups for username '" + username + "'";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        }
        return new ArrayList<>(permittedDeviceGroups.values());
    }

    @Override
    public List<DeviceGroup> getGroups(DeviceIdentifier deviceIdentifier) throws GroupManagementException {
        if (deviceIdentifier == null) {
            String msg = "Received empty device identifier for getGroups";
            log.error(msg);
            throw new GroupManagementException(msg);
        }
        if (log.isDebugEnabled()) {
            log.debug("Get groups of device " + deviceIdentifier.getId());
        }
        DeviceManagementProviderService managementProviderService = new DeviceManagementProviderServiceImpl();
        try {
            Device device = managementProviderService.getDevice(deviceIdentifier, false);
            GroupManagementDAOFactory.openConnection();
            return groupDAO.getGroups(device.getId(),
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
        } catch (DeviceManagementException | GroupManagementDAOException | SQLException e) {
            String msg = "Error occurred while retrieving device groups.";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred in getGroups";
            log.error(msg, e);
            throw new GroupManagementException(msg, e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeviceGroup createDefaultGroup(String groupName) throws GroupManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Create default group " + groupName);
        }
        DeviceGroup defaultGroup = this.getGroup(groupName);
        if (defaultGroup == null) {
            defaultGroup = new DeviceGroup(groupName);
            // Setting system level user (wso2.system.user) as the owner
            defaultGroup.setOwner(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            defaultGroup.setDescription("Default system group for devices with " + groupName + " ownership.");
            try {
                this.createGroup(defaultGroup, DeviceGroupConstants.Roles.DEFAULT_ADMIN_ROLE,
                        DeviceGroupConstants.Permissions.DEFAULT_ADMIN_PERMISSIONS);
            } catch (GroupAlreadyExistException e) {
                String msg = "Default group: " + defaultGroup.getName() + " already exists. Skipping group creation.";
                log.error(msg, e);
                throw new GroupManagementException(msg, e);
            } catch (Exception e) {
                String msg = "Error occurred in createDefaultGroup for groupName '" + groupName + "'";
                log.error(msg, e);
                throw new GroupManagementException(msg, e);
            }
            return this.getGroup(groupName);
        } else {
            return defaultGroup;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeviceMappedToGroup(int groupId, DeviceIdentifier deviceIdentifier)
            throws GroupManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Device device;
        try {
            device = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider().
                    getDevice(deviceIdentifier, false);
            if (device == null) {
                throw new GroupManagementException("Device not found for id '" + deviceIdentifier.getId() +
                                                   "' type '" + deviceIdentifier.getType() + "'");
            }
        } catch (DeviceManagementException e) {
            throw new GroupManagementException("Device management exception occurred when retrieving device. " +
                                               e.getMessage(), e);
        }

        try{
            GroupManagementDAOFactory.openConnection();
            return this.groupDAO.isDeviceMappedToGroup(groupId, device.getId(), tenantId);
        } catch (GroupManagementDAOException e) {
            throw new GroupManagementException("Error occurred when checking device, group mapping between device id '" +
                                               deviceIdentifier.getId() + "' and group id '" + groupId + "'", e);
        } catch (SQLException e) {
            throw new GroupManagementException("Error occurred when opening db connection to check device, group " +
                                               "mapping between device id '" + deviceIdentifier.getId() +
                                               "' and group id '" + groupId + "'", e);
        } finally {
            GroupManagementDAOFactory.closeConnection();
        }
    }
}
