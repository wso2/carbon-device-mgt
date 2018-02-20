/*
*  Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PosgreSQL specific DAO implementation for Application Management Operations
 */
public class PostgreSQLApplicationDAOImpl extends AbstractApplicationDAOImpl{

    @Override
    public List<Integer> addApplications(List<Application> applications,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<Integer> applicationIds = new ArrayList<>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_APPLICATION (NAME, PLATFORM, " +
                    "CATEGORY, VERSION, TYPE, LOCATION_URL, IMAGE_URL, TENANT_ID,APP_PROPERTIES, " +
                    "APP_IDENTIFIER, MEMORY_USAGE, IS_ACTIVE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"id"});

            for (Application application : applications) {

                stmt.setString(1, application.getName());
                stmt.setString(2, application.getPlatform());
                stmt.setString(3, application.getCategory());
                stmt.setString(4, application.getVersion());
                stmt.setString(5, application.getType());
                stmt.setString(6, application.getLocationUrl());
                stmt.setString(7, application.getImageUrl());
                stmt.setInt(8, tenantId);

                // Removing the application properties saving from the application table.
                stmt.setBytes(9, null);

                stmt.setString(10, application.getApplicationIdentifier());

                // Removing the application memory
                stmt.setInt(11, 0);
                stmt.setBoolean(12, true);

                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    applicationIds.add(rs.getInt(1));
                }
            }
            return applicationIds;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding bulk application list", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}
