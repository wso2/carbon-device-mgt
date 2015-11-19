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

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.EnrollmentDAO;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnrollmentDAOImpl implements EnrollmentDAO {

    @Override
    public int addEnrollment(int deviceId, EnrolmentInfo enrolmentInfo,
                             int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int enrolmentId = -1;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, OWNER, OWNERSHIP, STATUS, " +
                    "DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID) VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, deviceId);
            stmt.setString(2, enrolmentInfo.getOwner());
            stmt.setString(3, enrolmentInfo.getOwnership().toString());
            stmt.setString(4, enrolmentInfo.getStatus().toString());
            stmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            stmt.setTimestamp(6, new Timestamp(new Date().getTime()));
            stmt.setInt(7, tenantId);
            stmt.execute();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                enrolmentId = rs.getInt(1);
            }
            return enrolmentId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while adding enrolment configuration", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int updateEnrollment(int deviceId, EnrolmentInfo enrolmentInfo,
                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        int rows;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET OWNERSHIP = ?, STATUS = ?, " +
                    "DATE_OF_ENROLMENT = ?, DATE_OF_LAST_UPDATE = ? WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?" +
                         " AND ID = ?";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setString(1, enrolmentInfo.getOwnership().toString());
            stmt.setString(2, enrolmentInfo.getStatus().toString());
            stmt.setTimestamp(3, new Timestamp(enrolmentInfo.getDateOfEnrolment()));
            stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
            stmt.setInt(5, deviceId);
            stmt.setString(6, enrolmentInfo.getOwner());
            stmt.setInt(7, tenantId);
            stmt.setInt(8, enrolmentInfo.getId());
            rows = stmt.executeUpdate();

            if (rows > 0) {
                status = 1;
            }

            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating enrolment configuration", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int updateEnrollment(EnrolmentInfo enrolmentInfo) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET OWNERSHIP = ?, STATUS = ?, " +
                         "DATE_OF_ENROLMENT = ?, DATE_OF_LAST_UPDATE = ? WHERE ID = ?";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setString(1, enrolmentInfo.getOwnership().toString());
            stmt.setString(2, enrolmentInfo.getStatus().toString());
            stmt.setTimestamp(3, new Timestamp(enrolmentInfo.getDateOfEnrolment()));
            stmt.setTimestamp(4, new Timestamp(new Date().getTime()));
            stmt.setInt(5, enrolmentInfo.getId());
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while updating enrolment configuration", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }


    @Override
    public int removeEnrollment(int deviceId, String currentOwner,
                                int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int status = -1;
        try {
            conn = this.getConnection();
            String sql = "DELETE DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql, new String[] {"id"});
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                status = 1;
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while removing device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean setStatus(int deviceId, String currentOwner, EnrolmentInfo.Status status,
                             int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ? WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setInt(2, deviceId);
            stmt.setString(3, currentOwner);
            stmt.setInt(4, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public EnrolmentInfo.Status getStatus(int deviceId, String currentOwner,
                                          int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo.Status status = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT STATUS FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                status = EnrolmentInfo.Status.valueOf(rs.getString("STATUS"));
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getEnrollment(int deviceId, String currentOwner,
                                       int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, currentOwner);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                enrolmentInfo = this.loadEnrolment(rs);
            }
            return enrolmentInfo;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolment " +
                    "information of user '" + currentOwner + "' upon device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public List<EnrolmentInfo> getEnrollmentsOfUser(int deviceId, String user, int tenantId)
            throws DeviceManagementDAOException {
        List<EnrolmentInfo> enrolmentInfos = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, DATE_OF_ENROLMENT, " +
                         "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = ? AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setString(2, user);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                enrolmentInfo = this.loadEnrolment(rs);
                enrolmentInfos.add(enrolmentInfo);
            }
            return enrolmentInfos;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the enrolments " +
                                                   "information of user '" + user + "' upon device '" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    private EnrolmentInfo loadEnrolment(ResultSet rs) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner(rs.getString("OWNER"));
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
        enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
        enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        enrolmentInfo.setId(rs.getInt("ID"));
        return enrolmentInfo;
    }

}
