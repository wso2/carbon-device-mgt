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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.group.mgt.DeviceGroup;
import org.wso2.carbon.device.mgt.core.group.mgt.DeviceGroupBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
            String sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, "
                         + "OWNER, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[]{"id"});
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
                         + "WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setLong(3, deviceGroup.getDateOfLastUpdate());
            stmt.setString(4, deviceGroup.getOwner());
            stmt.setString(5, deviceGroup.getName());
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
    public void deleteGroup(String groupName, int tenantId) throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while deleting group '" + groupName + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceGroupBuilder getGroup(String groupName, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
                         + "FROM DM_GROUP WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return GroupManagementDAOUtil.loadGroup(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while obtaining information of Device Group '" +
                    groupName + "'", e);
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
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
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
    public List<DeviceGroupBuilder> getGroups(String groupName, int tenantId)
            throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroupBuilder> deviceGroups = new ArrayList<>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, GROUP_NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
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
        ResultSet rst = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID FROM DM_GROUP WHERE GROUP_NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            rst = stmt.executeQuery();
            return rst.next();
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while group Id listing by group name '" +
                    groupName + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, rst);
        }
    }

    @Override
    public void addDeviceToGroup(String group, Device device, int tenantId) throws GroupManagementDAOException {

    }

    @Override
    public void addDevicesToGroup(String group, Set<Device> devices,
                                  int tenantId) throws GroupManagementDAOException {

    }

    @Override
    public void removeDeviceFromGroup(String group, DeviceIdentifier id,
                                      int tenantId) throws GroupManagementDAOException {

    }

    @Override
    public boolean isDeviceMappedToGroup(String group, DeviceIdentifier id,
                                         int tenantId) throws GroupManagementDAOException {
        return false;
    }

    @Override
    public int getDeviceCount(String groupName, int tenantId) throws GroupManagementDAOException {
        return 0;
    }

}
