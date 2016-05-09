/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.analytics.dashboard.impl;

import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.*;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;

/**
 * To be updated...
 */
public class GadgetDataServiceImpl implements GadgetDataService {

    @Override
    public DeviceCountByGroupEntry getDeviceCount(FilterSet filterSet) throws InvalidParameterValueException, SQLException {
        DeviceCountByGroupEntry filteredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            filteredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDeviceCount(filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return filteredDeviceCount;
    }

    @Override
    public DeviceCountByGroupEntry getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode, FilterSet filterSet)
                                                                  throws InvalidParameterValueException, SQLException {
        DeviceCountByGroupEntry featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public DeviceCountByGroupEntry getTotalDeviceCount() throws SQLException {
        DeviceCountByGroupEntry totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByConnectivityStatuses() throws SQLException {
        List<DeviceCountByGroupEntry> deviceCountsByConnectivityStatuses;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByConnectivityStatuses = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getDeviceCountsByConnectivityStatuses();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByConnectivityStatuses;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPotentialVulnerabilities() throws SQLException {
        List<DeviceCountByGroupEntry> deviceCountsByPotentialVulnerabilities;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPotentialVulnerabilities = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPotentialVulnerabilities();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPotentialVulnerabilities;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                                                  throws SQLException, InvalidParameterValueException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(startIndex, resultCount);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPlatforms(FilterSet filterSet)
                                                                  throws InvalidParameterValueException, SQLException {
        List<DeviceCountByGroupEntry> deviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                             FilterSet filterSet) throws InvalidParameterValueException, SQLException {
        List<DeviceCountByGroupEntry> featureNonCompliantDeviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                                                 throws InvalidParameterValueException, SQLException {
        List<DeviceCountByGroupEntry> deviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public List<DeviceCountByGroupEntry>
        getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                            FilterSet filterSet) throws SQLException, InvalidParameterValueException {
        List<DeviceCountByGroupEntry> featureNonCompliantDeviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(FilterSet filterSet,
                                int startIndex, int resultCount) throws InvalidParameterValueException, SQLException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDevicesWithDetails(filterSet, startIndex, resultCount);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                        FilterSet filterSet, int startIndex, int resultCount)
                                                                 throws InvalidParameterValueException, SQLException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet, startIndex, resultCount);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DetailedDeviceEntry> getDevicesWithDetails(FilterSet filterSet)
                                                           throws InvalidParameterValueException, SQLException {
        List<DetailedDeviceEntry> devicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            devicesWithDetails = GadgetDataServiceDAOFactory.
                    getGadgetDataServiceDAO().getDevicesWithDetails(filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return devicesWithDetails;
    }

    @Override
    public List<DetailedDeviceEntry> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                            FilterSet filterSet) throws InvalidParameterValueException, SQLException {
        List<DetailedDeviceEntry> featureNonCompliantDevicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDevicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDevicesWithDetails;
    }

}
