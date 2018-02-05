/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.cache;

import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;

import java.util.List;

/**
 * This defines the contract to be implemented by PlatformConfigurationCacheManager which holds the necessary
 * functionalities to manage a cache of PlatformConfiguration object.
 */
public interface PlatformConfigurationCacheManager {

    /**
     * Adds a given PlatformConfiguration object to the PlatformConfiguration-cache.
     * @param tenantId - Owning tenant of the PlatformConfiguration.
     *
     */
    void addPlatformConfigurationCache(int tenantId, PlatformConfiguration platformConfiguration);

    /**
     * Updates a given PlatformConfiguration object in the PlatformConfiguration-cache.
     * @param tenantId - Owning tenant of the PlatformConfiguration.
     *
     */
    void updatePlatformConfigurationCache(int tenantId, PlatformConfiguration platformConfiguration);

    /**
     * Fetches a PlatformConfiguration object from PlatformConfiguration-cache.
     * @param tenantId - Owning tenant of the PlatformConfiguration.
     *
     */
    PlatformConfiguration getPlatformConfigurationFromCache(int tenantId);

    /**
     * Removed a PlatformConfiguration object from PlatformConfiguration-cache.
     * @param tenantId - Owning tenant of the PlatformConfiguration.
     *
     */
    void removePlatformConfigurationFromCache(int tenantId);
}
