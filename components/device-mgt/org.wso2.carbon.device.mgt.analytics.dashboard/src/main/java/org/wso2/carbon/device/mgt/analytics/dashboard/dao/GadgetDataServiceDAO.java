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

import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceWithDetails;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.DeviceCountByGroup;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.BasicFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.bean.ExtendedFilterSet;
import org.wso2.carbon.device.mgt.analytics.dashboard.exception.*;
import org.wso2.carbon.device.mgt.common.PaginationResult;

import java.sql.SQLException;
import java.util.List;

public interface GadgetDataServiceDAO {

    DeviceCountByGroup getDeviceCount(ExtendedFilterSet extendedFilterSet, String userName)
                                           throws InvalidPotentialVulnerabilityValueException, SQLException;

    DeviceCountByGroup getFeatureNonCompliantDeviceCount(String featureCode, BasicFilterSet basicFilterSet, String userName)
                                           throws InvalidFeatureCodeValueException, SQLException;

    DeviceCountByGroup getTotalDeviceCount(String userName) throws SQLException;

    List<DeviceCountByGroup> getDeviceCountsByConnectivityStatuses(String userName) throws SQLException;

    List<DeviceCountByGroup> getDeviceCountsByPotentialVulnerabilities(String userName) throws SQLException;

    PaginationResult getNonCompliantDeviceCountsByFeatures(int startIndex, int resultCount, String userName) throws
                                  InvalidStartIndexValueException, InvalidResultCountValueException, SQLException;

    List<DeviceCountByGroup> getDeviceCountsByPlatforms(ExtendedFilterSet extendedFilterSet, String userName)
                                           throws InvalidPotentialVulnerabilityValueException, SQLException;

    List<DeviceCountByGroup> getFeatureNonCompliantDeviceCountsByPlatforms(String featureCode,
                                  BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException, SQLException;

    List<DeviceCountByGroup> getDeviceCountsByOwnershipTypes(ExtendedFilterSet extendedFilterSet, String userName)
                                           throws InvalidPotentialVulnerabilityValueException, SQLException;

    List<DeviceCountByGroup> getFeatureNonCompliantDeviceCountsByOwnershipTypes(String featureCode,
                                  BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException, SQLException;

    PaginationResult getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, int startIndex, int resultCount, String userName)
                                  throws InvalidPotentialVulnerabilityValueException,
                                  InvalidStartIndexValueException, InvalidResultCountValueException, SQLException;

    PaginationResult getFeatureNonCompliantDevicesWithDetails(String featureCode, BasicFilterSet basicFilterSet,
                                  int startIndex, int resultCount, String userName) throws InvalidFeatureCodeValueException,
                                  InvalidStartIndexValueException, InvalidResultCountValueException, SQLException;

    List<DeviceWithDetails> getDevicesWithDetails(ExtendedFilterSet extendedFilterSet, String userName)
                                  throws InvalidPotentialVulnerabilityValueException, SQLException;

    List<DeviceWithDetails> getFeatureNonCompliantDevicesWithDetails(String featureCode,
                                  BasicFilterSet basicFilterSet, String userName) throws InvalidFeatureCodeValueException, SQLException;

}
