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


package org.wso2.carbon.device.mgt.core.search.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.device.details.DeviceInfo;
import org.wso2.carbon.device.mgt.common.device.details.DeviceLocation;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.search.mgt.*;
import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAOException;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessorImpl implements Processor {
    private ApplicationDAO applicationDAO;
    private static final Log log = LogFactory.getLog(ProcessorImpl.class);
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;

    public ProcessorImpl() {
        applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
        deviceAccessAuthorizationService = DeviceManagementDataHolder.getInstance()
                .getDeviceAccessAuthorizationService();
        if (deviceAccessAuthorizationService == null) {
            String msg = "DeviceAccessAuthorization service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public List<Device> execute(SearchContext searchContext) throws SearchMgtException {

        if(!Utils.validateOperators(searchContext.getConditions())){
            throw new SearchMgtException("Invalid validator is provided.");
        }

        QueryBuilder queryBuilder = new QueryBuilderImpl();
        List<Device> generalDevices = new ArrayList<>();
        List<List<Device>> allANDDevices = new ArrayList<>();
        List<List<Device>> allORDevices = new ArrayList<>();
        List<Device> locationDevices = new ArrayList<>();
        try {
            DeviceManagementDAOFactory.openConnection();
            Map<String, List<QueryHolder>> queries = queryBuilder.buildQueries(searchContext.getConditions());


            if (queries.containsKey(Constants.GENERAL)) {
                generalDevices = searchDeviceDetailsTable(queries.get(Constants.GENERAL).get(0));
            }
            if (queries.containsKey(Constants.PROP_AND)) {
                for (QueryHolder queryHolder : queries.get(Constants.PROP_AND)) {
                    List<Device> andDevices = searchDeviceDetailsTable(queryHolder);
                    allANDDevices.add(andDevices);
                }
            }
            if (queries.containsKey(Constants.PROP_OR)) {
                for (QueryHolder queryHolder : queries.get(Constants.PROP_OR)) {
                    List<Device> orDevices = searchDeviceDetailsTable(queryHolder);
                    allORDevices.add(orDevices);
                }
            }
            if (queries.containsKey(Constants.LOCATION)) {
                locationDevices = searchDeviceDetailsTable(queries.get(Constants.LOCATION).get(0));
            }
        } catch (InvalidOperatorException e) {
            throw new SearchMgtException("Invalid operator was provided, so cannot execute the search.", e);
        } catch (SQLException e) {
            throw new SearchMgtException("Error occurred while managing database transactions.", e);
        } catch (SearchDAOException e) {
            throw new SearchMgtException("Error occurred while running the search operations.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }

        ResultSetAggregator aggregator = new ResultSetAggregatorImpl();

        Map<String, List<Device>> devices = new HashMap<>();

        devices.put(Constants.GENERAL, generalDevices);
        devices.put(Constants.PROP_AND, this.processANDSearch(allANDDevices));
        devices.put(Constants.PROP_OR, this.processORSearch(allORDevices));
        devices.put(Constants.LOCATION, locationDevices);

        List<Device> finalDevices = aggregator.aggregate(devices);
        finalDevices = authorizedDevices(finalDevices);
        this.setApplicationListOfDevices(finalDevices);
        return finalDevices;
    }

    /**
     * To get the authorized devices for a particular user
     *
     * @param devices Devices that satisfy search results
     * @return Devices that satisfy search results and authorized to be viewed by particular user
     */
    private List<Device> authorizedDevices(List<Device> devices) throws SearchMgtException {
        List<Device> filteredList = new ArrayList<>();
        try {
            for (Device device : devices) {
                DeviceIdentifier deviceIdentifier = new DeviceIdentifier(device.getDeviceIdentifier(),
                        device.getType());
                if (deviceAccessAuthorizationService != null && deviceAccessAuthorizationService
                        .isUserAuthorized(deviceIdentifier)) {
                    filteredList.add(device);
                }
            }
            return filteredList;
        } catch (DeviceAccessAuthorizationException e) {
            log.error("Error getting authorized search results for logged in user");
            throw new SearchMgtException(e);
        }
    }

    @Override
    public List<Device> getUpdatedDevices(long epochTime) throws SearchMgtException {

        if ((1 + (int) Math.floor(Math.log10(epochTime))) <= 10) {
            epochTime = epochTime * 1000;
        }
        QueryBuilder queryBuilder = new QueryBuilderImpl();
        try {
            QueryHolder query = queryBuilder.processUpdatedDevices(epochTime);
            DeviceManagementDAOFactory.openConnection();
            return searchDeviceDetailsTable(query);
        } catch (InvalidOperatorException e) {
            throw new SearchMgtException("Invalid operator was provided, so cannot execute the search.", e);
        } catch (SQLException e) {
            throw new SearchMgtException("Error occurred while managing database transactions.", e);
        } catch (SearchDAOException e) {
            throw new SearchMgtException("Error occurred while running the search operations for given time.", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }


    private List<Device> processANDSearch(List<List<Device>> deLists) {
        List<Device> deviceList = new ArrayList<>();
        List<Device> smallestDeviceList = this.findListWithLowestItems(deLists);
        List<Map<Integer, Device>> maps = this.convertDeviceListToMap(deLists);
        boolean valueExist = false;
        for (Device device : smallestDeviceList) {
            for (Map<Integer, Device> devices : maps) {
                if (devices.containsKey(device.getId())) {
                    valueExist = true;
                } else {
                    valueExist = false;
                    break;
                }
            }
            if (valueExist) {
                deviceList.add(device);
            }
        }
        return deviceList;
    }

    private List<Device> processORSearch(List<List<Device>> deLists) {
        List<Device> devices = new ArrayList<>();
        Map<Integer, Device> map = new HashMap<>();

        for (List<Device> list : deLists) {
            for (Device device : list) {
                if (!map.containsKey(device.getId())) {
                    map.put(device.getId(), device);
                    devices.add(device);
                }
            }
        }
        return devices;
    }

    private List<Device> findListWithLowestItems(List<List<Device>> deLists) {
        int size = 0;
        List<Device> devices = new ArrayList<>();
        for (List<Device> list : deLists) {
            if (size == 0) {
                size = list.size();
                devices = list;
            } else {
                if (list.size() < size) {
                    devices = list;
                }
            }
        }
        return devices;
    }

    private List<Map<Integer, Device>> convertDeviceListToMap(List<List<Device>> deLists) {
        List<Map<Integer, Device>> maps = new ArrayList<>();
        for (List<Device> devices : deLists) {
            Map<Integer, Device> deviceMap = new HashMap<>();

            for (Device device : devices) {
                deviceMap.put(device.getId(), device);
            }
            maps.add(deviceMap);
        }
        return maps;
    }

    private void setApplicationListOfDevices(List<Device> devices) throws SearchMgtException {
        try {
            DeviceManagementDAOFactory.openConnection();
            for (Device device : devices) {
                device.setApplications(applicationDAO.getInstalledApplications(device.getId()));
            }
        } catch (DeviceManagementDAOException e) {
            throw new SearchMgtException("Error occurred while fetching the Application List of devices ", e);
        } catch (SQLException e) {
            throw new SearchMgtException("Error occurred while opening a connection to the data source", e);
        } finally {
            DeviceManagementDAOFactory.closeConnection();
        }
    }

    private List<Device> searchDeviceDetailsTable(QueryHolder queryHolder) throws SearchDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Query : " + queryHolder.getQuery());
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = new ArrayList<>();
        Map<Integer, Integer> devs = new HashMap<>();
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(queryHolder.getQuery());

            int x = 1;
            ValueType[] types = queryHolder.getTypes();
            for (ValueType type : types) {
                if (type.getColumnType().equals(ValueType.columnType.STRING)) {
                    stmt.setString(x, type.getStringValue());
                    x++;
                } else if (type.getColumnType().equals(ValueType.columnType.INTEGER)) {
                    stmt.setInt(x, type.getIntValue());
                    x++;
                } else if (type.getColumnType().equals(ValueType.columnType.LONG)){
                    stmt.setLong(x, type.getLongValue());
                    x++;
                } else  if(type.getColumnType().equals(ValueType.columnType.DOUBLE)){
                    stmt.setDouble(x, type.getDoubleValue());
                    x++;
                }
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                if (!devs.containsKey(rs.getInt("ID"))) {
                    Device device = new Device();
                    device.setId(rs.getInt("ID"));
                    device.setDescription(rs.getString("DESCRIPTION"));
                    device.setName(rs.getString("NAME"));
                    device.setType(rs.getString("DEVICE_TYPE_NAME"));
                    device.setDeviceIdentifier(rs.getString("DEVICE_IDENTIFICATION"));

                    EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                    enrolmentInfo.setStatus(EnrolmentInfo.Status.valueOf(rs.getString("DE_STATUS")));
                    enrolmentInfo.setOwner(rs.getString("OWNER"));
                    enrolmentInfo.setOwnership(EnrolmentInfo.OwnerShip.valueOf(rs.getString("OWNERSHIP")));
                    device.setEnrolmentInfo(enrolmentInfo);

                    DeviceIdentifier identifier = new DeviceIdentifier();
                    identifier.setType(rs.getString("DEVICE_TYPE_NAME"));
                    identifier.setId(rs.getString("DEVICE_IDENTIFICATION"));

                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setAvailableRAMMemory(rs.getDouble("AVAILABLE_RAM_MEMORY"));
                    deviceInfo.setBatteryLevel(rs.getDouble("BATTERY_LEVEL"));
                    deviceInfo.setConnectionType(rs.getString("CONNECTION_TYPE"));
                    deviceInfo.setCpuUsage(rs.getDouble("CPU_USAGE"));
                    deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
                    deviceInfo.setExternalAvailableMemory(rs.getDouble("EXTERNAL_AVAILABLE_MEMORY"));
                    deviceInfo.setExternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                    deviceInfo.setInternalAvailableMemory(rs.getDouble("INTERNAL_AVAILABLE_MEMORY"));
                    deviceInfo.setInternalTotalMemory(rs.getDouble("EXTERNAL_TOTAL_MEMORY"));
                    deviceInfo.setOsVersion(rs.getString("OS_VERSION"));
                    deviceInfo.setOsBuildDate(rs.getString("OS_BUILD_DATE"));
                    deviceInfo.setPluggedIn(rs.getBoolean("PLUGGED_IN"));
                    deviceInfo.setSsid(rs.getString("SSID"));
                    deviceInfo.setTotalRAMMemory(rs.getDouble("TOTAL_RAM_MEMORY"));
                    deviceInfo.setVendor(rs.getString("VENDOR"));
                    deviceInfo.setUpdatedTime(new java.util.Date(rs.getLong("UPDATE_TIMESTAMP")));

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
                    deviceLocation.setUpdatedTime(new java.util.Date(rs.getLong("DL_UPDATED_TIMESTAMP")));

                    deviceInfo.setLocation(deviceLocation);
                    device.setDeviceInfo(deviceInfo);
                    devices.add(device);
                    devs.put(device.getId(), device.getId());
                }
            }
        } catch (SQLException e) {
            throw new SearchDAOException("Error occurred while aquiring the device details.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
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

    private List<Device> fillPropertiesOfDevices(List<Device> devices) throws SearchDAOException {
        if (devices.isEmpty()) {
            return null;
        }
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String query = "SELECT * FROM DM_DEVICE_INFO WHERE DEVICE_ID IN (";
            if (conn.getMetaData().getDatabaseProductName().contains("H2") || conn.getMetaData()
                    .getDatabaseProductName().contains("MySQL")) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < devices.size(); i++) {
                    builder.append("?,");
                }
                query += builder.deleteCharAt(builder.length() - 1).toString() + ") ORDER BY DEVICE_ID";
                stmt = conn.prepareStatement(query);
                for (int i = 0; i < devices.size(); i++) {
                    stmt.setInt(i + 1, devices.get(i).getId());
                }
            } else {
                query += "?) ORDER BY DEVICE_ID";
                stmt = conn.prepareStatement(query);
                Array array = conn.createArrayOf("INT", Utils.getArrayOfDeviceIds(devices));
                stmt.setArray(1, array);
            }
            rs = stmt.executeQuery();

            DeviceInfo dInfo;
            while (rs.next()) {
                dInfo = this.getDeviceInfo(devices, rs.getInt("DEVICE_ID"));
                dInfo.getDeviceDetailsMap().put(rs.getString("KEY_FIELD"), rs.getString("VALUE_FIELD"));
            }
        } catch (SQLException e) {
            throw new SearchDAOException("Error occurred while retrieving the device properties.", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    private DeviceInfo getDeviceInfo(List<Device> devices, int deviceId) {
        for (Device device : devices) {
            if (device.getId() == deviceId) {
                if (device.getDeviceInfo() == null) {
                    device.setDeviceInfo(new DeviceInfo());
                }
                return device.getDeviceInfo();
            }
        }
        return null;
    }
}