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

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.GroupPaginationRequest;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.dao.GroupDAO;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dao.util.GroupManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents implementation of GroupDAO
 */
public class GroupDAOImpl implements GroupDAO {

    @Override
    public int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs;
        int groupId = -1;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"ID"});
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setString(3, deviceGroup.getOwner());
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                groupId = rs.getInt(1);
            }
            return groupId;
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding deviceGroup '" +
                                                          deviceGroup.getName() + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateGroup(DeviceGroup deviceGroup, int groupId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "UPDATE DM_GROUP SET DESCRIPTION = ?, GROUP_NAME = ?, OWNER = ? WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setString(3, deviceGroup.getOwner());
            stmt.setInt(4, groupId);
            stmt.setInt(5, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while updating deviceGroup '" +
                                                          deviceGroup.getName() + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteGroup(int groupId, int tenantId) throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
            sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
            sql = "DELETE FROM DM_DEVICE_GROUP_POLICY WHERE DEVICE_GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing mappings for group.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }

        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while deleting group.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceGroup getGroup(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return GroupManagementDAOUtil.loadGroup(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while obtaining information of Device Group '" +
                                                          groupId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(int deviceId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupBuilders = new ArrayList<>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT G.ID, G.GROUP_NAME, G.DESCRIPTION, G.OWNER FROM DM_GROUP G " +
                    "INNER JOIN DM_DEVICE_GROUP_MAP GM ON G.ID = GM.GROUP_ID " +
                    "WHERE GM.DEVICE_ID = ? AND GM.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceGroupBuilders.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while obtaining information of Device Groups ", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupBuilders;
    }

    @Override
    public List<DeviceGroup> getGroups(GroupPaginationRequest request, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;

        String groupName = request.getGroupName();
        boolean hasGroupName = false;
        String owner = request.getOwner();
        boolean hasOwner = false;
        boolean hasLimit = request.getRowCount() != 0;

        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE TENANT_ID = ?";
            if (groupName != null && !groupName.isEmpty()) {
                sql += " AND GROUP_NAME LIKE ?";
                hasGroupName = true;
            }
            if (owner != null && !owner.isEmpty()) {
                sql += " AND OWNER LIKE ?";
                hasOwner = true;
            }
            if (hasLimit) {
                sql += " LIMIT ?, ?";
            }

            int paramIndex = 1;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);
            if (hasGroupName) {
                stmt.setString(paramIndex++, groupName + "%");
            }
            if (hasOwner) {
                stmt.setString(paramIndex++, owner + "%");
            }
            if (hasLimit) {
                stmt.setInt(paramIndex++, request.getStartIndex());
                stmt.setInt(paramIndex, request.getRowCount());
            }
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<DeviceGroup> getGroups(GroupPaginationRequest request, List<Integer> deviceGroupIds,
                                       int tenantId) throws GroupManagementDAOException {
        int deviceGroupIdsCount = deviceGroupIds.size();
        if (deviceGroupIdsCount == 0) {
            return new ArrayList<>();
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;

        String groupName = request.getGroupName();
        boolean hasGroupName = false;
        String owner = request.getOwner();
        boolean hasOwner = false;
        boolean hasLimit = request.getRowCount() != 0;

        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE TENANT_ID = ?";
            if (groupName != null && !groupName.isEmpty()) {
                sql += " AND GROUP_NAME LIKE ?";
                hasGroupName = true;
            }
            if (owner != null && !owner.isEmpty()) {
                sql += " AND OWNER LIKE ?";
                hasOwner = true;
            }
            sql += " AND ID IN (";
            for (int i = 0; i < deviceGroupIdsCount; i++) {
                sql += (deviceGroupIdsCount - 1 != i) ? "?," : "?";
            }
            sql += ")";
            if (hasLimit) {
                sql += " LIMIT ?, ?";
            }

            int paramIndex = 1;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);
            if (hasGroupName) {
                stmt.setString(paramIndex++, groupName + "%");
            }
            if (hasOwner) {
                stmt.setString(paramIndex++, owner + "%");
            }
            for (Integer deviceGroupId : deviceGroupIds) {
                stmt.setInt(paramIndex++, deviceGroupId);
            }
            if (hasLimit) {
                stmt.setInt(paramIndex++, request.getStartIndex());
                stmt.setInt(paramIndex, request.getRowCount());
            }
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public int getGroupCount(int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting group count'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public int getGroupCount(GroupPaginationRequest request, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;

        String groupName = request.getGroupName();
        boolean hasGroupName = false;
        String owner = request.getOwner();
        boolean hasOwner = false;

        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE TENANT_ID = ?";
            if (groupName != null && !groupName.isEmpty()) {
                sql += " AND GROUP_NAME LIKE ?";
                hasGroupName = true;
            }
            if (owner != null && !owner.isEmpty()) {
                sql += " AND OWNER LIKE ?";
                hasOwner = true;
            }

            int paramIndex = 1;
            stmt = conn.prepareStatement(sql);
            stmt.setInt(paramIndex++, tenantId);
            if (hasGroupName) {
                stmt.setString(paramIndex++, groupName + "%");
            }
            if (hasOwner) {
                stmt.setString(paramIndex, owner + "%");
            }
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public DeviceGroup getGroup(String groupName, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE GROUP_NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return GroupManagementDAOUtil.loadGroup(resultSet);
            }
            return null;
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name.'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void addDevice(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_DEVICE_GROUP_MAP(DEVICE_ID, GROUP_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding device to Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeDevice(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ? AND GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing device from Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public boolean isDeviceMappedToGroup(int groupId, int deviceId, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, deviceId);
            stmt.setInt(3, tenantId);
            resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while checking device mapping with group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public int getDeviceCount(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql =
                    "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("DEVICE_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting device count from the group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<Device> getDevices(int groupId, int startIndex, int rowCount, int tenantId)
            throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT gd.DEVICE_ID, gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.DEVICE_TYPE_ID FROM" +
                    " DM_DEVICE d, (" +
                    "SELECT dgm.DEVICE_ID FROM DM_DEVICE_GROUP_MAP dgm WHERE dgm.GROUP_ID = ?) dgm1 " +
                    "WHERE d.ID = dgm1.DEVICE_ID AND d.TENANT_ID = ?) gd, DM_DEVICE_TYPE t " +
                    "WHERE gd.DEVICE_TYPE_ID = t.ID) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? LIMIT ? , ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            //noinspection JpaQueryApiInspection
            stmt.setInt(4, startIndex);
            //noinspection JpaQueryApiInspection
            stmt.setInt(5, rowCount);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while retrieving information of all " +
                                                          "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<String> getRoles(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<String> userRoles;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ROLE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            userRoles = new ArrayList<>();
            while (resultSet.next()) {
                userRoles.add(resultSet.getString("ROLE"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return userRoles;
    }

    @Override
    public void addRole(int groupId, String role, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_ROLE_GROUP_MAP(GROUP_ID, ROLE, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setString(2, role);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding new user role to Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeRole(int groupId, String role, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_ROLE_GROUP_MAP WHERE GROUP_ID = ? AND ROLE = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setString(2, role);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing device from Group.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<DeviceGroup> getGroups(String[] roles, int tenantId) throws GroupManagementDAOException {
        int rolesCount = roles.length;
        if (rolesCount == 0) {
            return new ArrayList<>();
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP g, " +
                    "(SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";

            int index = 0;
            while (index++ < rolesCount - 1) {
                sql += "?,";
            }
            sql += "?)) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? GROUP BY g.ID";

            stmt = conn.prepareStatement(sql);
            index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<Integer> getGroupIds(String[] roles, int tenantId) throws GroupManagementDAOException {
        if (roles.length == 0) {
            return new ArrayList<>();
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceGroupIdList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP g, (SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";

            int rolesCount = roles.length;
            for (int i = 0; i < rolesCount; i++) {
                sql += (rolesCount - 1 != i) ? "?," : "?";
            }
            sql += ")) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? GROUP BY g.ID";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupIdList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupIdList.add(resultSet.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing all groups in tenant: " + tenantId, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupIdList;
    }

    @Override
    public int getGroupsCount(String[] roles, int tenantId) throws GroupManagementDAOException {
        int rolesCount = roles.length;
        if (rolesCount == 0) {
            return 0;
        }
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP g, " +
                    "(SELECT GROUP_ID FROM DM_ROLE_GROUP_MAP WHERE ROLE IN (";
            for (int i = 0; i < rolesCount; i++) {
                sql += (rolesCount - 1 != i) ? "?," : "?";
            }
            sql += ")) gr WHERE g.ID = gr.GROUP_ID AND TENANT_ID = ? GROUP BY g.ID";

            stmt = conn.prepareStatement(sql);
            int index = 0;
            while (index++ < rolesCount) {
                stmt.setString(index, roles[index - 1]);
            }
            stmt.setInt(index, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting permitted groups count.", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<DeviceGroup> getOwnGroups(String username, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, OWNER FROM DM_GROUP WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups of user '"
                                                          + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override
    public List<Integer> getOwnGroupIds(String username, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Integer> deviceGroupIdList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupIdList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupIdList.add(resultSet.getInt("ID"));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups of user '"
                                                          + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupIdList;
    }

    @Override
    public int getOwnGroupsCount(String username, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT COUNT(ID) AS GROUP_COUNT FROM DM_GROUP WHERE OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("GROUP_COUNT");
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while getting own groups count of user '"
                                                          + username + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

}
