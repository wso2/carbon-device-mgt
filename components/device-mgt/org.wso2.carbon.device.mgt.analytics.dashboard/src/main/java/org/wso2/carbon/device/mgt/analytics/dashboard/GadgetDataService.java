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

import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.*;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.util.List;

/**
 * This interface exposes useful service layer functions to retrieve data
 * required by high level dashboard APIs.
 */
public interface GadgetDataService {

    /**
     * This method is used to get a count of devices based on a defined filter set.
     * @param extendedFilterSet An abstract representation of possible filtering options.
     *                          if this value is simply "null" or no values are set for the defined filtering
     *                          options, this method would return total device count in the system
     *                          wrapped by the defined return format.
     * @return An object of type DeviceCountByGroup.
     * @throws InvalidPotentialVulnerabilityValueException This can occur if potentialVulnerability
     *                                                     value of extendedFilterSet is set with some
     *                                                     value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    DeviceCountByGroup getDeviceCount(ExtendedFilterSet extendedFilterSet, String userName)
                       throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException;

    /**
     * This method is used to get a count of devices non-compliant upon on a particular feature
     * and a defined filter set.
     * @param featureCode Code name of the non-compliant feature.
     * @param basicFilterSet An abstract representation of possible filtering options.
     *                       if this value is simply "null" or no values are set for the defined filtering
     *                       options, this method would return total non-compliant device count in the system
     *                       for the given feature-code, wrapped by the defined return format.
     * @return An object of type DeviceCountByGroup.
     * @throws InvalidFeatureCodeValueException This can occur if featureCode is set to null or empty.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    DeviceCountByGroup getFeatureNonCompliantDeviceCount(String featureCode, BasicFilterSet basicFilterSet, String userName)
                       throws InvalidFeatureCodeValueException, DataAccessLayerException;

    /**
     * This method is used to get total count of devices currently enrolled under a particular tenant.
     * @return An object of type DeviceCountByGroup.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    DeviceCountByGroup getTotalDeviceCount(String userName) throws DataAccessLayerException;

    /**
     * This method is used to get device counts classified by connectivity statuses.
     * @return A list of objects of type DeviceCountByGroup.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getDeviceCountsByConnectivityStatuses(String userName) throws DataAccessLayerException;

    /**
     * This method is used to get device counts classified by potential vulnerabilities.
     * @return A list of objects of type DeviceCountByGroup.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getDeviceCountsByPotentialVulnerabilities(String userName) throws DataAccessLayerException;

    /**
     * This method is used to get non-compliant device counts classified by individual features.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidStartIndexValueException This can occur if startIndex value is lesser than its minimum (0).
     * @throws InvalidResultCountValueException This can occur if resultCount value is lesser than its minimum (5).
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount, String userName) throws
                     InvalidStartIndexValueException, InvalidResultCountValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts classified by platforms.
     * @param extendedFilterSet An abstract representation of possible filtering options.
     *                          if this value is simply "null" or no values are set for the defined filtering
     *                          options, this method would return total device counts per each platform in
     *                          the system, wrapped by the defined return format.
     * @return An object of type DeviceCountByGroup.
     * @throws InvalidPotentialVulnerabilityValueException This can occur if potentialVulnerability
     *                                                     value of extendedFilterSet is set with some
     *                                                     value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getDeviceCountsByPlatforms(ExtendedFilterSet extendedFilterSet, String userName)
                             throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts non-compliant upon a particular feature classified by platforms.
     * @param featureCode Code name of the non-compliant feature.
     * @param basicFilterSet An abstract representation of possible filtering options.
     *                       if this value is simply "null" or no values are set for the defined filtering
     *                       options, this method would return total non-compliant device counts per each platform
     *                       in the system, wrapped by the defined return format.
     * @return A list of objects of type DeviceCountByGroup.
     * @throws InvalidFeatureCodeValueException This can occur if featureCode is set to null or empty.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getFeatureNonCompliantDeviceCountsByPlatforms(String featureCode,
                             BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException,
                             DataAccessLayerException;

    /**
     * This method is used to get device counts classified by ownership types.
     * @param extendedFilterSet An abstract representation of possible filtering options.
     *                          if this value is simply "null" or no values are set for the defined filtering
     *                          options, this method would return total device counts per each ownership
     *                          type in the system, wrapped by the defined return format.
     * @return A list of objects of type DeviceCountByGroup.
     * @throws InvalidPotentialVulnerabilityValueException This can occur if potentialVulnerability
     *                                                     value of extendedFilterSet is set with some
     *                                                     value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getDeviceCountsByOwnershipTypes(ExtendedFilterSet extendedFilterSet, String userName)
                             throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException;

    /**
     * This method is used to get device counts non-compliant upon a particular feature
     * classified by ownership types.
     * @param featureCode Code name of the non-compliant feature.
     * @param basicFilterSet An abstract representation of possible filtering options.
     *                       if this value is simply "null" or no values are set for the defined filtering
     *                       options, this method would return total non-compliant device counts per each
     *                       ownership type in the system, wrapped by the defined return format.
     * @return A list of objects of type DeviceCountByGroup.
     * @throws InvalidFeatureCodeValueException This can occur if featureCode is set to null or empty.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceCountByGroup> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String featureCode,
                             BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException,
                             DataAccessLayerException;

    /**
     * This method is used to get a paginated list of devices with details, based on a defined filter set.
     * @param extendedFilterSet An abstract representation of possible filtering options.
     *                          if this value is simply "null" or no values are set for the defined
     *                          filtering options, this method would return a paginated device list in the
     *                          system specified by result count, starting from specified start index, and
     *                          wrapped by the defined return format.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidPotentialVulnerabilityValueException This can occur if potentialVulnerability
     *                                                     value of extendedFilterSet is set with some
     *                                                     value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     * @throws InvalidStartIndexValueException This can occur if startIndex value is lesser than its minimum (0).
     * @throws InvalidResultCountValueException This can occur if resultCount value is lesser than its minimum (5).
     */
    @SuppressWarnings("unused")
    PaginationResult getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, int startIndex, int resultCount, String userName)
                     throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException,
                     InvalidStartIndexValueException, InvalidResultCountValueException;

    /**
     * This method is used to get a paginated list of non-compliant devices with details,
     * upon a particular feature.
     * @param featureCode Code name of the non-compliant feature.
     * @param basicFilterSet An abstract representation of possible filtering options.
     *                       if this value is simply "null" or no values are set for the defined filtering
     *                       options, this method would return a paginated device list in the system,
     *                       non-compliant by specified feature-code, result count, starting from specified
     *                       start index, and wrapped by the defined return format.
     * @param startIndex Starting index of the data set to be retrieved.
     * @param resultCount Total count of the result set retrieved.
     * @return An object of type PaginationResult.
     * @throws InvalidFeatureCodeValueException This can occur if featureCode is set to null or empty.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     * @throws InvalidStartIndexValueException This can occur if startIndex value is lesser than its minimum (0).
     * @throws InvalidResultCountValueException This can occur if resultCount value is lesser than its minimum (5).
     */
    @SuppressWarnings("unused")
    PaginationResult getFeatureNonCompliantDevicesWithDetails(String featureCode, BasicFilterSet basicFilterSet,
                     int startIndex, int resultCount, String userName) throws InvalidFeatureCodeValueException,
                     DataAccessLayerException, InvalidStartIndexValueException,
                     InvalidResultCountValueException;

    /**
     * This method is used to get a list of devices with details, based on a defined filter set.
     * @param extendedFilterSet An abstract representation of possible filtering options.
     *                          if this value is simply "null" or no values are set for the defined filtering
     *                          options, this method would return total device list in the system
     *                          wrapped by the defined return format.
     * @return A list of objects of type DeviceWithDetails.
     * @throws InvalidPotentialVulnerabilityValueException This can occur if potentialVulnerability
     *                                                     value of extendedFilterSet is set with some
     *                                                     value other than "NON_COMPLIANT" or "UNMONITORED".
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceWithDetails> getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, String userName)
                            throws InvalidPotentialVulnerabilityValueException, DataAccessLayerException;

    /**
     * This method is used to get a list of non-compliant devices with details, upon a particular feature.
     * @param featureCode Code name of the non-compliant feature.
     * @param basicFilterSet An abstract representation of possible filtering options.
     *                       if this value is simply "null" or no values are set for the defined filtering
     *                       options, this method would return total set of non-compliant devices in the
     *                       system upon given feature-code, wrapped by the defined return format.
     * @return A list of objects of type DeviceWithDetails.
     * @throws InvalidFeatureCodeValueException This can occur if featureCode is set to null or empty.
     * @throws DataAccessLayerException This can occur due to errors connecting to database,
     *                                  executing SQL query and retrieving data.
     */
    @SuppressWarnings("unused")
    List<DeviceWithDetails> getFeatureNonCompliantDevicesWithDetails(String featureCode,
                            BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException,
                            DataAccessLayerException;

}
