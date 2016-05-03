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
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataServiceException;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOException;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.GadgetDataServiceDAOFactory;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * To be updated...
 */
public class GadgetDataServiceImpl implements GadgetDataService {

    @Override
    public int getTotalDeviceCount() throws GadgetDataServiceException {
        int totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for total device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public int getActiveDeviceCount() throws GadgetDataServiceException {
        int activeDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            activeDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getActiveDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for active device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return activeDeviceCount;
    }

    @Override
    public int getInactiveDeviceCount() throws GadgetDataServiceException {
        int inactiveDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            inactiveDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getInactiveDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for inactive device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return inactiveDeviceCount;
    }

    @Override
    public int getRemovedDeviceCount() throws GadgetDataServiceException {
        int removedDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            removedDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getRemovedDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for removed device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return removedDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount() throws GadgetDataServiceException {
        int nonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            nonCompliantDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getNonCompliantDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for non-compliant device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return nonCompliantDeviceCount;
    }

    @Override
    public int getUnmonitoredDeviceCount() throws GadgetDataServiceException {
        int unmonitoredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            unmonitoredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getUnmonitoredDeviceCount();
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for unmonitored device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return unmonitoredDeviceCount;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(PaginationRequest paginationRequest)
                                                                  throws GadgetDataServiceException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(paginationRequest);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for non-compliant device counts by features.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public int getDeviceCount(Map<String, Object> filters) throws GadgetDataServiceException {
        int deviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDeviceCount(filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in calling DAO function for getting a filtered device count.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCount;
    }

    @Override
    public int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode, Map<String, Object> filters)
                                                 throws GadgetDataServiceException {
        int featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting a filtered device count, non compliant by a particular feature.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters)
                                                           throws GadgetDataServiceException {
        Map<String, Integer> deviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered device counts by platforms.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                                       Map<String, Object> filters) throws GadgetDataServiceException {
        Map<String, Integer> featureNonCompliantDeviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered device counts by platforms, non compliant by a particular feature.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters)
                                                                throws GadgetDataServiceException {
        Map<String, Integer> deviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered device counts by ownership types.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                                        Map<String, Object> filters) throws GadgetDataServiceException {
        Map<String, Integer> featureNonCompliantDeviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes =
                GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection.", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered device counts by ownership types, non compliant by a particular feature.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(Map<String, Object> filters,
                        PaginationRequest paginationRequest) throws GadgetDataServiceException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getDevicesWithDetails(filters, paginationRequest);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered devices with details when pagination is enabled.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                   Map<String, Object> filters, PaginationRequest paginationRequest) throws GadgetDataServiceException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filters, paginationRequest);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered devices with details, non compliant by feature when pagination is enabled.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters)
                                                           throws GadgetDataServiceException {
        List<Map<String, Object>> devicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            devicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDevicesWithDetails(filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered devices with details.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return devicesWithDetails;
    }

    @Override
    public List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                       Map<String, Object> filters) throws GadgetDataServiceException {
        List<Map<String, Object>> featureNonCompliantDevicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDevicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filters);
        } catch (SQLException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer " +
                "in opening database connection", e);
        } catch (GadgetDataServiceDAOException e) {
            throw new GadgetDataServiceException("Error occurred @ GadgetDataService layer in calling DAO function " +
                "for getting filtered devices with details, non compliant by feature.", e);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDevicesWithDetails;
    }

}
