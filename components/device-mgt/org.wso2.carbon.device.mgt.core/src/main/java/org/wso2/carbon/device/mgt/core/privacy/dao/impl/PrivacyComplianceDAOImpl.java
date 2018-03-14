/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.device.mgt.core.privacy.dao.impl;

import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.privacy.dao.PrivacyComplianceDAO;
import org.wso2.carbon.device.mgt.core.privacy.dao.PrivacyComplianceDAOException;
import org.wso2.carbon.device.mgt.core.privacy.impl.DeviceEnrollmentMapping;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrivacyComplianceDAOImpl implements PrivacyComplianceDAO {

    @Override
    public List<DeviceEnrollmentMapping> getDevicesOfUser(String username, int tenantId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<DeviceEnrollmentMapping> deviceIds = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM DM_ENROLMENT WHERE OWNER = ? AND TENANT_ID = ? ORDER BY DEVICE_ID";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();

            while (rs.next()) {
                DeviceEnrollmentMapping mapping = new DeviceEnrollmentMapping();
                mapping.setDeviceId(rs.getInt("DEVICE_ID"));
                mapping.setEnrolmentId(rs.getInt("ENROLMENT_ID"));
                deviceIds.add(mapping);
            }
            if (deviceIds.isEmpty()) {
                return null;
            }
            return deviceIds;

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while retrieving device ids " +
                    "related to the given user.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteDevice(int deviceId, int tenantId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceEnrollments(int deviceId, int tenantId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device enrolments", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceEnrollments(int deviceId, int enrolmentId, int tenantId)
            throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND TENANT_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device enrolments", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceDetails(int deviceId, int enrolmentId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device details.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceApplications(int deviceId, int enrolmentId, int tenantId)
            throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_APPLICATION_MAPPING WHERE DEVICE_ID = ? " +
                    "AND ENROLMENT_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device applications.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceProperties(int deviceId, int enrolmentId, int tenantId)
            throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_INFO WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device information.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceLocation(int deviceId, int enrolmentId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_LOCATION WHERE DEVICE_ID = ? AND ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setInt(2, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device location.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDeviceOperationResponses(int enrolmentId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE_OPERATION_RESPONSE SET OPERATION_RESPONSE = ? WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setNull(1, java.sql.Types.BLOB);
            stmt.setInt(2, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device information.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }


    @Override
    public void deleteDeviceOperationDetails(int enrolmentId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_DEVICE_OPERATION_RESPONSE WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device information.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteOperationEnrolmentMappings(int enrolmentId) throws PrivacyComplianceDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "DELETE FROM DM_ENROLMENT_OP_MAPPING WHERE ENROLMENT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new PrivacyComplianceDAOException("Error occurred while deleting the device information.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }
}

