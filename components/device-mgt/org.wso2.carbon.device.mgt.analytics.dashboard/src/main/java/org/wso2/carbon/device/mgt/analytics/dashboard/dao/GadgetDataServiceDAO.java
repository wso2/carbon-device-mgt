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

package org.wso2.carbon.device.mgt.analytics.dashboard.dao;

import java.util.List;
import java.util.Map;

public interface GadgetDataServiceDAO {

    /**
     * Method to get total filtered device count from a particular tenant.
     *
     * @param filters List of filters to be applied in getting
     * total filtered device count.
     *
     * @return Total filtered device count.
     */
    int getTotalDeviceCount(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    int getFeatureNonCompliantDeviceCount(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    int getActiveDeviceCount() throws GadgetDataServiceDAOException;

    int getInactiveDeviceCount() throws GadgetDataServiceDAOException;

    int getRemovedDeviceCount() throws GadgetDataServiceDAOException;

    /**
     * Method to get non-compliant device count.
     *
     * @return Non-compliant device count.
     */
    @SuppressWarnings("unused")
    int getNonCompliantDeviceCount() throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    Map<String, Integer> getNonCompliantDeviceCountsByFeatures() throws GadgetDataServiceDAOException;

    /**
     * Method to get unmonitored device count.
     *
     * @return Unmonitored device count.
     */
    @SuppressWarnings("unused")
    int getUnmonitoredDeviceCount() throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters) throws GadgetDataServiceDAOException;

    @SuppressWarnings("unused")
    List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(Map<String, Object> filters) throws GadgetDataServiceDAOException;

}
