/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.Device;

import java.util.List;

/**
 * This defines the contract to be implemented by DeviceCacheManager which holds the necessary functionalities to
 * manage a cache of Device objects.
 */
public interface DeviceCacheManager {

    /**
     * Adds a given device object to the device-cache.
     * @param deviceIdentifier - DeviceIdentifier of the device to be added.
     * @param device - Device object to be added.
     * @param tenantId - Owning tenant of the device.
     *
     */
    void addDeviceToCache(DeviceIdentifier deviceIdentifier, Device device, int tenantId);

    /**
     * Removes a device object from device-cache.
     * @param deviceIdentifier - DeviceIdentifier of the device to be removed.
     * @param tenantId - Owning tenant of the device.
     *
     */
    void removeDeviceFromCache(DeviceIdentifier deviceIdentifier, int tenantId);

    /**
     * Removes a list of devices from device-cache.
     * @param deviceList - List of Cache-Keys of the device objects to be removed.
     *
     */
    void removeDevicesFromCache(List<DeviceCacheKey> deviceList);

    /**
     * Updates a given device object in the device-cache.
     * @param deviceIdentifier - DeviceIdentifier of the device to be updated.
     * @param device - Device object to be updated.
     * @param tenantId - Owning tenant of the device.
     *
     */
    void updateDeviceInCache(DeviceIdentifier deviceIdentifier, Device device, int tenantId);

    /**
     * Fetches a device object from device-cache.
     * @param deviceIdentifier - DeviceIdentifier of the device to be fetched.
     * @param tenantId - Owning tenant of the device.
     * @return Device object
     *
     */
    Device getDeviceFromCache(DeviceIdentifier deviceIdentifier, int tenantId);
}
