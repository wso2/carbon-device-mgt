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
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DetailedDeviceEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroupEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.FilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.DataAccessLayerException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementation class of GadgetDataService.
 */
public class GadgetDataServiceImpl implements GadgetDataService {

    @Override
    public DeviceCountByGroupEntry getDeviceCount(FilterSet filterSet)
                                                  throws InvalidParameterValueException, DataAccessLayerException {
        DeviceCountByGroupEntry filteredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            filteredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDeviceCount(filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return filteredDeviceCount;
    }

    @Override
    public DeviceCountByGroupEntry getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                             FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException {
        DeviceCountByGroupEntry featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public DeviceCountByGroupEntry getTotalDeviceCount() throws DataAccessLayerException {
        DeviceCountByGroupEntry totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount();
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByConnectivityStatuses() throws DataAccessLayerException {
        List<DeviceCountByGroupEntry> deviceCountsByConnectivityStatuses;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByConnectivityStatuses = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getDeviceCountsByConnectivityStatuses();
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByConnectivityStatuses;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPotentialVulnerabilities() throws DataAccessLayerException {
        List<DeviceCountByGroupEntry> deviceCountsByPotentialVulnerabilities;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPotentialVulnerabilities = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPotentialVulnerabilities();
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPotentialVulnerabilities;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                                     throws InvalidParameterValueException, DataAccessLayerException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(startIndex, resultCount);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByPlatforms(FilterSet filterSet)
                                                     throws InvalidParameterValueException, DataAccessLayerException {
        List<DeviceCountByGroupEntry> deviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                 FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException {
        List<DeviceCountByGroupEntry> featureNonCompliantDeviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroupEntry> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                                      throws InvalidParameterValueException, DataAccessLayerException {
        List<DeviceCountByGroupEntry> deviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public List<DeviceCountByGroupEntry>
        getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                 FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException {
        List<DeviceCountByGroupEntry> featureNonCompliantDeviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(FilterSet filterSet,
                     int startIndex, int resultCount) throws InvalidParameterValueException, DataAccessLayerException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDevicesWithDetails(filterSet, startIndex, resultCount);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                      FilterSet filterSet, int startIndex, int resultCount)
                                                      throws InvalidParameterValueException, DataAccessLayerException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet, startIndex, resultCount);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DetailedDeviceEntry> getDevicesWithDetails(FilterSet filterSet)
                                                      throws InvalidParameterValueException, DataAccessLayerException {
        List<DetailedDeviceEntry> devicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            devicesWithDetails = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getDevicesWithDetails(filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return devicesWithDetails;
    }

    @Override
    public List<DetailedDeviceEntry> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                 FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException {
        List<DetailedDeviceEntry> featureNonCompliantDevicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDevicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filterSet);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDevicesWithDetails;
    }

}
