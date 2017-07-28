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

package org.wso2.carbon.device.mgt.core.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.core.cache.DeviceCacheKey;
import org.wso2.carbon.device.mgt.core.cache.DeviceCacheManager;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.cache.Cache;
import java.util.List;

/**
 * Implementation of DeviceCacheManager.
 */
public class DeviceCacheManagerImpl implements DeviceCacheManager {

    private static final Log log = LogFactory.getLog(DeviceCacheManagerImpl.class);

    private static DeviceCacheManagerImpl deviceCacheManager;

    private DeviceCacheManagerImpl() {
    }

    public static DeviceCacheManagerImpl getInstance() {
        if (deviceCacheManager == null) {
            synchronized (DeviceCacheManagerImpl.class) {
                if (deviceCacheManager == null) {
                    deviceCacheManager = new DeviceCacheManagerImpl();
                }
            }
        }
        return deviceCacheManager;
    }

    @Override
    public void addDeviceToCache(DeviceIdentifier deviceIdentifier, Device device, int tenantId) {
        Cache<DeviceCacheKey, Device> lCache = DeviceManagerUtil.getDeviceCache();
        if (lCache != null) {
            DeviceCacheKey cacheKey = getCacheKey(deviceIdentifier, tenantId);
            if (lCache.containsKey(cacheKey)) {
                this.updateDeviceInCache(deviceIdentifier, device, tenantId);
            } else {
                lCache.put(cacheKey, device);
            }
        }
    }

    @Override
    public void removeDeviceFromCache(DeviceIdentifier deviceIdentifier, int tenantId) {
        Cache<DeviceCacheKey, Device> lCache = DeviceManagerUtil.getDeviceCache();
        if (lCache != null) {
            DeviceCacheKey cacheKey = getCacheKey(deviceIdentifier, tenantId);
            if (lCache.containsKey(cacheKey)) {
                lCache.remove(cacheKey);
            }
        }
    }

    @Override
    public void removeDevicesFromCache(List<DeviceCacheKey> deviceList) {
        Cache<DeviceCacheKey, Device> lCache = DeviceManagerUtil.getDeviceCache();
        if (lCache != null) {
            for (DeviceCacheKey cacheKey : deviceList) {
                if (lCache.containsKey(cacheKey)) {
                    lCache.remove(cacheKey);
                }
            }
        }
    }

    @Override
    public void updateDeviceInCache(DeviceIdentifier deviceIdentifier, Device device, int tenantId) {
        Cache<DeviceCacheKey, Device> lCache = DeviceManagerUtil.getDeviceCache();
        if (lCache != null) {
            DeviceCacheKey cacheKey = getCacheKey(deviceIdentifier, tenantId);
            if (lCache.containsKey(cacheKey)) {
                lCache.replace(cacheKey, device);
            }
        }
    }

    @Override
    public Device getDeviceFromCache(DeviceIdentifier deviceIdentifier, int tenantId) {
        Cache<DeviceCacheKey, Device> lCache = DeviceManagerUtil.getDeviceCache();
        if (lCache != null) {
            return lCache.get(getCacheKey(deviceIdentifier, tenantId));
        }
        return null;
    }


    private DeviceCacheKey getCacheKey(DeviceIdentifier deviceIdentifier, int tenantId) {
        DeviceCacheKey deviceCacheKey = new DeviceCacheKey();
        deviceCacheKey.setDeviceId(deviceIdentifier.getId());
        deviceCacheKey.setDeviceType(deviceIdentifier.getType());
        deviceCacheKey.setTenantId(tenantId);
        return deviceCacheKey;
    }
}