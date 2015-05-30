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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.dao.DeviceDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.dto.Device;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.common.Device.Status;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceDAOImpl implements DeviceDAO {


    private DataSource dataSource;
    private static final Log log = LogFactory.getLog(DeviceDAOImpl.class);

    public DeviceDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void addDevice(Device device) throws DeviceManagementDAOException {
        Connection conn = null;
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
            stmt.setString(5, device.getOwnerShip());
            stmt.setString(6, device.getStatus().toString());
            stmt.setInt(7, device.getDeviceTypeId());
            stmt.setString(8, device.getDeviceIdentificationId());
            stmt.setString(9, device.getOwnerId());
            stmt.setInt(10, device.getTenantId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device " +
                    "'" + device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public void updateDevice(Device device) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String sql =
                    "UPDATE DM_DEVICE SET STATUS=?, OWNER=? WHERE DEVICE_IDENTIFICATION=?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, device.getStatus().toString());
            stmt.setString(2, device.getOwnerId());
            stmt.setString(3, device.getDeviceIdentificationId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while enrolling device " +
                                                   "'" + device.getName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
    }

    @Override
    public void updateDeviceStatus(int deviceId, Status status) throws DeviceManagementDAOException {

    }

    @Override
    public void deleteDevice(int deviceId) throws DeviceManagementDAOException {

    }

    @Override
    public Device getDevice(int deviceId) throws DeviceManagementDAOException {
        return null;
    }

    @Override
    public Device getDevice(DeviceIdentifier deviceIdentifier) throws DeviceManagementDAOException {
        Connection conn = null;
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
            stmt.setString(1, deviceIdentifier.getType());
            stmt.setString(2, deviceIdentifier.getId());

            rs = stmt.executeQuery();
            if (rs.next()) {
                device = new Device();
                device.setId(rs.getInt("ID"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setName(rs.getString("NAME"));
                device.setDateOfEnrollment(rs.getLong("DATE_OF_ENROLLMENT"));
                device.setDateOfLastUpdate(rs.getLong("DATE_OF_LAST_UPDATE"));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnerShip(rs.getString("OWNERSHIP"));
                device.setStatus(Status.valueOf(rs.getString("STATUS")));
                device.setDeviceTypeId(rs.getInt("DEVICE_TYPE_ID"));
                device.setDeviceIdentificationId(rs.getString("DEVICE_IDENTIFICATION"));
                device.setOwnerId(rs.getString("OWNER"));
                device.setTenantId(rs.getInt("TENANT_ID"));
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while listing devices for type " +
                    "'" + deviceIdentifier.getType() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, rs);
        }
        return device;
    }

    @Override
    public List<Device> getDevices() throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Device> devicesList = null;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, " +
                                          "DATE_OF_LAST_UPDATE, OWNERSHIP, STATUS, DEVICE_TYPE_ID, " +
                                          "DEVICE_IDENTIFICATION, OWNER, TENANT_ID FROM DM_DEVICE ";
            stmt = conn.prepareStatement(selectDBQueryForType);
            resultSet = stmt.executeQuery();
            devicesList = new ArrayList<Device>();
            while (resultSet.next()) {
                Device device = new Device();
                device.setId(resultSet.getInt(1));
                device.setDescription(resultSet.getString(2));
                device.setName(resultSet.getString(3));
                device.setDateOfEnrollment(resultSet.getLong(4));
                device.setDateOfLastUpdate(resultSet.getLong(5));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnerShip(resultSet.getString(6));
                device.setStatus(Status.valueOf(resultSet.getString(7)));
                device.setDeviceTypeId(resultSet.getInt(8));
                device.setDeviceIdentificationId(resultSet.getString(9));
                device.setOwnerId(resultSet.getString(10));
                device.setTenantId(resultSet.getInt(11));
                devicesList.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing all devices for type ";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return devicesList;
    }

    @Override
    public List<Integer> getDeviceIds(List<DeviceIdentifier> devices) throws DeviceManagementDAOException {
        List<Integer> deviceIds = new ArrayList<Integer>();
        try {
            Connection conn = this.getConnection();
            String sql = "SELECT DISTINCT ID FROM DEVICE WHERE NAME IN (?) AND ID IN (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            //stmt.setArray(1, new java.sql.Date[0]);
            stmt.setString(2, "");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deviceIds.add(rs.getInt("ID"));
            }
            return deviceIds;
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving device ids", e);
        }
    }

    private String getDeviceNameString(List<DeviceIdentifier> devices) {
        StringBuilder sb = new StringBuilder();
        for (DeviceIdentifier device : devices) {
            sb.append(device.getId());
        }
        return sb.toString();
    }

    @Override
    public List<Device> getDevices(int type) throws DeviceManagementDAOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        List<Device> devicesList = null;
        try {
            conn = this.getConnection();
            String selectDBQueryForType = "SELECT ID, DESCRIPTION, NAME, DATE_OF_ENROLLMENT, DATE_OF_LAST_UPDATE, OWNERSHIP, STATUS, DEVICE_TYPE_ID, DEVICE_IDENTIFICATION, OWNER, TENANT_ID FROM DM_DEVICE " +
                    "WHERE DM_DEVICE.DEVICE_TYPE_ID = ?";
            stmt = conn.prepareStatement(selectDBQueryForType);
            stmt.setInt(1, type);
            resultSet = stmt.executeQuery();
            devicesList = new ArrayList<Device>();
            while (resultSet.next()) {
                Device device = new Device();
                device.setId(resultSet.getInt(1));
                device.setDescription(resultSet.getString(2));
                device.setName(resultSet.getString(3));
                device.setDateOfEnrollment(resultSet.getLong(4));
                device.setDateOfLastUpdate(resultSet.getLong(5));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnerShip(resultSet.getString(6));
                device.setStatus(Status.valueOf(resultSet.getString(7)));
                device.setDeviceTypeId(resultSet.getInt(8));
                device.setDeviceIdentificationId(resultSet.getString(9));
                device.setOwnerId(resultSet.getString(10));
                device.setTenantId(resultSet.getInt(11));
                devicesList.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while listing devices for type '" + type + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
        }
        return devicesList;
    }

    @Override public List<Device> getDeviceListOfUser(String username, int tenantId) throws DeviceManagementDAOException {
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            stmt = conn.prepareStatement(
                    "SELECT DM_DEVICE_TYPE.ID, DM_DEVICE_TYPE.NAME, DM_DEVICE.ID, DM_DEVICE.DESCRIPTION, " +
                        "DM_DEVICE.NAME, DM_DEVICE.DATE_OF_ENROLLMENT, DM_DEVICE.DATE_OF_LAST_UPDATE, " +
                            "DM_DEVICE.OWNERSHIP, DM_DEVICE.STATUS, DM_DEVICE.DEVICE_TYPE_ID, " +
                                "DM_DEVICE.DEVICE_IDENTIFICATION, DM_DEVICE.OWNER, DM_DEVICE.TENANT_ID FROM " +
                                    "DM_DEVICE, DM_DEVICE_TYPE WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID " +
                                        "AND DM_DEVICE.OWNER =? AND DM_DEVICE.TENANT_ID =?");
            stmt.setString(1, username);
            stmt.setInt(2, tenantId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Device device = new Device();
                DeviceType deviceType = new DeviceType();
                int id = resultSet.getInt(resultSet.getInt(1));
                deviceType.setId(id);
                deviceType.setName(resultSet.getString(2));
                device.setId(resultSet.getInt(3));
                device.setDescription(resultSet.getString(4));
                device.setName(resultSet.getString(5));
                device.setDateOfEnrollment(resultSet.getLong(6));
                device.setDateOfLastUpdate(resultSet.getLong(7));
                //TODO:- Ownership is not a enum in DeviceDAO
                device.setOwnerShip(resultSet.getString(8));
                device.setStatus(Status.valueOf(resultSet.getString(9)));
                device.setDeviceTypeId(resultSet.getInt(10));
                device.setDeviceIdentificationId(resultSet.getString(11));
                device.setOwnerId(resultSet.getString(12));
                device.setTenantId(resultSet.getInt(13));
                deviceList.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices belongs to " + username;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
        return deviceList;
    }

    private Connection getConnection() throws DeviceManagementDAOException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DeviceManagementDAOException(
                    "Error occurred while obtaining a connection from the device " +
                            "management metadata repository datasource", e);
        }
    }

    /**
     * Get device count of all devices.
     * @return device count
     * @throws DeviceManagementDAOException
     */
    @Override
    public int getDeviceCount() throws DeviceManagementDAOException {
        Connection conn = null;
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
            String msg = "Error occurred while getting count of devices";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, resultSet);
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
        Connection conn = this.getConnection();
        PreparedStatement stmt = null;
        List<Device> deviceList = new ArrayList<Device>();
        try {
            stmt = conn.prepareStatement(
                    "SELECT DM_DEVICE_TYPE.ID, DM_DEVICE_TYPE.NAME, DM_DEVICE.ID, DM_DEVICE.DESCRIPTION, " +
                    "DM_DEVICE.NAME, DM_DEVICE.DATE_OF_ENROLLMENT, DM_DEVICE.DATE_OF_LAST_UPDATE, " +
                    "DM_DEVICE.OWNERSHIP, DM_DEVICE.STATUS, DM_DEVICE.DEVICE_TYPE_ID, " +
                    "DM_DEVICE.DEVICE_IDENTIFICATION, DM_DEVICE.OWNER, DM_DEVICE.TENANT_ID FROM " +
                    "DM_DEVICE, DM_DEVICE_TYPE WHERE DM_DEVICE.DEVICE_TYPE_ID = DM_DEVICE_TYPE.ID " +
                    "AND DM_DEVICE.NAME LIKE ? AND DM_DEVICE.TENANT_ID =?");
            stmt.setString(1, deviceName + "%");
            stmt.setInt(2, tenantId);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                Device device = new Device();
                DeviceType deviceType = new DeviceType();
                int id = resultSet.getInt(resultSet.getInt(1));
                deviceType.setId(id);
                deviceType.setName(resultSet.getString(2));
                device.setId(resultSet.getInt(3));
                device.setDescription(resultSet.getString(4));
                device.setName(resultSet.getString(5));
                device.setDateOfEnrollment(resultSet.getLong(6));
                device.setDateOfLastUpdate(resultSet.getLong(7));
                device.setOwnerShip(resultSet.getString(8));
                device.setStatus(Status.valueOf(resultSet.getString(9)));
                device.setDeviceTypeId(resultSet.getInt(10));
                device.setDeviceIdentificationId(resultSet.getString(11));
                device.setOwnerId(resultSet.getString(12));
                device.setTenantId(resultSet.getInt(13));
                deviceList.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices that matches to '" + deviceName + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(conn, stmt, null);
        }
        return deviceList;
    }

}
