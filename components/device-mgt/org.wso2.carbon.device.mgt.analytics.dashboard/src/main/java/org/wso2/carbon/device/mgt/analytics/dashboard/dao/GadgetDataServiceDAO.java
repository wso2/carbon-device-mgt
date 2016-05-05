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

import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface GadgetDataServiceDAO {

    /**
     * Method to get total filtered device count from a particular tenant.
     *
     * @return Total filtered device count.
     */
    int getTotalDeviceCount() throws SQLException;

    /**
     * Method to get active device count from a particular tenant.
     *
     * @return active device count.
     */
    int getActiveDeviceCount() throws SQLException;

    /**
     * Method to get inactive device count from a particular tenant.
     *
     * @return inactive device count.
     */
    int getInactiveDeviceCount() throws SQLException;

    /**
     * Method to get removed device count from a particular tenant.
     *
     * @return removed device count.
     */
    int getRemovedDeviceCount() throws SQLException;

    /**
     * Method to get non-compliant device count from a particular tenant.
     *
     * @return Non-compliant device count.
     */
    int getNonCompliantDeviceCount() throws SQLException;

    /**
     * Method to get unmonitored device count from a particular tenant.
     *
     * @return Unmonitored device count.
     */
    int getUnmonitoredDeviceCount() throws SQLException;

    PaginationResult getNonCompliantDeviceCountsByFeatures(PaginationRequest paginationRequest)
            throws InvalidParameterException, SQLException;

    int getDeviceCount(Map<String, Object> filters) throws SQLException;

    int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                                          Map<String, Object> filters) throws InvalidParameterException, SQLException;

    Map<String, Integer> getDeviceCountsByPlatforms(Map<String, Object> filters) throws SQLException;

    Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                          Map<String, Object> filters) throws InvalidParameterException, SQLException;

    Map<String, Integer> getDeviceCountsByOwnershipTypes(Map<String, Object> filters) throws SQLException;

    Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                          Map<String, Object> filters) throws InvalidParameterException, SQLException;

    PaginationResult getDevicesWithDetails(Map<String, Object> filters,
                                  PaginationRequest paginationRequest) throws InvalidParameterException, SQLException;

    PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                    Map<String, Object> filters, PaginationRequest paginationRequest)
                                            throws InvalidParameterException, SQLException;

    List<Map<String, Object>> getDevicesWithDetails(Map<String, Object> filters) throws SQLException;

    List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                          Map<String, Object> filters) throws InvalidParameterException, SQLException;

}
