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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.Device.Status;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
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
            stmt.setString(5, device.getOwnership());
            stmt.setString(6, device.getStatus().toString());
            stmt.setInt(7, typeId);
            stmt.setString(8, device.getDeviceIdentifier());
            stmt.setString(9, device.getOwner());
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
            stmt.setString(1, device.getStatus().toString());
            stmt.setString(2, device.getOwner());
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
                    "SELECT d.ID, d.DESCRIPTION, d.NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, d.OWNERSHIP, d.STATUS, " +
                            "d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM DM_DEVICE d, DM_DEVICE_TYPE dt WHERE " +
                            "dt.NAME = ? AND d.DEVICE_IDENTIFICATION = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceId.getType());
            stmt.setString(2, deviceId.getId());

            rs = stmt.executeQuery();
            if (rs.next()) {
                device = new Device();
                device.setId(rs.getInt("ID"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setName(rs.getString("NAME"));
                device.setDateOfEnrolment(rs.getLong("DATE_OF_ENROLLMENT"));
                device.setDateOfLastUpdate(rs.getLong("DATE_OF_LAST_UPDATE"));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnership(rs.getString("OWNERSHIP"));
                device.setStatus(Status.valueOf(rs.getString("STATUS")));
                device.setDeviceType(deviceId.getType());
                device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
                device.setOwner(rs.getString("OWNER"));
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
        List<Device> devicesList = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, " +
                                          "d.DATE_OF_LAST_UPDATE, d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                                          "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID, t.NAME AS DEVICE_TYPE_NAME FROM DM_DEVICE d, DEVICE_TYPE t " +
                    "WHERE d.DEVICE_TYPE_ID = t.ID ";
            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();
            devicesList = new ArrayList<Device>();
            while (rs.next()) {
                Device device = new Device();
                device.setId(rs.getInt("DEVICE_ID"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setName(rs.getString("DEVICE_NAME"));
                device.setDateOfEnrolment(rs.getLong("DATE_OF_ENROLLMENT"));
                device.setDateOfLastUpdate(rs.getLong("DATE_OF_LAST_UPDATE"));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnership("OWNERSHIP");
                device.setStatus(Status.valueOf(rs.getString("STATUS")));
                device.setDeviceType("DEVICE_TYPE_NAME");
                device.setDeviceIdentifier("DEVICE_IDENTIFICATION");
                device.setOwner("OWNER");
                devicesList.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devicesList;
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
        ResultSet resultSet = null;
        List<Device> devicesList = null;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                    "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM DM_DEVICE d, DM_DEVICE_TYPE t " +
                    "WHERE d.DM_DEVICE.DEVICE_TYPE_ID = t.ID AND t.NAME = ?";
            stmt = conn.prepareStatement(selectDBQueryForType);
            stmt.setString(1, type);
            resultSet = stmt.executeQuery();
            devicesList = new ArrayList<Device>();
            while (resultSet.next()) {
                Device device = new Device();
                device.setId(resultSet.getInt("DEVICE_ID"));
                device.setDescription(resultSet.getString("DESCRIPTION"));
                device.setName(resultSet.getString("DEVICE_NAME"));
                device.setDateOfEnrolment(resultSet.getLong("DATE_OF_ENROLLMENT"));
                device.setDateOfLastUpdate(resultSet.getLong("DATE_OF_LAST_UPDATE"));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnership(resultSet.getString("OWNERSHIP"));
                device.setStatus(Status.valueOf(resultSet.getString("STATUS")));
                device.setDeviceType(type);
                device.setDeviceIdentifier(resultSet.getString("DEVICE_IDENTIFICATION"));
                device.setOwner(resultSet.getString("OWNER"));
                devicesList.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type '" + type + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, resultSet);
        }
        return devicesList;
    }

    @Override public List<Device> getDeviceListOfUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT t.NAME AS DEVICE_TYPE_NAME, d.ID AS DEVICE_ID, d.DESCRIPTION, " +
                        "d.NAME AS DEVICE_NAME, d.DATE_OF_ENROLLMENT, d.DATE_OF_LAST_UPDATE, " +
                            "d.OWNERSHIP, d.STATUS, d.DEVICE_TYPE_ID, " +
                                "d.DEVICE_IDENTIFICATION, d.OWNER, d.TENANT_ID FROM " +
                                    "DM_DEVICE d, DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID " +
                                        "AND d.OWNER =? AND d.TENANT_ID =?");
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Device device = new Device();
                device.setId(resultSet.getInt("DEVICE_ID"));
                device.setDescription(resultSet.getString("DESCRIPTION"));
                device.setName(resultSet.getString("DEVICE_NAME"));
                device.setDateOfEnrolment(resultSet.getLong("DATE_OF_ENROLLMENT"));
                device.setDateOfLastUpdate(resultSet.getLong("DATE_OF_LAST_UPDATE"));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnership(resultSet.getString("OWNERSHIP"));
                device.setStatus(Status.valueOf(resultSet.getString("STATUS")));
                device.setDeviceType("DEVICE_TYPE_NAME");
                device.setDeviceIdentifier("DEVICE_IDENTIFICATION");
                device.setOwner("OWNER");
                deviceList.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                    username + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceList;
    }

    private Connection getConnection() throws DeviceManagementDAOException {
        return DeviceManagementDAOFactory.getConnection();
    }

    /**
     * Get device count of all devices.
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
                deviceCount = resultSet.getInt(0);
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
    public List<Device> getDevicesByName(String deviceName, String type,
                                         int tenantId) throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(
                    "SELECT DM_DEVICE_TYPE.ID, DM_DEVICE_TYPE.NAME, DM_DEVICE.ID, DM_DEVICE.DESCRIPTION, " +
                    "DM_DEVICE.NAME, DM_DEVICE.DATE_OF_ENROLLMENT, DM_DEVICE.DATE_OF_LAST_UPDATE, " +
                    "DM_DEVICE.OWNERSHIP, DM_DEVICE.STATUS, DM_DEVICE.DEVICE_TYPE_ID, " +
                    "DM_DEVICE.DEVICE_IDENTIFICATION, DM_DEVICE.OWNER, DM_DEVICE.TENANT_ID FROM " +
                    "DM_DEVICE, DM_DEVICE_TYPE WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID " +
                    "AND DM_DEVICE.NAME LIKE ? AND DM_DEVICE.TENANT_ID =?");
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = new Device();
                DeviceType deviceType = new DeviceType();
                int id = rs.getInt(1);
                deviceType.setId(id);
                deviceType.setName(rs.getString(2));
                device.setId(rs.getInt(3));
                device.setDescription(rs.getString(4));
                device.setName(rs.getString(5));
                device.setDateOfEnrolment(rs.getLong(6));
                device.setDateOfLastUpdate(rs.getLong(7));
                device.setOwnership(rs.getString(8));
                device.setStatus(Status.valueOf(rs.getString(9)));
                device.setDeviceType(type);
                device.setDeviceIdentifier(rs.getString(11));
                device.setOwner(rs.getString(12));
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

}
