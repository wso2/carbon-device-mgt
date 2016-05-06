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

import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.FilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterValueException;
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

    PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                          throws InvalidParameterValueException, SQLException;

    int getDeviceCount(FilterSet filterSet) throws InvalidParameterValueException, SQLException;

    int getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                                          FilterSet filterSet) throws InvalidParameterValueException, SQLException;

    Map<String, Integer> getDeviceCountsByPlatforms(FilterSet filterSet)
                                          throws InvalidParameterValueException, SQLException;

    Map<String, Integer> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                          FilterSet filterSet) throws InvalidParameterValueException, SQLException;

    Map<String, Integer> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                          throws InvalidParameterValueException, SQLException;

    Map<String, Integer> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                          FilterSet filterSet) throws InvalidParameterValueException, SQLException;

    PaginationResult getDevicesWithDetails(FilterSet filterSet, int startIndex, int resultCount)
                                          throws InvalidParameterValueException, SQLException;

    PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
         FilterSet filterSet, int startIndex, int resultCount) throws InvalidParameterValueException, SQLException;

    List<Map<String, Object>> getDevicesWithDetails(FilterSet filterSet)
                                          throws InvalidParameterValueException, SQLException;

    List<Map<String, Object>> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                          FilterSet filterSet) throws InvalidParameterValueException, SQLException;

}
