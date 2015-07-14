/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDAOImpl implements DeviceDAO {

    private static final Log log = LogFactory.getLog(DeviceDAOImpl.class);

    @Override
    public int addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceId = -1;
        try {
            conn = this.getConnection();
            String sql =
                    "INSERT INTO DM_DEVICE(DESCRIPTION, NAME, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, TENANT_ID) " +
                            "VALUES (?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, device.getDescription());
            stmt.setString(2, device.getName());
            stmt.setInt(3, typeId);
            stmt.setString(4, device.getDeviceIdentifier());
            stmt.setInt(5, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                deviceId = rs.getInt(1);
            }
            return deviceId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device " +
                    "'" + device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int updateDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceId = -1;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE SET DESCRIPTION = ?, NAME = ? WHERE DEVICE_IDENTIFICATION = ? AND " +
                    "DEVICE_TYPE_ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, device.getDescription());
            stmt.setString(2, device.getName());
            stmt.setString(3, device.getDeviceIdentifier());
            stmt.setInt(4, typeId);
            stmt.setInt(5, tenantId);
            stmt.executeUpdate();

            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                deviceId = rs.getInt(1);
            }
            return deviceId;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device '" +
                    device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public int removeDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException {
        return 0;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Device device = null;
        try {
            conn = this.getConnection();
            String sql =
                    "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, d1.DEVICE_IDENTIFICATION, " +
                            "e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                            "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE " +
                            "t.NAME = ? AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?) d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                device = this.loadDevice(rs);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceId.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return device;
    }

    @Override
    public List<Device> getDevices(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, d1.DEVICE_IDENTIFICATION, " +
                    "e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME," +
                    "d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            devices = new ArrayList<Device>();
            while (rs.next()) {
                Device device = this.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getDevices(String type, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, d1.DEVICE_IDENTIFICATION," +
                    "e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, d.NAME, " +
                    "d.DEVICE_IDENTIFICATION, d.OWNER, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DM_DEVICE.DEVICE_TYPE_ID = t.ID AND t.NAME = ? AND d.TENANT_ID = ?) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?";
            stmt = conn.prepareStatement(selectDBQueryForType);
            stmt.setString(1, type);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            rs = stmt.executeQuery();
            devices = new ArrayList<Device>();
            while (rs.next()) {
                Device device = this.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type '" + type + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesOfUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, d.DEVICE_IDENTIFICATION" +
                            " e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e, (SELECT t.NAME AS DEVICE_TYPE, d.ID, d.DESCRIPTION, " +
                            "d.NAME, d.DEVICE_IDENTIFICATION FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.OWNER =? AND d.TENANT_ID = ?) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?");
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = this.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    username + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    private Connection getConnection() throws DeviceManagementDAOException {
        return DeviceManagementDAOFactory.getConnection();
    }

    /**
     * Get device count of all devices.
     *
     * @return device count
     * @throws DeviceManagementDAOException
     */
    @Override
    public int getDeviceCount(int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS DEVICE_COUNT FROM DM_DEVICE WHERE TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceCount = rs.getInt("DEVICE_COUNT");
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return deviceCount;
    }

    /**
     * Get the list of devices that matches with the given device name.
     *
     * @param deviceName Name of the device.
     * @param tenantId   Id of the current tenant
     * @return device list
     * @throws DeviceManagementDAOException
     */
    @Override
    public List<Device> getDevicesByName(String deviceName, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, d.DEVICE_IDENTIFICATION " +
                            "e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e, (SELECT d.ID AS DEVICE_ID, d.NAME, d.DESCRIPTION, " +
                            "t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                            "AND d.NAME LIKE ? AND d.TENANT_ID = ?) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?) d1 WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ?");
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            stmt.setInt(4, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = this.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches " +
                    "'" + deviceName + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public int addEnrollment(Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int enrolmentId = -1;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_ENROLMENT(DEVICE_ID, OWNER, OWNERSHIP, STATUS, " +
                    "DATE_OF_ENROLMENT, DATE_OF_LAST_UPDATE, TENANT_ID) VALUES(?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, device.getId());
            stmt.setString(2, device.getEnrolmentInfo().getOwner());
            stmt.setString(3, device.getEnrolmentInfo().getOwnership().toString());
            stmt.setString(4, device.getEnrolmentInfo().getStatus().toString());
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
            throw new DeviceManagementDAOException("Error occurred while adding enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public boolean setEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner, Status status,
                                      int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_ENROLMENT SET STATUS = ? WHERE DEVICE_ID = " +
                    "(SELECT d.ID FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND " +
                    "d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, status.toString());
            stmt.setString(2, deviceId.getId());
            stmt.setString(3, deviceId.getType());
            stmt.setInt(4, tenantId);
            stmt.setString(5, currentOwner);
            stmt.setInt(6, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while setting the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return true;
    }

    @Override
    public Status getEnrolmentStatus(DeviceIdentifier deviceId, String currentOwner,
                                     int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Status status = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT STATUS FROM DM_ENROLMENT WHERE DEVICE_ID = " +
                    "(SELECT d.ID FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND " +
                    "d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            stmt.setString(4, currentOwner);
            stmt.setInt(5, tenantId);

            rs = stmt.executeQuery();
            if (rs.next()) {
                status = Status.valueOf(rs.getString("STATUS"));
            }
            return status;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving the status of device enrolment", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public EnrolmentInfo getEnrolment(DeviceIdentifier deviceId, String currentOwner,
                                      int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        EnrolmentInfo enrolmentInfo = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT ID, DEVICE_ID, OWNER, OWNERSHIP, STATUS, DATE_OF_ENROLMENT, " +
                    "DATE_OF_LAST_UPDATE, TENANT_ID FROM DM_ENROLMENT WHERE DEVICE_ID = " +
                    "(SELECT d.ID FROM DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND" +
                    " d.DEVICE_IDENTIFICATION = ? AND t.NAME = ? AND d.TENANT_ID = ?) AND OWNER = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getId());
            stmt.setString(2, deviceId.getType());
            stmt.setInt(3, tenantId);
            stmt.setString(4, currentOwner);
            stmt.setInt(5, tenantId);
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

    private Device loadDevice(ResultSet rs) throws SQLException {
        Device device = new Device();
        device.setId(rs.getInt("DEVICE_ID"));
        device.setName(rs.getString("DEVICE_NAME"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setType(rs.getString("DEVICE_TYPE"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
        device.setEnrolmentInfo(this.loadEnrolment(rs));
        return device;
    }

    private EnrolmentInfo loadEnrolment(ResultSet rs) throws SQLException {
        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setOwner(rs.getString("OWNER"));
        enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
        enrolmentInfo.setDateOfEnrolment(rs.getTimestamp("DATE_OF_ENROLMENT").getTime());
        enrolmentInfo.setDateOfLastUpdate(rs.getTimestamp("DATE_OF_LAST_UPDATE").getTime());
        enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("STATUS")));
        return enrolmentInfo;
    }

    public List<Device> getDevicesByStatus(EnrolmentInfo.Status status, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_TYPE_ID, " +
                                         "d.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                                         "e.DATE_OF_ENROLMENT FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                                         "e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE FROM DM_ENROLMENT e WHERE TENANT_ID = ? " +
                                         "AND STATUS = ?) e, DM_DEVICE d WHERE DEVICE_ID = e.DEVICE_ID AND d.TENANT_ID = ?");
            stmt.setInt(1, tenantId);
            stmt.setString(2, status.toString());
            stmt.setInt(3, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = this.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to status " +
                                                   "'" + status + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }
}
