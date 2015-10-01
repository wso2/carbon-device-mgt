/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.group.core.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.group.common.DeviceGroup;
import org.wso2.carbon.device.mgt.group.core.internal.DeviceGroupBroker;

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

    private static final Log log = LogFactory.getLog(GroupDAOImpl.class);

    @Override public int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs;
        int groupId = -1;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "INSERT INTO DM_GROUP(DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, "
                    + "OWNER, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
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
            String msg = "Error occurred while adding deviceGroup " +
                    "'" + deviceGroup.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override public int updateGroup(DeviceGroup deviceGroup) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        int sqlReturn = -1;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "UPDATE DM_GROUP SET DESCRIPTION = ?, NAME = ?, DATE_OF_LAST_UPDATE = ?, OWNER = ? "
                    + "WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setLong(3, deviceGroup.getDateOfLastUpdate());
            stmt.setString(4, deviceGroup.getOwner());
            stmt.setInt(5, deviceGroup.getId());
            sqlReturn = stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while updating deviceGroup " +
                    "'" + deviceGroup.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
        return sqlReturn;
    }

    @Override public int deleteGroup(int groupId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        int sqlReturn = -1;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "DELETE FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            sqlReturn = stmt.executeUpdate();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group " +
                    "'" + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
        return sqlReturn;
    }

    @Override public DeviceGroup getGroup(int groupId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
                    + "FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return loadGroup(resultSet);
            } else {
                return null;
            }
        } catch (SQLException e) {
            String msg = "Error occurred while obtaining Device Group by id: " + groupId;
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
    }

    @Override public List<DeviceGroup> getGroups(int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroupList = null;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
                    + "FROM DM_GROUP WHERE TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            resultSet = stmt.executeQuery();
            deviceGroupList = new ArrayList<>();
            while (resultSet.next()) {
                deviceGroupList.add(loadGroup(resultSet));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups in tenant: " + tenantId;
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroupList;
    }

    @Override public List<DeviceGroup> getGroups(String groupName, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<DeviceGroup> deviceGroups = new ArrayList<>();
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNER "
                    + "FROM DM_GROUP WHERE NAME LIKE ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + groupName + "%");
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceGroups.add(loadGroup(resultSet));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing Device Groups by name: " + groupName + " in tenant: " + tenantId;
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceGroups;
    }

    private DeviceGroup loadGroup(ResultSet resultSet) throws SQLException {
        DeviceGroupBroker group = new DeviceGroupBroker(new DeviceGroup());
        group.setId(resultSet.getInt("ID"));
        group.setDescription(resultSet.getString("DESCRIPTION"));
        group.setName(resultSet.getString("NAME"));
        group.setDateOfCreation(resultSet.getLong("DATE_OF_ENROLLMENT"));
        group.setDateOfLastUpdate(resultSet.getLong("DATE_OF_LAST_UPDATE"));
        group.setOwner(resultSet.getString("OWNER"));
        return group.getGroup();
    }

}
