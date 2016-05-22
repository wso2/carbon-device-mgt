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

package org.wso2.carbon.device.mgt.analytics.dashboard;

import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DetailedDeviceEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroupEntry;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.FilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.DataAccessLayerException;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.InvalidParameterValueException;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.util.List;

/**
 * This interface exposes useful service layer functions to retrieve data
 * required by high level dashboard APIs.
 */
public interface GadgetDataService {

    /**
     * This method is used to get a count of devices based on a defined filter set.
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
    @SuppressWarnings("unused")
    DeviceCountByGroupEntry getDeviceCount(FilterSet filterSet)
                            throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get a count of devices non-compliant upon on a particular feature
     * and a defined filter set.
     * @param nonCompliantFeatureCode Code name of the non-compliant feature.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return An object of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if nonCompliantFeatureCode is set to null or empty.
     *                                        This can also occur if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    DeviceCountByGroupEntry getFeatureNonCompliantDeviceCount(String nonCompliantFeatureCode,
                            FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get total count of devices currently enrolled under a particular tenant.
     * @return An object of type DeviceCountByGroupEntry.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    DeviceCountByGroupEntry getTotalDeviceCount() throws DataAccessLayerException;

    /**
     * This method is used to get device counts classified by connectivity statuses.
     * @return A list of objects of type DeviceCountByGroupEntry.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getDeviceCountsByConnectivityStatuses() throws DataAccessLayerException;

    /**
     * This method is used to get device counts classified by potential vulnerabilities.
     * @return A list of objects of type DeviceCountByGroupEntry.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getDeviceCountsByPotentialVulnerabilities() throws DataAccessLayerException;

    /**
     * This method is used to get non-compliant device counts classified by individual features.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidParameterValueException This can occur if startIndex or resultCount is set to values
     *                                        lesser than their minimums.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts classified by platforms.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return An object of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if startIndex or resultCount is set to values
     *                                        lesser than their minimums.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getDeviceCountsByPlatforms(FilterSet filterSet)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts non-compliant upon a particular feature classified by platforms.
     * @param nonCompliantFeatureCode Code name of the non-compliant feature.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return A list of objects of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if and only if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByPlatforms(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts classified by ownership types.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return A list of objects of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if nonCompliantFeatureCode is set to null or empty.
     *                                        This can also occur if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getDeviceCountsByOwnershipTypes(FilterSet filterSet)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts non-compliant upon a particular feature classified by ownership types.
     * @param nonCompliantFeatureCode Code name of the non-compliant feature.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return A list of objects of type DeviceCountByGroupEntry.
     * @throws InvalidParameterValueException This can occur if and only if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroupEntry> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get a paginated list of devices with details, based on a defined filter set.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidParameterValueException This can occur if nonCompliantFeatureCode is set to null or empty.
     *                                        This can also occur if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    PaginationResult getDevicesWithDetails(FilterSet filterSet, int startIndex, int resultCount)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get a paginated list of non-compliant devices with details, upon a particular feature.
     * @param nonCompliantFeatureCode Code name of the non-compliant feature.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidParameterValueException This can occur if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     *                                        This can also occur if startIndex or resultCount is set to values
     *                                        lesser than their minimums.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    PaginationResult getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                                   FilterSet filterSet, int startIndex, int resultCount)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get a list of devices with details, based on a defined filter set.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return A list of objects of type DetailedDeviceEntry.
     * @throws InvalidParameterValueException This can occur if nonCompliantFeatureCode is set to null or empty.
     *                                        This can occur if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     *                                        This can also occur if startIndex or resultCount is set to values
     *                                        lesser than their minimums.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DetailedDeviceEntry> getDevicesWithDetails(FilterSet filterSet)
                                                       throws InvalidParameterValueException, DataAccessLayerException;

    /**
     * This method is used to get a list of non-compliant devices with details, upon a particular feature.
     * @param nonCompliantFeatureCode Code name of the non-compliant feature.
     * @param filterSet An abstract representation of possible filtering options.
     *                  if this value is simply "null" or no values are set for the defined filtering options,
     *                  this method would return total device count in the system
     *                  wrapped with in the defined return format.
     * @return A list of objects of type DetailedDeviceEntry.
     * @throws InvalidParameterValueException This can occur if and only if potentialVulnerability value of filterSet
     *                                        is set with some value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DetailedDeviceEntry> getFeatureNonCompliantDevicesWithDetails(String nonCompliantFeatureCode,
                                  FilterSet filterSet) throws InvalidParameterValueException, DataAccessLayerException;

}
