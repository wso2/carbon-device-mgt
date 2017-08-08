/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.dao.impl.lifecyclestate;

import org.wso2.carbon.device.application.mgt.common.LifecycleState;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.LifecycleStateDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.exception.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericLifecycleStateImpl extends AbstractDAOImpl implements LifecycleStateDAO {

    @Override
    public LifecycleState getLifeCycleStateByIdentifier(String identifier) throws ApplicationManagementDAOException {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String sql = "";

        try {
            conn = this.getConnection();
            sql += "SELECT * ";
            sql += "FROM APPM_LIFECYCLE_STATE ";
            sql += "WHERE IDENTIFIER = ? ";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, identifier);
            rs = stmt.executeQuery();

            LifecycleState lifecycleState = null;

            if (rs.next()) {
                lifecycleState = new LifecycleState();
                lifecycleState.setId(rs.getInt("ID"));
                lifecycleState.setName(rs.getString("NAME"));
                lifecycleState.setIdentifier(rs.getString("IDENTIFIER"));
                lifecycleState.setDescription(rs.getString("DESCRIPTION"));
            }
            return lifecycleState;

        } catch (SQLException e) {
            throw new ApplicationManagementDAOException("Error occurred while getting application List", e);
        }  catch (DBConnectionException e) {
            throw new ApplicationManagementDAOException("Error occurred while obtaining the DB connection.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<LifecycleState> getLifecycleStates() throws DAOException {
        List<LifecycleState> lifecycleStates = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "SELECT IDENTIFIER, NAME, DESCRIPTION FROM APPM_LIFECYCLE_STATE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            while(rs.next()) {
                LifecycleState lifecycleState = new LifecycleState();
                lifecycleState.setIdentifier(rs.getString("IDENTIFIER"));
                lifecycleState.setName(rs.getString("NAME"));
                lifecycleState.setDescription(rs.getString("DESCRIPTION"));
                lifecycleStates.add(lifecycleState);
            }
        } catch (DBConnectionException e) {
            throw new DAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new DAOException("Error occurred while retrieving lifecycle states.", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
        return lifecycleStates;
    }

    @Override
    public void addLifecycleState(LifecycleState state) throws DAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getDBConnection();
            String sql = "INSERT INTO APPM_LIFECYCLE_STATE ('NAME', 'IDENTIFIER', 'DESCRIPTION') VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, state.getName());
            stmt.setString(2, state.getIdentifier());
            stmt.setString(3, state.getDescription());
            stmt.executeUpdate();

        } catch (DBConnectionException e) {
            throw new DAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new DAOException("Error occurred while adding lifecycle: " + state.getIdentifier(), e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteLifecycleState(String identifier) throws DAOException {
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
            throw new DAOException("Error occurred while obtaining the DB connection.", e);
        } catch (SQLException e) {
            throw new DAOException("Error occurred while deleting lifecycle: " + identifier, e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }
}
