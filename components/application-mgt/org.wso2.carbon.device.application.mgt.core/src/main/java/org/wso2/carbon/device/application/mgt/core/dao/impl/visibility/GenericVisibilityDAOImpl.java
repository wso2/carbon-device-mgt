/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.dao.impl.visibility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.UnrestrictedRole;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.VisibilityDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.VisibilityManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic database level implementation for the DAO which can be used by different databases.
 */
public class GenericVisibilityDAOImpl extends AbstractDAOImpl implements VisibilityDAO {

    private static final Log log = LogFactory.getLog(GenericVisibilityDAOImpl.class);

    @Override
    public void addUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws
            VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to add unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int index = 0;
        String sql = "INSERT INTO AP_UNRESTRICTED_ROLES (ROLE, TENANT_ID, AP_APP_ID) VALUES (?, ?, ?)";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            for (UnrestrictedRole role : unrestrictedRoles) {
                stmt.setString(++index, role.getRole());
                stmt.setInt(++index, tenantId);
                stmt.setInt(++index, applicationId);
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<UnrestrictedRole> getUnrestrictedRoles(int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<UnrestrictedRole> unrestrictedRoles = new ArrayList<>();
        UnrestrictedRole unrestrictedRole = null;
        int index = 0;
        String sql = "SELECT ID, ROLE FROM AP_UNRESTRICTED_ROLES WHERE AP_APP_ID = ? AND TENANT_ID = ?;";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);
            stmt.setInt(++index, applicationId);
            stmt.setInt(++index, tenantId);
            rs = stmt.executeQuery();

            while (rs.next()){
                unrestrictedRole = new UnrestrictedRole();
                unrestrictedRole.setId(rs.getInt("ID"));
                unrestrictedRole.setRole(rs.getString("ROLE"));
                unrestrictedRoles.add(unrestrictedRole);
            }
            return unrestrictedRoles;

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteUnrestrictedRoles(List<UnrestrictedRole> unrestrictedRoles, int applicationId, int tenantId) throws VisibilityManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to delete unrestricted roles");
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int index = 0;
        String sql = "DELETE FROM AP_UNRESTRICTED_ROLES WHERE AP_APP_ID = 1 AND ROLE = 'role1' AND TENANT_ID = -1234;";
        try{
            conn = this.getDBConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);

            for (UnrestrictedRole role : unrestrictedRoles) {
                stmt.setInt(++index, applicationId);
                stmt.setString(++index, role.getRole());
                stmt.setInt(++index, role.getTenantId());
                stmt.addBatch();
            }
            stmt.executeBatch();

        }catch (DBConnectionException e) {
            throw new VisibilityManagementDAOException("Error occurred while obtaining the DB connection when adding roles", e);
        }catch (SQLException e) {
            throw new VisibilityManagementDAOException("Error occurred while adding unrestricted roles", e);
        } finally {
            Util.cleanupResources(stmt, rs);
        }
    }
}
