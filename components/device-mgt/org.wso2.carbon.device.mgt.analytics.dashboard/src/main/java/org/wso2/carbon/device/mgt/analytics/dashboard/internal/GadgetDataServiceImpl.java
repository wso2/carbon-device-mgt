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

package org.wso2.carbon.device.mgt.analytics.dashboard.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.analytics.dashboard.GadgetDataService;
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
class GadgetDataServiceImpl implements GadgetDataService {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(GadgetDataServiceImpl.class);

    @Override
    public int getTotalDeviceCount() {
        int totalDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            totalDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getTotalDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            totalDeviceCount = -1;
            return totalDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return totalDeviceCount;
    }

    @Override
    public int getActiveDeviceCount() {
        int activeDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            activeDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getActiveDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            activeDeviceCount = -1;
            return activeDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return activeDeviceCount;
    }

    @Override
    public int getInactiveDeviceCount() {
        int inactiveDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            inactiveDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getInactiveDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            inactiveDeviceCount = -1;
            return inactiveDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return inactiveDeviceCount;
    }

    @Override
    public int getRemovedDeviceCount() {
        int removedDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            removedDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getRemovedDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            removedDeviceCount = -1;
            return removedDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return removedDeviceCount;
    }

    @Override
    public int getNonCompliantDeviceCount() {
        int nonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            nonCompliantDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getNonCompliantDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            nonCompliantDeviceCount = -1;
            return nonCompliantDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return nonCompliantDeviceCount;
    }

    @Override
    public int getUnmonitoredDeviceCount() {
        int unmonitoredDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            unmonitoredDeviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getUnmonitoredDeviceCount();
        } catch (GadgetDataServiceDAOException | SQLException e) {
            unmonitoredDeviceCount = -1;
            return unmonitoredDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return unmonitoredDeviceCount;
    }

    @Override
    public PaginationResult getNonCompliantDeviceCountsByFeatures(PaginationRequest paginationRequest) {
        PaginationResult paginationResult = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getNonCompliantDeviceCountsByFeatures(paginationRequest);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public int getDeviceCount(Map<String, Object> filters) {
        int deviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCount = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().getDeviceCount(filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            deviceCount = -1;
            return deviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCount;
    }

    @Override
    public int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode, Map<String, Object> filters) {
        int featureNonCompliantDeviceCount;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCount = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCount(nonCompliantFeatureCode, filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            featureNonCompliantDeviceCount = -1;
            return featureNonCompliantDeviceCount;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCount;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters) {
        Map<String, Integer> deviceCountsByPlatforms = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByPlatforms = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByPlatforms(filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                                                              Map<String, Object> filters) {
        Map<String, Integer> featureNonCompliantDeviceCountsByPlatforms = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByPlatforms = GadgetDataServiceDAOFactory.
                getGadgetDataServiceDAO().getFeatureNonCompliantDeviceCountsByPlatforms(nonCompliantFeatureCode, filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByPlatforms;
    }

    @Override
    public Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters) {
        Map<String, Integer> deviceCountsByOwnershipTypes = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            deviceCountsByOwnershipTypes = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                getDeviceCountsByOwnershipTypes(filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return deviceCountsByOwnershipTypes;
    }

    @Override
    public Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                                                                   Map<String, Object> filters) {
        Map<String, Integer> featureNonCompliantDeviceCountsByOwnershipTypes = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            featureNonCompliantDeviceCountsByOwnershipTypes =
                GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getFeatureNonCompliantDeviceCountsByOwnershipTypes(nonCompliantFeatureCode, filters);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return featureNonCompliantDeviceCountsByOwnershipTypes;
    }

    @Override
    public PaginationResult getDevicesWithDetails(Map<String, Object> filters, PaginationRequest paginationRequest) {
        PaginationResult paginationResult = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getDevicesWithDetails(filters, paginationRequest);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                            Map<String, Object> filters, PaginationRequest paginationRequest) {
        PaginationResult paginationResult = null;
        try {
            GadgetDataServiceDAOFactory.openConnection();
            paginationResult = GadgetDataServiceDAOFactory.getGadgetDataServiceDAO().
                    getFeatureNonCompliantDevicesWithDetails(nonCompliantFeatureCode, filters, paginationRequest);
        } catch (GadgetDataServiceDAOException | SQLException e) {
            return null;
        } finally {
            GadgetDataServiceDAOFactory.closeConnection();
        }
        return paginationResult;
    }

    @Override
    public List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters) {
        return null;
    }

    @Override
    public List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode, Map<String, Object> filters) {
        return null;
    }

}
