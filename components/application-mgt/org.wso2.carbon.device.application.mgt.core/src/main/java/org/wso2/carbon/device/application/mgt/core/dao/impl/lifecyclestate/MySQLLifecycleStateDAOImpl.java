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
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.common.Util;
import org.wso2.carbon.device.application.mgt.core.dao.impl.platform.AbstractPlatformDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLLifecycleStateDAOImpl extends AbstractLifecycleStateDAOImpl {

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

}