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

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.search.SearchContext;
import org.wso2.carbon.device.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.search.mgt.*;
import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAO;
import org.wso2.carbon.device.mgt.core.search.mgt.dao.SearchDAOException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessorImpl implements Processor {

    private SearchDAO searchDAO;
    private ApplicationDAO applicationDAO;

    public ProcessorImpl() {
        searchDAO = DeviceManagementDAOFactory.getSearchDAO();
        applicationDAO = DeviceManagementDAOFactory.getApplicationDAO();
    }

    @Override
    public List<Device> execute(SearchContext searchContext) throws SearchMgtException {

        QueryBuilder queryBuilder = new QueryBuilderImpl();
        List<Device> generalDevices = new ArrayList<>();
        List<List<Device>> allANDDevices = new ArrayList<>();
        List<List<Device>> allORDevices = new ArrayList<>();
        List<Device> locationDevices = new ArrayList<>();
        try {
            Map<String, List<String>> queries = queryBuilder.buildQueries(searchContext.getConditions());
            DeviceManagementDAOFactory.openConnection();

            if (queries.containsKey(Constants.GENERAL)) {
                generalDevices = searchDAO.searchDeviceDetailsTable(queries.get(Constants.GENERAL).get(0));
            }
            if (queries.containsKey(Constants.PROP_AND)) {
                for (String query : queries.get(Constants.PROP_AND)) {
                    List<Device> andDevices = searchDAO.searchDevicePropertyTable(query);
                    allANDDevices.add(andDevices);
                }
            }
            if (queries.containsKey(Constants.PROP_OR)) {
                for (String query : queries.get(Constants.PROP_OR)) {
                    List<Device> orDevices = searchDAO.searchDevicePropertyTable(query);
                    allORDevices.add(orDevices);
                }
            }
            if (queries.containsKey(Constants.LOCATION)) {
                locationDevices = searchDAO.searchDevicePropertyTable(
                        queries.get(Constants.LOCATION).get(0));
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
        this.setApplicationListOfDevices(finalDevices);
        return finalDevices;
    }

    @Override
    public List<Device> getUpdatedDevices(long epochTime) throws SearchMgtException {

        if((1 + (int)Math.floor(Math.log10(epochTime))) <=10 ) {
            epochTime = epochTime * 1000;
        }
        QueryBuilder queryBuilder = new QueryBuilderImpl();
        try {
           String query =  queryBuilder.processUpdatedDevices(epochTime);
            DeviceManagementDAOFactory.openConnection();
            return searchDAO.searchDeviceDetailsTable(query);
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

            for (Device device: devices) {
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

}

