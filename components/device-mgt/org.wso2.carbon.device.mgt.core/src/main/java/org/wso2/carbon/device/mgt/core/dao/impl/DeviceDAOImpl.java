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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.Status;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo.OwnerShip;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDAOImpl implements DeviceDAO {

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
            String sql = "UPDATE DM_DEVICE SET STATUS=?, OWNER=? WHERE DEVICE_IDENTIFICATION = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, device.getEnrolmentInfo().getStatus().toString());
            stmt.setString(2, device.getEnrolmentInfo().getOwner());
            stmt.setString(3, device.getDeviceIdentifier());
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
                            "d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM DM_DEVICE d, DM_DEVICE_TYPE dt WHERE " +
                            "dt.NAME = ? AND d.DEVICE_IDENTIFICATION = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());
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
                    "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID, t.NAME AS DEVICE_TYPE_NAME FROM DM_DEVICE d, DEVICE_TYPE t " +
                    "WHERE d.DEVICE_TYPE_ID = t.ID ";
            stmt = conn.prepareStatement(sql);
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
    public List<Integer> getDeviceIds(List<DeviceIdentifier> devices,
                                      int tenantId) throws DeviceManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            List<Integer> deviceIds = new ArrayList<Integer>();
            Connection conn = this.getConnection();
            String sql = "SELECT DISTINCT ID FROM DEVICE WHERE NAME IN (?) AND ID IN (?)";
            stmt = conn.prepareStatement(sql);
            //stmt.setArray(1, new java.sql.Date[0]);
            stmt.setString(2, "");
            rs = stmt.executeQuery();
            while (rs.next()) {
                deviceIds.add(rs.getInt("ID"));
            }
            return deviceIds;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving device ids", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
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
                    "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DM_DEVICE.DEVICE_TYPE_ID = t.ID AND t.NAME = ?";
            stmt = conn.prepareStatement(selectDBQueryForType);
            stmt.setString(1, type);
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
                    "SELECT t.NAME AS DEVICE_TYPE, d.ID AS DEVICE_ID, d.DESCRIPTION, " +
                            "d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                            "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                            "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                            "AND d.OWNER =? AND d.TENANT_ID =?");
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
        ResultSet resultSet = null;
        int deviceCount = 0;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT COUNT(DM_DEVICE.ID) FROM DM_DEVICE";
            stmt = conn.prepareStatement(selectDBQueryForType);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                deviceCount = resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while getting the device count", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return deviceCount;
    }

    /**
     * Get the list of devices that matches with the given device name.
     *
     * @param deviceName Name of the device.
     * @param tenantId
     * @return device list
     * @throws DeviceManagementDAOException
     */
    @Override
    public List<Device> getDevicesByName(String deviceName, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT d.ID AS DEVICE_ID, d.NAME AS DEVICE_NAME, t.ID AS DEVICE_TYPE_ID, d.DESCRIPTION, " +
                            "t.NAME AS DEVICE_TYPE, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                            "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                            "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM " +
                            "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                            "AND d.NAME LIKE ? AND d.TENANT_ID = ?");
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = this.loadDevice(rs);
                deviceList.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches " +
                    "'" + deviceName + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceList;
    }

    private Device loadDevice(ResultSet rs) throws SQLException {
        Device device = new Device();
        DeviceType deviceType = new DeviceType();
        deviceType.setId(rs.getInt("ID"));
        deviceType.setName(rs.getString("DEVICE_NAME"));
        device.setId(rs.getInt("DEVICE_TYPE_ID"));
        device.setDescription(rs.getString("DESCRIPTION"));
        device.setType(rs.getString("DEVICE_TYPE"));
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
