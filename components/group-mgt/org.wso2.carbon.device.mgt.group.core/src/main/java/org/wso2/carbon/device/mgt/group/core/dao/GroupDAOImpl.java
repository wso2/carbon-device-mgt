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
import org.wso2.carbon.device.mgt.group.core.internal.GroupBroker;

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
    public int addGroup(Group group) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int sqlReturn = -1;
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
            stmt.setString(5, group.getOwner());
            stmt.setInt(6, group.getTenantId());
            sqlReturn = stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while adding group " +
                    "'" + group.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
        return sqlReturn;
    }

    @Override
    public int updateGroup(Group group) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int sqlReturn = -1;
        try {
            conn = this.getConnection();
            String sql =
                    "UPDATE DM_GROUP SET DESCRIPTION = ?, NAME = ?, DATE_OF_LAST_UPDATE = ?, " +
                            "OWNER = ? WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, group.getDescription());
            stmt.setString(2, group.getName());
            stmt.setLong(3, group.getDateOfLastUpdate());
            stmt.setString(4, group.getOwner());
            stmt.setInt(5, group.getId());
            sqlReturn = stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while updating group " +
                    "'" + group.getName() + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
        return sqlReturn;
    }

    @Override
    public int deleteGroup(int groupId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        int sqlReturn = -1;
        try {
            conn = this.getConnection();
            String sql =
                    "DELETE FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            sqlReturn = stmt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting group " +
                    "'" + groupId + "'";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
        return sqlReturn;
    }

    @Override
    public Group getGroupById(int groupId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        GroupBroker group = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                    "DATE_OF_LAST_UPDATE, OWNER, TENANT_ID FROM DM_GROUP WHERE ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                group = new GroupBroker(new Group());
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwner(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return group.getGroup();
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
            groupList = new ArrayList<>();
            while (resultSet.next()) {
                GroupBroker group = new GroupBroker(new Group());
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwner(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
                groupList.add(group.getGroup());
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
    public List<Group> getGroupsByName(String groupName, String owner, int tenantId) throws GroupManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Group> groups = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                    "DATE_OF_LAST_UPDATE, OWNER, TENANT_ID FROM DM_GROUP WHERE NAME = ? AND OWNER = ? AND TENANT_ID = ? ORDER BY ID DESC LIMIT 1";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, groupName);
            stmt.setString(2, owner);
            stmt.setInt(3, tenantId);
            resultSet = stmt.executeQuery();
            GroupBroker group;
            while (resultSet.next()) {
                group = new GroupBroker(new Group());
                group.setId(resultSet.getInt(1));
                group.setDescription(resultSet.getString(2));
                group.setName(resultSet.getString(3));
                group.setDateOfCreation(resultSet.getLong(4));
                group.setDateOfLastUpdate(resultSet.getLong(5));
                group.setOwner(resultSet.getString(6));
                group.setTenantId(resultSet.getInt(7));
                groups.add(group.getGroup());
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all groups";
            log.error(msg, e);
            throw new GroupManagementDAOException(msg, e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return groups;
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
