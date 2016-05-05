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
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterException;
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
    public int getTotalDeviceCount() throws SQLException {
        int totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public int getActiveDeviceCount() throws SQLException {
        int activeDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            activeDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getActiveDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return activeDeviceCount;
    }

    @Override
    public int getInactiveDeviceCount() throws SQLException {
        int inactiveDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            inactiveDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getInactiveDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return inactiveDeviceCount;
    }

    @Override
    public int getRemovedDeviceCount() throws SQLException {
        int removedDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            removedDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getRemovedDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return removedDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount() throws SQLException {
        int nonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            nonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getNonCompliantDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return nonCompliantDeviceCount;
    }

    @Override
    public int getUnmonitoredDeviceCount() throws SQLException {
        int unmonitoredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            unmonitoredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getUnmonitoredDeviceCount();
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return unmonitoredDeviceCount;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(PaginationRequest paginationRequest)
                                                            throws SQLException, InvalidParameterException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(paginationRequest);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public int getDeviceCount(Map<String, Object> filters) throws SQLException {
        int deviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDeviceCount(filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCount;
    }

    @Override
    public int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode, Map<String, Object> filters)
                                                                    throws SQLException, InvalidParameterException {
        int featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters) throws SQLException {
        Map<String, Integer> deviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                         Map<String, Object> filters) throws SQLException, InvalidParameterException {
        Map<String, Integer> featureNonCompliantDeviceCountsByPlatforms;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws SQLException {
        Map<String, Integer> deviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                         Map<String, Object> filters) throws SQLException, InvalidParameterException {
        Map<String, Integer> featureNonCompliantDeviceCountsByOwnershipTypes;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(Map<String, Object> filters,
                        PaginationRequest paginationRequest) throws InvalidParameterException, SQLException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDevicesWithDetails(filters, paginationRequest);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                            Map<String, Object> filters, PaginationRequest paginationRequest)
                                                                    throws InvalidParameterException, SQLException {
        PaginationResult paginationResult;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filters, paginationRequest);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters) throws SQLException {
        List<Map<String, Object>> devicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            devicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDevicesWithDetails(filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return devicesWithDetails;
    }

    @Override
    public List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                         Map<String, Object> filters) throws SQLException, InvalidParameterException {
        List<Map<String, Object>> featureNonCompliantDevicesWithDetails;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDevicesWithDetails = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filters);
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDevicesWithDetails;
    }

}
