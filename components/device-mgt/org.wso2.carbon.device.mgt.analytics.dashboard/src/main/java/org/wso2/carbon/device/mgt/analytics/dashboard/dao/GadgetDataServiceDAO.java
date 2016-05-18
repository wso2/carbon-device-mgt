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

import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.DetailedDeviceEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.DeviceCountByGroupEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.bean.FilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.DataAccessLayerException;
import org.wso2.carbon.device.mgt.analytics.dashboard.dao.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.util.List;

public interface GadgetDataServiceDAO {

    /**
     * This method is used to get device count based on a defined filter set.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return An object of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if and only if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    DeviceCountByGroupEntry getDeviceCount(FilterSet filterSet)
                                           throws InvalidParameterValueException, DataAccessLayerException;

    DeviceCountByGroupEntry getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    DeviceCountByGroupEntry getTotalDeviceCount() throws DataAccessLayerException;

    List<DeviceCountByGroupEntry> getDeviceCountsByConnectivityStatuses() throws DataAccessLayerException;

    List<DeviceCountByGroupEntry> getDeviceCountsByPotentialVulnerabilities() throws DataAccessLayerException;

    PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                          throws InvalidParameterValueException, DataAccessLayerException;

    List<DeviceCountByGroupEntry> getDeviceCountsByPlatforms(FilterSet filterSet)
                                          throws InvalidParameterValueException, DataAccessLayerException;

    List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    List<DeviceCountByGroupEntry> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                          throws InvalidParameterValueException, DataAccessLayerException;

    List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    PaginationResult getDevicesWithDetails(FilterSet filterSet, int startIndex, int resultCount)
                                           throws InvalidParameterValueException, DataAccessLayerException;

    PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                 FilterSet filterSet, int startIndex, int resultCount)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    List<DetailedDeviceEntry> getDevicesWithDetails(FilterSet filterSet)
                                           throws InvalidParameterValueException, DataAccessLayerException;

    List<DetailedDeviceEntry> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

}
