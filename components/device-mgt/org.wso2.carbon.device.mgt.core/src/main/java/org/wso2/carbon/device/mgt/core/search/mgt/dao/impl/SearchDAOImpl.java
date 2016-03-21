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


package org.wso2.carbon.device.mgt.core.search.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.device.details.DeviceWrapper;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAO;
import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAOException;
import org.wso2.carbon.device.mgt.core.search.mgt.impl.Utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class SearchDAOImpl implements SearchDAO {

    private static final Log log = LogFactory.getLog(SearchDAOImpl.class);

    @Override
    public List<DeviceWrapper> searchDeviceDetailsTable(String query) throws SearchDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Query : " + query);
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<DeviceWrapper> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {

                Device device = new Device();
                device.setId(rs.getInt("ID"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setName("NAME");
                device.setType(rs.getString("DEVICE_TYPE_NAME"));
                device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));

                DeviceIdentifier identifier = new DeviceIdentifier();
                identifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));

                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceId(rs.getInt("ID"));
                deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
                deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
                deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
                deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
                deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
                deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setIMEI(rs.getString("IMEI"));
                deviceInfo.setIMSI(rs.getString("IMSI"));
                deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setInternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setMobileSignalStrength(rs.getDouble("MOBILE_SIGNAL_STRENGTH"));
                deviceInfo.setOperator(rs.getString("OPERATOR"));
                deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
                deviceInfo.setSsid(rs.getString("SSID"));
                deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
                deviceInfo.setVendor(rs.getString("VENDOR"));

                DeviceLocation deviceLocation = new DeviceLocation();
                deviceLocation.setLatitude(rs.getDouble("LATITUDE"));
                deviceLocation.setLongitude(rs.getDouble("LONGITUDE"));
                deviceLocation.setStreet1(rs.getString("STREET1"));
                deviceLocation.setStreet2(rs.getString("STREET2"));
                deviceLocation.setCity(rs.getString("CITY"));
                deviceLocation.setState(rs.getString("STATE"));
                deviceLocation.setZip(rs.getString("ZIP"));
                deviceLocation.setCountry(rs.getString("COUNTRY"));
                deviceLocation.setDeviceId(rs.getInt("ID"));

                DeviceWrapper wrapper = new DeviceWrapper();
                wrapper.setDevice(device);
                wrapper.setDeviceInfo(deviceInfo);
                wrapper.setDeviceLocation(deviceLocation);
                wrapper.setDeviceIdentifier(identifier);
                devices.add(wrapper);

            }
        } catch (SQLException e) {
            throw new SearchDAOException("Error occurred while acquiring the device details.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

        this.fillPropertiesOfDevices(devices);

        if (log.isDebugEnabled()) {
            log.debug("Number of the device returned from the query : " + devices.size());
        }
        return devices;
    }

    @Override
    public List<DeviceWrapper> searchDevicePropertyTable(String query) throws SearchDAOException {

        if (log.isDebugEnabled()) {
            log.debug("Query : " + query);
        }

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<DeviceWrapper> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {

                Device device = new Device();
                device.setId(rs.getInt("ID"));
                device.setDescription(rs.getString("DESCRIPTION"));
                device.setName("NAME");
                device.setType(rs.getString("DEVICE_TYPE_NAME"));
                device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));

                DeviceIdentifier identifier = new DeviceIdentifier();
                identifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));

                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setDeviceId(rs.getInt("ID"));
                deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
                deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
                deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
                deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
                deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
                deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setIMEI(rs.getString("IMEI"));
                deviceInfo.setIMSI(rs.getString("IMSI"));
                deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
                deviceInfo.setInternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                deviceInfo.setMobileSignalStrength(rs.getDouble("MOBILE_SIGNAL_STRENGTH"));
                deviceInfo.setOperator(rs.getString("OPERATOR"));
                deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
                deviceInfo.setSsid(rs.getString("SSID"));
                deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
                deviceInfo.setVendor(rs.getString("VENDOR"));

                DeviceLocation deviceLocation = new DeviceLocation();
                deviceLocation.setLatitude(rs.getDouble("LATITUDE"));
                deviceLocation.setLongitude(rs.getDouble("LONGITUDE"));
                deviceLocation.setStreet1(rs.getString("STREET1"));
                deviceLocation.setStreet2(rs.getString("STREET2"));
                deviceLocation.setCity(rs.getString("CITY"));
                deviceLocation.setState(rs.getString("STATE"));
                deviceLocation.setZip(rs.getString("ZIP"));
                deviceLocation.setCountry(rs.getString("COUNTRY"));
                deviceLocation.setDeviceId(rs.getInt("ID"));

                DeviceWrapper wrapper = new DeviceWrapper();
                wrapper.setDevice(device);
                wrapper.setDeviceInfo(deviceInfo);
                wrapper.setDeviceLocation(deviceLocation);
                wrapper.setDeviceIdentifier(identifier);
                devices.add(wrapper);

            }
        } catch (SQLException e) {
            throw new SearchDAOException("Error occurred while aquiring the device details.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }

        this.fillPropertiesOfDevices(devices);

        if (log.isDebugEnabled()) {
            log.debug("Number of the device returned from the query : " + devices.size());
        }

        return devices;
    }


    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    private List<DeviceWrapper> fillPropertiesOfDevices(List<DeviceWrapper> devices) throws SearchDAOException {

        if (devices.isEmpty()) {
            return null;
        }

        Connection conn;
        PreparedStatement stmt;
        ResultSet rs;

        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_INFO WHERE DEVICE_ID IN (?) ORDER BY DEVICE_ID ;";
            stmt = conn.prepareStatement(query);
            if (conn.getMetaData().getDatabaseProductName().contains("H2")) {
                String inData = Utils.getDeviceIdsAsString(devices);
                stmt.setString(1, inData);
            } else {
                Array array = conn.createArrayOf("INT", Utils.getArrayOfDeviceIds(devices));
                stmt.setArray(1, array);
            }
            rs = stmt.executeQuery();

            int deviceId = 0;
            while (rs.next()) {
                DeviceInfo dInfo = new DeviceInfo();

                if (deviceId != rs.getInt("DEVICE_ID")) {
                    deviceId = rs.getInt("DEVICE_ID");
                    dInfo = this.getDeviceInfo(devices, deviceId);
                    if (dInfo != null) {
                        dInfo.getDeviceDetailsMap().put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
                    }
                } else {
                    dInfo.getDeviceDetailsMap().put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return devices;
    }

    private DeviceInfo getDeviceInfo(List<DeviceWrapper> devices, int deviceId) {

        for (DeviceWrapper dw : devices) {
            if (dw.getDevice().getId() == deviceId) {
                return dw.getDeviceInfo();
            }
        }
        return null;
    }

}

