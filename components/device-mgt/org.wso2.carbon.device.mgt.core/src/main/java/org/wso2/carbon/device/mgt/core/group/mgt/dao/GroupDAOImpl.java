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

package org.wso2.carbon.device.mgt.core.group.mgt.dao;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
            String sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, DATE_OF_CREATE, DATE_OF_LAST_UPDATE, "
                         + "OWNER, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"ID"});
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setLong(3, new Date().getTime());
            stmt.setLong(4, new Date().getTime());
            stmt.setString(5, deviceGroup.getOwner());
            stmt.setInt(6, tenantId);
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
    public void updateGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_GROUP SET DESCRIPTION = ?, GROUP_NAME = ?, DATE_OF_LAST_UPDATE = ?, OWNER = ? "
                         + "WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setLong(3, deviceGroup.getDateOfLastUpdate());
            stmt.setString(4, deviceGroup.getOwner());
            stmt.setInt(5, deviceGroup.getId());
            stmt.setInt(6, tenantId);
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
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing mappings for group '" + groupId + "'", e);
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
            throw new GroupManagementDAOException("Error occurred while deleting group '" + groupId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceGroupBuilder getGroup(int groupId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_CREATE, DATE_OF_LAST_UPDATE, OWNER "
                         + "FROM DM_GROUP WHERE ID = ? AND TENANT_ID = ?";
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
    public List<DeviceGroupBuilder> getGroups(int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroupBuilder> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_CREATE, DATE_OF_LAST_UPDATE, OWNER "
                         + "FROM DM_GROUP WHERE TENANT_ID = ?";
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
    public List<DeviceGroupBuilder> findInGroups(String groupName, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroupBuilder> deviceGroups = new ArrayList<>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_CREATE, DATE_OF_LAST_UPDATE, OWNER "
                         + "FROM DM_GROUP WHERE GROUP_NAME LIKE ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + groupName + "%");
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceGroups.add(GroupManagementDAOUtil.loadGroup(resultSet));
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while listing Device Groups by name '" +
                    groupName + "' in tenant '" + tenantId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroups;
    }

    @Override
    public boolean isGroupExist(String groupName, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP WHERE GROUP_NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name '" +
                    groupName + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public void addDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_DEVICE_GROUP_MAP(DEVICE_ID, GROUP_ID, TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
            stmt.getGeneratedKeys();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding device to Group '" + groupId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void removeDevice(int groupId, int deviceId, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ? AND GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();
            stmt.getGeneratedKeys();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while removing device from Group '" +
                                                  groupId + "'", e);
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
            String sql = "SELECT ID FROM DM_DEVICE_GROUP_MAP WHERE DEVICE_ID = ? AND GROUP_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, groupId);
            stmt.setInt(3, tenantId);
            resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name '" +
                                                  groupId + "'", e);
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
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_DEVICE_GROUP_MAP WHERE GROUP_ID = ? " +
                         "AND TENANT_ID = ?";
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
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name '" +
                                                  groupId + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override
    public List<Device> getDevices(int groupId, int tenantId) throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT gd.DEVICE_ID, " +
                         "gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE " +
                         "FROM (SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, " +
                         "d.DEVICE_TYPE_ID FROM DM_DEVICE d, DM_DEVICE_GROUP_MAP dgm WHERE dgm.GROUP_ID = ? " +
                         "AND d.ID = dgm.DEVICE_ID AND d.TENANT_ID = ?) gd, DM_DEVICE_TYPE t " +
                         "WHERE gd.DEVICE_TYPE_ID = t.ID) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
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

    @SuppressWarnings("JpaQueryApiInspection")
    @Override
    public List<Device> getDevices(int groupId, PaginationRequest request, int tenantId)
            throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT gd.DEVICE_ID, " +
                         "gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE " +
                         "FROM (SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, " +
                         "d.DEVICE_TYPE_ID FROM DM_DEVICE d, DM_DEVICE_GROUP_MAP dgm WHERE dgm.GROUP_ID = ? " +
                         "AND d.ID = dgm.DEVICE_ID AND d.TENANT_ID = ?) gd, DM_DEVICE_TYPE t " +
                         "WHERE gd.DEVICE_TYPE_ID = t.ID) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? " +
                         "LIMIT ?, ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            //noinspection JpaQueryApiInspection
            stmt.setInt(4, request.getStartIndex());
            stmt.setInt(5, request.getRowCount());
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

}
