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

import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAO;


public class SearchDAOImpl implements SearchDAO {
//
//    private static final Log log = LogFactory.getLog(SearchDAOImpl.class);
//
//    @Override
//    public List<Device> searchDeviceDetailsTable(String query) throws SearchDAOException {
//        if (log.isDebugEnabled()) {
//            log.debug("Query : " + query);
//        }
//        Connection conn;
//        PreparedStatement stmt = null;
//        ResultSet rs;
//        List<Device> devices = new ArrayList<>();
//        Map<Integer, Integer> devs = new HashMap<>();
//        try {
//            conn = this.getConnection();
//            stmt = conn.prepareStatement(query);
//            rs = stmt.executeQuery();
//            while (rs.next()) {
//                if (!devs.containsKey(rs.getInt("ID"))) {
//                    Device device = new Device();
//                    device.setId(rs.getInt("ID"));
//                    device.setDescription(rs.getString("DESCRIPTION"));
//                    device.setName("NAME");
//                    device.setType(rs.getString("DEVICE_TYPE_NAME"));
//                    device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
//
//                    DeviceIdentifier identifier = new DeviceIdentifier();
//                    identifier.setType(rs.getString("DEVICE_TYPE_NAME"));
//                    identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
//
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
//                    deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
//                    deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
//                    deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
//                    deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
//                    deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
//                    deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
//                    deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
//                    deviceInfo.setInternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
//                    deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
//                    deviceInfo.setOsBuildDate(rs.getString("OS_BUILD_DATE"));
//                    deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
//                    deviceInfo.setSsid(rs.getString("SSID"));
//                    deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
//                    deviceInfo.setVendor(rs.getString("VENDOR"));
//                    deviceInfo.setUpdatedTime(new java.util.Date(rs.getLong("UPDATE_TIMESTAMP")));
//
//                    DeviceLocation deviceLocation = new DeviceLocation();
//                    deviceLocation.setLatitude(rs.getDouble("LATITUDE"));
//                    deviceLocation.setLongitude(rs.getDouble("LONGITUDE"));
//                    deviceLocation.setStreet1(rs.getString("STREET1"));
//                    deviceLocation.setStreet2(rs.getString("STREET2"));
//                    deviceLocation.setCity(rs.getString("CITY"));
//                    deviceLocation.setState(rs.getString("STATE"));
//                    deviceLocation.setZip(rs.getString("ZIP"));
//                    deviceLocation.setCountry(rs.getString("COUNTRY"));
//                    deviceLocation.setDeviceId(rs.getInt("ID"));
//                    deviceLocation.setUpdatedTime(new java.util.Date(rs.getLong("DL_UPDATED_TIMESTAMP")));
//
//                    deviceInfo.setLocation(deviceLocation);
//                    device.setDeviceInfo(deviceInfo);
//                    devices.add(device);
//                    devs.put(device.getId(), device.getId());
//                }
//            }
//        } catch (SQLException e) {
//            throw new SearchDAOException("Error occurred while acquiring the device details.", e);
//        } finally {
//            DeviceManagementDAOUtil.cleanupResources(stmt, null);
//        }
//
//        this.fillPropertiesOfDevices(devices);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Number of the device returned from the query : " + devices.size());
//        }
//        return devices;
//    }
//
//    @Override
//    public List<Device> searchDevicePropertyTable(String query) throws SearchDAOException {
//        if (log.isDebugEnabled()) {
//            log.debug("Query : " + query);
//        }
//
//        Connection conn;
//        PreparedStatement stmt = null;
//        ResultSet rs;
//        List<Device> devices = new ArrayList<>();
//        Map<Integer, Integer> devs = new HashMap<>();
//        try {
//            conn = this.getConnection();
//            stmt = conn.prepareStatement(query);
//            rs = stmt.executeQuery();
//            while (rs.next()) {
//                if (!devs.containsKey(rs.getInt("ID"))) {
//                    Device device = new Device();
//                    device.setId(rs.getInt("ID"));
//                    device.setDescription(rs.getString("DESCRIPTION"));
//                    device.setName(rs.getString("NAME"));
//                    device.setType(rs.getString("DEVICE_TYPE_NAME"));
//                    device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));
//
//                    DeviceIdentifier identifier = new DeviceIdentifier();
//                    identifier.setType(rs.getString("DEVICE_TYPE_NAME"));
//                    identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));
//
//                    DeviceInfo deviceInfo = new DeviceInfo();
//                    deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
//                    deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
//                    deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
//                    deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
//                    deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
//                    deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
//                    deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
//                    deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
//                    deviceInfo.setInternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
//                    deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
//                    deviceInfo.setOsBuildDate(rs.getString("OS_BUILD_DATE"));
//                    deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
//                    deviceInfo.setSsid(rs.getString("SSID"));
//                    deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
//                    deviceInfo.setVendor(rs.getString("VENDOR"));
//                    deviceInfo.setUpdatedTime(new java.util.Date(rs.getLong("UPDATE_TIMESTAMP")));
//
//                    DeviceLocation deviceLocation = new DeviceLocation();
//                    deviceLocation.setLatitude(rs.getDouble("LATITUDE"));
//                    deviceLocation.setLongitude(rs.getDouble("LONGITUDE"));
//                    deviceLocation.setStreet1(rs.getString("STREET1"));
//                    deviceLocation.setStreet2(rs.getString("STREET2"));
//                    deviceLocation.setCity(rs.getString("CITY"));
//                    deviceLocation.setState(rs.getString("STATE"));
//                    deviceLocation.setZip(rs.getString("ZIP"));
//                    deviceLocation.setCountry(rs.getString("COUNTRY"));
//                    deviceLocation.setDeviceId(rs.getInt("ID"));
//                    deviceLocation.setUpdatedTime(new java.util.Date(rs.getLong("DL_UPDATED_TIMESTAMP")));
//
//                    deviceInfo.setLocation(deviceLocation);
//                    device.setDeviceInfo(deviceInfo);
//                    devices.add(device);
//                    devs.put(device.getId(), device.getId());
//                }
//
//            }
//        } catch (SQLException e) {
//            throw new SearchDAOException("Error occurred while aquiring the device details.", e);
//        } finally {
//            DeviceManagementDAOUtil.cleanupResources(stmt, null);
//        }
//
//        this.fillPropertiesOfDevices(devices);
//
//        if (log.isDebugEnabled()) {
//            log.debug("Number of the device returned from the query : " + devices.size());
//        }
//
//        return devices;
//    }
//
//
//    private Connection getConnection() throws SQLException {
//        return DeviceManagementDAOFactory.getConnection();
//    }
//
//    private List<Device> fillPropertiesOfDevices(List<Device> devices) throws SearchDAOException {
//        if (devices.isEmpty()) {
//            return null;
//        }
//
//        Connection conn;
//        PreparedStatement stmt;
//        ResultSet rs;
//
//        try {
//            conn = this.getConnection();
//            String query = "SELECT * FROM DM_DEVICE_INFO WHERE DEVICE_ID IN (?) ORDER BY DEVICE_ID ;";
//            stmt = conn.prepareStatement(query);
//            if (conn.getMetaData().getDatabaseProductName().contains("H2") ||
//                    conn.getMetaData().getDatabaseProductName().contains("MySQL")) {
//                String inData = Utils.getDeviceIdsAsString(devices);
//                stmt.setString(1, inData);
//            } else {
//                Array array = conn.createArrayOf("INT", Utils.getArrayOfDeviceIds(devices));
//                stmt.setArray(1, array);
//            }
//            rs = stmt.executeQuery();
//
//            DeviceInfo dInfo;
//            while (rs.next()) {
//                dInfo = this.getDeviceInfo(devices, rs.getInt("DEVICE_ID"));
//                dInfo.getDeviceDetailsMap().put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
//            }
//        } catch (SQLException e) {
//            throw new SearchDAOException("Error occurred while retrieving the device properties.", e);
//        }
//        return devices;
//    }
//
//    private DeviceInfo getDeviceInfo(List<Device> devices, int deviceId) {
//        for (Device device : devices) {
//            if (device.getId() == deviceId) {
//                if (device.getDeviceInfo() == null) {
//                    device.setDeviceInfo(new DeviceInfo());
//                }
//                return device.getDeviceInfo();
//            }
//        }
//        return null;
//    }

}

