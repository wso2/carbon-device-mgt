/*
 *
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */
package org.wso2.carbon.device.application.mgt.core.dao.impl.lifecycle;

import org.wso2.carbon.device.application.mgt.common.Lifecycle;
import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.LifeCycleManagementDAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation for Lifecycle related DB operations.
 */
public class GenericLifecycleImpl extends AbstractDAOImpl implements LifecycleDAO {

    @Override
    public Lifecycle getLifeCycleOfApplication(int identifier) throws LifeCycleManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT ID, CREATED_BY, CREATED_TIMESTAMP, APPROVED, APPROVED_TIMESTAMP, APPROVED_BY, "
                    + "PUBLISHED, PUBLISHED_BY, PUBLISHED_TIMESTAMP, RETIRED FROM AP_APP_LIFECYCLE WHERE "
                    + "AP_APP_RELEASE_ID = ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, identifier);
            rs = stmt.executeQuery();

            Lifecycle lifecycle = null;

            if (rs.next()) {
                lifecycle = new Lifecycle();
                lifecycle.setId(rs.getInt("ID"));
                lifecycle.setCreatedBy(rs.getString("CREATED_BY"));
                lifecycle.setCreatedAt(rs.getDate("CREATED_TIMESTAMP"));
                lifecycle.setIsApproved(rs.getInt("APPROVED"));
                lifecycle.setApprovedAt(rs.getDate("APPROVED_TIMESTAMP"));
                lifecycle.setApprovedBy(rs.getString("APPROVED_BY"));
                lifecycle.setIsPublished(rs.getInt("PUBLISHED"));
                lifecycle.setPublishedBy(rs.getString("PUBLISHED_BY"));
                lifecycle.setPublishedAt(rs.getDate("PUBLISHED_TIMESTAMP"));
                lifecycle.setIsRetired(rs.getInt("RETIRED"));
            }
            return lifecycle;

        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<Lifecycle> getLifecyclesOfAllAppVersions(int identifier) throws LifeCycleManagementDAOException {
        List<Lifecycle> lifecycles = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT ID, CREATED_BY, CREATED_TIMESTAMP, APPROVED, APPROVED_TIMESTAMP, APPROVED_BY, "
                    + "PUBLISHED, PUBLISHED_BY, PUBLISHED_TIMESTAMP, RETIRED FROM AP_APP_LIFECYCLE WHERE "
                    + "AP_APP_ID = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, identifier);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Lifecycle lifecycle = new Lifecycle();
                lifecycle.setId(rs.getInt("ID"));
                lifecycle.setCreatedBy(rs.getString("CREATED_BY"));
                lifecycle.setCreatedAt(rs.getDate("CREATED_TIMESTAMP"));
                lifecycle.setIsApproved(rs.getInt("APPROVED"));
                lifecycle.setApprovedAt(rs.getDate("APPROVED_TIMESTAMP"));
                lifecycle.setApprovedBy(rs.getString("APPROVED_BY"));
                lifecycle.setIsPublished(rs.getInt("PUBLISHED"));
                lifecycle.setPublishedBy(rs.getString("PUBLISHED_BY"));
                lifecycle.setPublishedAt(rs.getDate("PUBLISHED_TIMESTAMP"));
                lifecycle.setIsRetired(rs.getInt("RETIRED"));
                lifecycles.add(lifecycle);
            }
        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while retrieving lifecycle states.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return lifecycles;
    }

    @Override
    public void addLifecycle(Lifecycle lifecycle) throws LifeCycleManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "INSERT INTO AP_APP_LIFECYCLE ('CREATED_BY', 'CREATED_TIMESTAMP') VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, lifecycle.getCreatedBy());
            stmt.setDate(2, new Date(lifecycle.getCreatedAt().getTime()));
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while adding lifecycle ", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

//    have to modify

    @Override
    public void updateLifecycleOfApplication(LifecycleState state) throws LifeCycleManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "INSERT INTO APPM_LIFECYCLE_STATE ('NAME', 'IDENTIFIER',) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, state.getName());
            stmt.setString(2, state.getIdentifier());
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while adding lifecycle: " + state.getIdentifier(), e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteLifecycleOfApplication(String identifier) throws LifeCycleManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "DELETE FROM APPM_LIFECYCLE_STATE WHERE IDENTIFIER = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, identifier);
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new LifeCycleManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new LifeCycleManagementDAOException("Error occurred while deleting lifecycle: " + identifier, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

//    end modification
}
