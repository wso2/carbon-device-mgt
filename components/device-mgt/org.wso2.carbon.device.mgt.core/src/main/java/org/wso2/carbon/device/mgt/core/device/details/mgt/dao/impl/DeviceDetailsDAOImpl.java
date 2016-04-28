/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.device.mgt.core.device.details.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import org.wso2.carbon.device.mgt.core.device.details.mgt.dao.DeviceDetailsMgtDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DeviceDetailsDAOImpl implements DeviceDetailsDAO {

    private static Log log = LogFactory.getLog(DeviceDetailsDAOImpl.class);

    @Override
    public void addDeviceInformation(DeviceInfo deviceInfo) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();

            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_DETAIL (DEVICE_ID, DEVICE_MODEL, " +
                    "VENDOR, OS_VERSION, BATTERY_LEVEL, INTERNAL_TOTAL_MEMORY, INTERNAL_AVAILABLE_MEMORY, " +
                    "EXTERNAL_TOTAL_MEMORY, EXTERNAL_AVAILABLE_MEMORY,  CONNECTION_TYPE, " +
                    "SSID, CPU_USAGE, TOTAL_RAM_MEMORY, AVAILABLE_RAM_MEMORY, PLUGGED_IN) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            stmt.setInt(1, deviceInfo.getDeviceId());
            stmt.setString(2, deviceInfo.getDeviceModel());
            stmt.setString(3, deviceInfo.getVendor());
            stmt.setString(4, deviceInfo.getOsVersion());
            stmt.setDouble(5, deviceInfo.getBatteryLevel());
            stmt.setDouble(6, deviceInfo.getInternalTotalMemory());
            stmt.setDouble(7, deviceInfo.getInternalAvailableMemory());
            stmt.setDouble(8, deviceInfo.getExternalTotalMemory());
            stmt.setDouble(9, deviceInfo.getExternalAvailableMemory());
            stmt.setString(10, deviceInfo.getConnectionType());
            stmt.setString(11, deviceInfo.getSsid());
            stmt.setDouble(12, deviceInfo.getCpuUsage());
            stmt.setDouble(13, deviceInfo.getTotalRAMMemory());
            stmt.setDouble(14, deviceInfo.getAvailableRAMMemory());
            stmt.setBoolean(15, deviceInfo.isPluggedIn());

            stmt.execute();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while inserting device details to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public void addDeviceProperties(Map<String, String> propertyMap, int deviceId) throws DeviceDetailsMgtDAOException {

        if (propertyMap.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("Property map of device id :" + deviceId + " is empty.");
            }
            return;
        }
        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_INFO (DEVICE_ID, KEY_FIELD, VALUE_FIELD) VALUES (?, ?, ?)");

            for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
                stmt.setInt(1, deviceId);
                stmt.setString(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while inserting device properties to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

    }

    @Override
    public DeviceInfo getDeviceInformation(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceInfo deviceInfo = new DeviceInfo();
        try {
            conn = this.getConnection();

            String sql = "SELECT * FROM  DM_DEVICE_DETAIL WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                deviceInfo.setDeviceId(rs.getInt("DEVICE_ID"));
//                deviceInfo.setIMEI(rs.getString("IMEI"));
//                deviceInfo.setIMSI(rs.getString("IMSI"));
                deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
                deviceInfo.setVendor(rs.getString("VENDOR"));
                deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
                deviceInfo.setInternalTotalMemory(rs.getDouble("INTERNAL_TOTAL_MEMORY"));
                deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
//                deviceInfo.setOperator(rs.getString("OPERATOR"));
                deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
//                deviceInfo.setMobileSignalStrength(rs.getDouble("MOBILE_SIGNAL_STRENGTH"));
                deviceInfo.setSsid(rs.getString("SSID"));
                deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
                deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
                deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
                deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
            }

            deviceInfo.setDeviceId(deviceId);
            return deviceInfo;
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the details of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public Map<String, String> getDeviceProperties(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM  DM_DEVICE_INFO WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                map.put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
            }
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the properties of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return map;
    }

    @Override
    public void deleteDeviceInformation(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_DETAIL WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device information from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void deleteDeviceProperties(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_INFO WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device properties from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public void addDeviceLocation(DeviceLocation deviceLocation) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement("INSERT INTO DM_DEVICE_LOCATION (DEVICE_ID, LATITUDE, LONGITUDE, STREET1, " +
                    "STREET2, CITY, ZIP, STATE, COUNTRY) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, deviceLocation.getDeviceId());
            stmt.setDouble(2, deviceLocation.getLatitude());
            stmt.setDouble(3, deviceLocation.getLongitude());
            stmt.setString(4, deviceLocation.getStreet1());
            stmt.setString(5, deviceLocation.getStreet2());
            stmt.setString(6, deviceLocation.getCity());
            stmt.setString(7, deviceLocation.getZip());
            stmt.setString(8, deviceLocation.getState());
            stmt.setString(9, deviceLocation.getCountry());
            stmt.execute();
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while adding the device location to database.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public DeviceLocation getDeviceLocation(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DeviceLocation location = new DeviceLocation();
        try {
            conn = this.getConnection();
            String sql = "SELECT * FROM  DM_DEVICE_LOCATION WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, deviceId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                location.setDeviceId(deviceId);
                location.setLatitude(rs.getDouble("LATITUDE"));
                location.setLongitude(rs.getDouble("LONGITUDE"));
                location.setStreet1(rs.getString("STREET1"));
                location.setStreet2(rs.getString("STREET2"));
                location.setCity(rs.getString("CITY"));
                location.setZip(rs.getString("ZIP"));
                location.setState(rs.getString("STATE"));
                location.setCountry(rs.getString("COUNTRY"));
            }
            location.setDeviceId(deviceId);

            return location;
        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while fetching the location of the registered devices.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
    }

    @Override
    public void deleteDeviceLocation(int deviceId) throws DeviceDetailsMgtDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            String query = "DELETE FROM DM_DEVICE_LOCATION WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, deviceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new DeviceDetailsMgtDAOException("Error occurred while deleting the device location from the data base.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }


//    @Override
//    public void addDeviceApplications(DeviceApplication deviceApplication) throws DeviceDetailsMgtDAOException {
//
//    }
//
//    @Override
//    public DeviceApplication getDeviceApplications(int deviceId) throws DeviceDetailsMgtDAOException {
//        return null;
//    }
//
//    @Override
//    public void deleteDeviceApplications(int deviceId) throws DeviceDetailsMgtDAOException {
//
//    }
}

