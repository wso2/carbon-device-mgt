/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.geo.service;

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;

import java.util.List;

/**
 * This represents the Geo service functionality which should be implemented by
 * required GeoServiceManagers.
 */
public interface GeoService {

    List<GeoFence> getWithinAlerts(DeviceIdentifier identifier) throws GeoServiceException;

    List<GeoFence> getExitAlerts(DeviceIdentifier identifier) throws GeoServiceException;

    boolean createGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType)
            throws GeoServiceException;

    boolean updateGeoAlert(Alert alert, DeviceIdentifier identifier, String alertType)
            throws GeoServiceException;

    boolean removeGeoAlert(String alertType, DeviceIdentifier identifier, String queryName)
            throws GeoServiceException;

    String getSpeedAlerts(DeviceIdentifier identifier) throws GeoServiceException;

    String getProximityAlerts(DeviceIdentifier identifier) throws GeoServiceException;

    List<GeoFence> getStationaryAlerts(DeviceIdentifier identifier) throws GeoServiceException;

    List<GeoFence> getTrafficAlerts(DeviceIdentifier identifier) throws GeoServiceException;
}
