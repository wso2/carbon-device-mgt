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
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.*;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementation class of GadgetDataService.
 */
public class GadgetDataServiceImpl implements GadgetDataService {

    @Override
    public DeviceCountByGroup getDeviceCount(ExtendedFilterSet extendedFilterSet, String userName)
                                   throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException {
        DeviceCountByGroup filteredDeviceCount;
        try {

            GadgetDataServiceDAOFactory.openConnection();
            filteredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCount(extendedFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return filteredDeviceCount;
    }

    @Override
    public DeviceCountByGroup getFeatureNonCompliantDeviceCount(String featureCode, BasicFilterSet basicFilterSet, String userName)
                                   throws InvalidFeatureCodeValueException, DataAccessLayerException {
        DeviceCountByGroup featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(featureCode, basicFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public DeviceCountByGroup getTotalDeviceCount(String userName) throws DataAccessLayerException {
        DeviceCountByGroup totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount(userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByConnectivityStatuses(String userName) throws DataAccessLayerException {
        List<DeviceCountByGroup> deviceCountsByConnectivityStatuses;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByConnectivityStatuses = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getDeviceCountsByConnectivityStatuses(userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByConnectivityStatuses;
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByPotentialVulnerabilities(String userName) throws DataAccessLayerException {
        List<DeviceCountByGroup> deviceCountsByPotentialVulnerabilities;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPotentialVulnerabilities = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPotentialVulnerabilities(userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPotentialVulnerabilities;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount, String userName)
                            throws InvalidStartIndexValueException, InvalidResultCountValueException,
                            DataAccessLayerException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(startIndex, resultCount, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByPlatforms(ExtendedFilterSet extendedFilterSet, String userName)
                                         throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException {
        List<DeviceCountByGroup> deviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(extendedFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroup> getFeatureNonCompliantDeviceCountsByPlatforms(String featureCode,
                                         BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException,
                                         DataAccessLayerException {
        List<DeviceCountByGroup> featureNonCompliantDeviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByPlatforms(featureCode, basicFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public List<DeviceCountByGroup> getDeviceCountsByOwnershipTypes(ExtendedFilterSet extendedFilterSet, String userName)
                                         throws InvalidPotentialVulnerabilityValueException,
                                         DataAccessLayerException {
        List<DeviceCountByGroup> deviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(extendedFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public List<DeviceCountByGroup>
        getFeatureNonCompliantDeviceCountsByOwnershipTypes(String featureCode, BasicFilterSet basicFilterSet, String userName)
                                 throws InvalidFeatureCodeValueException, DataAccessLayerException {
        List<DeviceCountByGroup> featureNonCompliantDeviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByOwnershipTypes(featureCode, basicFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, int startIndex, int resultCount, String userName)
                            throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException,
                            InvalidStartIndexValueException, InvalidResultCountValueException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDevicesWithDetails(extendedFilterSet, startIndex, resultCount, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String featureCode, BasicFilterSet basicFilterSet,
                            int startIndex, int resultCount, String userName) throws InvalidFeatureCodeValueException,
                            DataAccessLayerException, InvalidStartIndexValueException,
                            InvalidResultCountValueException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(featureCode, basicFilterSet, startIndex, resultCount, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<DeviceWithDetails> getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, String userName)
                                     throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException {
        List<DeviceWithDetails> devicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            devicesWithDetails = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getDevicesWithDetails(extendedFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return devicesWithDetails;
    }

    @Override
    public List<DeviceWithDetails> getFeatureNonCompliantDevicesWithDetails(String featureCode,
                                     BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException,
                                     DataAccessLayerException {
        List<DeviceWithDetails> featureNonCompliantDevicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDevicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(featureCode, basicFilterSet, userName);
        } catch (SQLException e) {
            throw new DataAccessLayerException("Error in either opening a database connection or " +
                "accessing the database to fetch corresponding results.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDevicesWithDetails;
    }

}
