/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAOImpl implements ApplicationDAO {

    private static final Log log = LogFactory.getLog(ApplicationDAOImpl.class);

    @Override
    public int addApplication(Application application, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int applicationId = -1;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_APPLICATION (NAME, PACKAGE_NAME, PLATFORM, CATEGORY, " +
                    "VERSION, TYPE, LOCATION_URL, IMAGE_URL, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, application.getName());
            stmt.setString(2, application.getPackageName());
            stmt.setString(3, application.getPlatform());
            stmt.setString(4, application.getCategory());
            stmt.setString(5, application.getVersion());
            stmt.setString(6, application.getType());
            stmt.setString(7, application.getLocationUrl());
            stmt.setString(8, application.getImageUrl());
            stmt.setInt(9, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }
            return applicationId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding application '" +
                    application.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<Integer> addApplications(List<Application> applications,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<Integer> applicationIds = new ArrayList<Integer>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_APPLICATION (NAME, PACKAGE_NAME, PLATFORM, CATEGORY, " +
                    "VERSION, TYPE, LOCATION_URL, IMAGE_URL, TENANT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (Application application : applications) {
                stmt.setString(1, application.getName());
                stmt.setString(2, application.getPackageName());
                stmt.setString(3, application.getPlatform());
                stmt.setString(4, application.getCategory());
                stmt.setString(5, application.getVersion());
                stmt.setString(6, application.getType());
                stmt.setString(7, application.getLocationUrl());
                stmt.setString(8, application.getImageUrl());
                stmt.setInt(9, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();

            rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                applicationIds.add(rs.getInt(1));
            }
            return applicationIds;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding bulk application list", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public int removeApplication(String applicationName, int tenantId) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int applicationId = -1;
        try {
            conn = this.getConnection();
            conn.setAutoCommit(false);
            stmt = conn.prepareStatement("DELETE DM_APPLICATION WHERE NAME = ? AND TENANT_ID = ?");
            stmt.setString(1, applicationName);
            stmt.setInt(2, tenantId);
            stmt.execute();
            conn.commit();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                applicationId = rs.getInt(1);
            }
            return applicationId;
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                log.warn("Error occurred while roll-backing the transaction", e);
            }
            throw new DeviceManagementDAOException("Error occurred while removing application '" +
                    applicationName + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Application getApplication(String identifier, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Application application = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT ID, NAME, PACKAGE_NAME, CATEGORY, PLATFORM, TYPE, VERSION, IMAGE_URL, " +
                    "LOCATION_URL FROM DM_APPLICATION WHERE PACKAGE_NAME = ? AND TENANT_ID = ?");
            stmt.setString(1, identifier);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                application = this.loadApplication(rs);
            }
            return application;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving application application '" +
                    identifier + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private Connection getConnection() throws DeviceManagementDAOException {
        return DeviceManagementDAOFactory.getConnection();
    }

    private Application loadApplication(ResultSet rs) throws SQLException {
        Application application = new Application();
        application.setId(rs.getInt("ID"));
        application.setName(rs.getString("NAME"));
        application.setPackageName(rs.getString("PACKAGE_NAME"));
        application.setCategory(rs.getString("CATEGORY"));
        application.setType(rs.getString("TYPE"));
        application.setVersion(rs.getString("VERSION"));
        application.setImageUrl(rs.getString("IMAGE_URL"));
        application.setLocationUrl(rs.getString("LOCATION_URL"));
        application.setPlatform(rs.getString("PLATFORM"));
        return application;
    }

}
