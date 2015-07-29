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
import org.wso2.carbon.device.mgt.group.common.Group;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupDAOImpl implements GroupDAO {


    private static final Log log = LogFactory.getLog(GroupDAOImpl.class);
    private DataSource dataSource;

    public GroupDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addGroup(Group group) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql =
                    "INSERT INTO DM_GROUP(DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, " +
                            "OWNER, TENANT_ID) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, group.getDescription());
            stmt.setString(2, group.getName());
            stmt.setLong(3, new Date().getTime());
            stmt.setLong(4, new Date().getTime());
            stmt.setString(5, group.getOwnerId());
            stmt.setInt(6, group.getTenantId());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while adding group " +
                    "'" + group.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public void updateGroup(Group group) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql =
                    "UPDATE DM_GROUP SET DESCRIPTION = ?, NAME = ?, DATE_OF_LAST_UPDATE = ?, " +
                            "OWNER = ? WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, group.getDescription());
            stmt.setString(2, group.getName());
            stmt.setLong(3, group.getDateOfLastUpdate());
            stmt.setString(4, group.getOwnerId());
            stmt.setInt(5, group.getId());
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while updating group " +
                    "'" + group.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public void deleteGroup(int groupId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql =
                    "DELETE FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group " +
                    "'" + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public Group getGroupById(int groupId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Group group = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                    "DATE_OF_LAST_UPDATE, OWNER, TENANT_ID FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                group = new Group();
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwnerId(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return group;
    }

    @Override
    public List<Group> getAllGroups() throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Group> groupList = null;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                    "DATE_OF_LAST_UPDATE, OWNER, TENANT_ID FROM DM_GROUP ";
            stmt = conn.prepareStatement(selectDBQueryForType);
            resultSet = stmt.executeQuery();
            groupList = new ArrayList<Group>();
            while (resultSet.next()) {
                Group group = new Group();
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwnerId(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
                groupList.add(group);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return groupList;
    }

    @Override
    public Group getGroupByName(String groupName, int tenantId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Group group = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                    "DATE_OF_LAST_UPDATE, OWNER, TENANT_ID FROM DM_GROUP WHERE NAME = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setInt(2, tenantId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                group = new Group();
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwnerId(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return group;
    }

    private Connection getConnection() throws GroupManagementDAOException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new GroupManagementDAOException(
                    "Error occurred while obtaining a connection from the group " +
                            "management metadata repository datasource", e);
        }
    }
}
