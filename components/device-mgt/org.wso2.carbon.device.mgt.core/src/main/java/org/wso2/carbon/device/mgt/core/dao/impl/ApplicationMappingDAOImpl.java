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
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.DeviceApplicationMapping;
import org.wso2.carbon.device.mgt.core.dao.ApplicationMappingDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ApplicationMappingDAOImpl implements ApplicationMappingDAO {

    private static Log log = LogFactory.getLog(ApplicationMappingDAOImpl.class);

    @Override
    public int addApplicationMapping(int deviceId, int applicationId,
                                     int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int mappingId = -1;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_APPLICATION_MAPPING (DEVICE_ID, APPLICATION_ID, " +
                    "TENANT_ID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, deviceId);
            stmt.setInt(2, applicationId);
            stmt.setInt(3, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                mappingId = rs.getInt(1);
            }
            return mappingId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void addApplicationMappings(int deviceId, List<Integer> applicationIds,
                                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_APPLICATION_MAPPING (DEVICE_ID, APPLICATION_ID, " +
                    "TENANT_ID) VALUES (?, ?, ?)";

            conn.setAutoCommit(false);
            stmt = conn.prepareStatement(sql);

            for (int applicationId : applicationIds) {
                stmt.setInt(1, deviceId);
                stmt.setInt(2, applicationId);
                stmt.setInt(3, tenantId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mappings", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void removeApplicationMapping(int deviceId, List<Integer> appIdList,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_ID = ? AND " +
                    "APPLICATION_ID = ? AND TENANT_ID = ?";

            conn = this.getConnection();
            for (int appId : appIdList) {
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, deviceId);
                stmt.setInt(2, appId);
                stmt.setInt(3, tenantId);
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device application mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public int addDeviceApplicationMapping(DeviceApplicationMapping deviceApp) throws
            DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int mappingId = -1;
        try {
            conn = getConnection();
            String sql = "SELECT ID FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_IDENTIFIER = ? AND " +
                    "APPLICATION_UUID = ? AND VERSION_NAME = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceApp.getDeviceIdentifier());
            stmt.setString(2, deviceApp.getApplicationUUID());
            stmt.setString(3, deviceApp.getVersionName());
            rs = stmt.executeQuery();

            if (!rs.next()) {
                sql = "INSERT INTO DM_DEVICE_APPLICATION_MAPPING (DEVICE_IDENTIFIER, APPLICATION_UUID, VERSION_NAME," +
                        "INSTALLED) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql, new String[]{"id"});
                stmt.setString(1, deviceApp.getDeviceIdentifier());
                stmt.setString(2, deviceApp.getApplicationUUID());
                stmt.setString(3, deviceApp.getVersionName());
                stmt.setBoolean(4, deviceApp.isInstalled());
                stmt.executeUpdate();

                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    mappingId = rs.getInt(1);
                }
                return mappingId;
            } else {
                log.warn("Device[" + deviceApp.getDeviceIdentifier() + "] application[" + deviceApp.getApplicationUUID() + "] mapping already " +
                        "exists in the DB");
                return -1;
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping to DB", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void updateDeviceApplicationMapping(DeviceApplicationMapping deviceApp) throws
            DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "UPDATE DM_DEVICE_APPLICATION_MAPPING " +
                    "SET INSTALLED = ? " +
                    "WHERE DEVICE_IDENTIFIER = ? AND APPLICATION_UUID = ? AND VERSION_NAME = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setBoolean(1, deviceApp.isInstalled());
            stmt.setString(2, deviceApp.getDeviceIdentifier());
            stmt.setString(3, deviceApp.getApplicationUUID());
            stmt.setString(3, deviceApp.getVersionName());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping to DB", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<DeviceApplicationMapping> getApplicationsOfDevice(String deviceIdentifier, boolean installed) throws
            DeviceManagementDAOException {
        List<DeviceApplicationMapping> applications = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int mappingId = -1;
        try {
            conn = getConnection();
            String sql = "SELECT APPLICATION_UUID, VERSION_NAME FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_IDENTIFIER = ? AND INSTALLED = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceIdentifier);
            stmt.setBoolean(2, installed);
            rs = stmt.executeQuery();

            while (rs.next()) {
                DeviceApplicationMapping deviceApp = new DeviceApplicationMapping();
                deviceApp.setDeviceIdentifier(deviceIdentifier);
                deviceApp.setApplicationUUID(rs.getString(1));
                deviceApp.setVersionName(rs.getString(2));
                deviceApp.setInstalled(true);
                applications.add(deviceApp);
            }
            return applications;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding device application mapping to DB", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void removeApplicationMapping(DeviceApplicationMapping deviceApp) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_IDENTIFIER = ? AND " +
                    "APPLICATION_UUID = ? AND VERSION_NAME = ?";

            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceApp.getDeviceIdentifier());
            stmt.setString(2, deviceApp.getApplicationUUID());
            stmt.setString(3, deviceApp.getVersionName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device application mapping", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

}
