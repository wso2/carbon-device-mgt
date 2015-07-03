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
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.OwnerShip;
import org.wso2.carbon.device.mgt.common.app.mgt.Application;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.operation.mgt.ProfileOperation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDAOImpl implements DeviceDAO {

    private static final Log log = LogFactory.getLog(DeviceDAOImpl.class);

    @Override
    public void addDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql =
                    "INSERT INTO DM_DEVICE(DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, " +
                            "OWNERSHIP, STATUS, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, OWNER, TENANT_ID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, device.getDescription());
            stmt.setString(2, device.getName());
            stmt.setLong(3, new Date().getTime());
            stmt.setLong(4, new Date().getTime());
            stmt.setString(5, device.getEnrolmentInfo().getOwnership().toString());
            stmt.setString(6, device.getEnrolmentInfo().getStatus().toString());
            stmt.setInt(7, typeId);
            stmt.setString(8, device.getDeviceIdentifier());
            stmt.setString(9, device.getEnrolmentInfo().getOwner());
            stmt.setInt(10, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device " +
                    "'" + device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDevice(int typeId, Device device, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "UPDATE DM_DEVICE SET STATUS = ?, OWNER = ?, DATE_OF_ENROLLMENT=?, " +
                    "DATE_OF_LAST_UPDATE=? WHERE DEVICE_IDENTIFICATION = ? AND TENANT_ID = ? AND DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, device.getEnrolmentInfo().getStatus().toString());
            stmt.setString(2, device.getEnrolmentInfo().getOwner());
            stmt.setLong(3, device.getEnrolmentInfo().getDateOfEnrolment());
            stmt.setLong(4, device.getEnrolmentInfo().getDateOfLastUpdate());
            stmt.setString(5, device.getDeviceIdentifier());
            stmt.setInt(6, typeId);
            stmt.setInt(7, tenantId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device '" +
                    device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void updateDeviceStatus(DeviceIdentifier deviceId, Status status,
                                   int tenantId) throws DeviceManagementDAOException {

    }

    @Override
    public void deleteDevice(DeviceIdentifier deviceId, int tenantId) throws DeviceManagementDAOException {

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
                    "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, d.OWNERSHIP, d.STATUS, " +
                            "d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID, dt.NAME AS DEVICE_TYPE_NAME FROM DM_DEVICE d, DM_DEVICE_TYPE dt WHERE " +
                            "dt.NAME = ? AND d.DEVICE_IDENTIFICATION = ? AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());
            stmt.setInt(3, tenantId);
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
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, " +
                    "d.DATE_OF_LAST_UPDATE, d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                    "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID, t.NAME AS DEVICE_TYPE_NAME FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? ";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
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
            String selectDBQueryForType = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                    "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID,t.NAME AS DEVICE_TYPE_NAME FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DM_DEVICE.DEVICE_TYPE_ID = t.ID AND t.NAME = ? AND d.TENANT_ID = ?";
            stmt = conn.prepareStatement(selectDBQueryForType);
            stmt.setString(1, type);
            stmt.setInt(2, tenantId);
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
    public List<Device> getDeviceListOfUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT t.NAME AS DEVICE_TYPE_NAME, d.ID AS DEVICE_ID, d.DESCRIPTION, " +
                            "d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                            "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                            "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                            "AND d.OWNER =? AND d.TENANT_ID = ?");
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
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
     * @param deviceName    Name of the device.
     * @param tenantId      Id of the current tenant
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
                    "SELECT d.ID AS DEVICE_ID, d.NAME AS DEVICE_NAME, t.ID AS DEVICE_TYPE_ID, d.DESCRIPTION, " +
                            "t.NAME AS DEVICE_TYPE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                            "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                            "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                            "AND d.NAME LIKE ? AND d.TENANT_ID = ?");
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
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
    public void addDeviceApplications(int deviceId, Object appList) throws DeviceManagementDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql = "INSERT INTO DM_DEVICE_APPLICATIONS(DEVICE_ID, APPLICATIONS) " +
                         "VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            stmt.setObject(2, appList);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while update application list for device " +
                    "'" + deviceId + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public List<Application> getInstalledApplications(int deviceId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Application> applications = new ArrayList<Application>();
        Application application;
        ByteArrayInputStream bais;
        ObjectInputStream ois;

        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT DEVICE_ID, APPLICATIONS FROM DM_DEVICE_APPLICATIONS WHERE DEVICE_ID = ?");
            stmt.setInt(1, deviceId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                byte[] applicationDetails = rs.getBytes("APPLICATIONS");
                bais = new ByteArrayInputStream(applicationDetails);
                ois = new ObjectInputStream(bais);
                application = (Application) ois.readObject();
                applications.add(application);
            }

        }catch (IOException e) {
            String errorMsg = "IO Error occurred while de serialize the Application object";
            log.error(errorMsg, e);
            throw new DeviceManagementDAOException(errorMsg, e);
        } catch (ClassNotFoundException e) {
            String errorMsg = "Class not found error occurred while de serialize the Application object";
            log.error(errorMsg, e);
            throw new DeviceManagementDAOException(errorMsg, e);
        } catch (SQLException e) {
            String errorMsg = "SQL Error occurred while retrieving the list of Applications installed in device id '"
                              + deviceId;
            log.error(errorMsg, e);
            throw new DeviceManagementDAOException(errorMsg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return applications;
    }

    private Device loadDevice(ResultSet rs) throws SQLException {

        Device device = new Device();
        DeviceType deviceType = new DeviceType();
        deviceType.setId(rs.getInt("DEVICE_TYPE_ID"));
        deviceType.setName(rs.getString("DEVICE_TYPE_NAME"));
        device.setId(rs.getInt("DEVICE_ID"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setType(rs.getString("DEVICE_TYPE_NAME"));
        device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));

        EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
        enrolmentInfo.setDateOfEnrolment(rs.getLong("DATE_OF_ENROLLMENT"));
        enrolmentInfo.setDateOfLastUpdate(rs.getLong("DATE_OF_LAST_UPDATE"));
        enrolmentInfo.setOwnership(OwnerShip.valueOf(rs.getString("OWNERSHIP")));
        enrolmentInfo.setStatus(Status.valueOf(rs.getString("STATUS")));
        enrolmentInfo.setOwner(rs.getString("OWNER"));
        device.setEnrolmentInfo(enrolmentInfo);

        return device;
    }

}
