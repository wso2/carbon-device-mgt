/*
*  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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
*/

package org.wso2.carbon.device.mgt.core.scope.mgt.dao.impl;

import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAO;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAOException;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.scope.mgt.dao.ScopeManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScopeManagementDAOImpl implements ScopeManagementDAO {

    @Override
    public void updateScopes(List<Scope> scopes) throws ScopeManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.getConnection();
            String sql = "UPDATE IDN_OAUTH2_SCOPE SET ROLES=? WHERE SCOPE_KEY=?";
            stmt = conn.prepareStatement(sql);

            // creating a batch request
            for (Scope scope : scopes) {
                stmt.setString(1, scope.getRoles());
                stmt.setString(2, scope.getKey());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new ScopeManagementDAOException("Error occurred while updating the details of the scopes.", e);
        } finally {
            ScopeManagementDAOUtil.cleanupResources(stmt, rs);
        }

    }


    public List<Scope> getAllScopes() throws ScopeManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Scope> scopes = new ArrayList<>();
        Scope scope;

        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM IDN_OAUTH2_SCOPE";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                scope = new Scope();
                scope.setKey(rs.getString("SCOPE_KEY"));
                scope.setName(rs.getString("NAME"));
                scope.setDescription(rs.getString("DESCRIPTION"));
                scope.setRoles(rs.getString("ROLES"));
                scopes.add(scope);
            }
            return scopes;
        } catch (SQLException e) {
            throw new ScopeManagementDAOException("Error occurred while fetching the details of the scopes.", e);
        } finally {
            ScopeManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private Connection getConnection() throws SQLException {
        return ScopeManagementDAOFactory.getConnection();
    }

}
