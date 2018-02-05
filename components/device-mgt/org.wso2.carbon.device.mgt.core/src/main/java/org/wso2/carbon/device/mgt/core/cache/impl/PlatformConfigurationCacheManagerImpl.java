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

package org.wso2.carbon.device.mgt.core.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.core.cache.PlatformConfigCacheKey;
import org.wso2.carbon.device.mgt.core.cache.PlatformConfigurationCacheManager;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.util.concurrent.TimeUnit;


public class PlatformConfigurationCacheManagerImpl implements PlatformConfigurationCacheManager {

    private static final Log log = LogFactory.getLog(PlatformConfigurationCacheManagerImpl.class);

    public static final String PLATFORM_CONFIG_CACHE_MANAGER = "PLATFORM_CONFIG_CACHE_MANAGER";
    public static final String PLATFORM_CONFIG_CACHE = "PLATFORM_CONFIG_CACHE";
    private static boolean isPlatformConfigCacheInitialized = false;
    private static String SERIAL_PRE = "S_";
    private static String COMMON_NAME_PRE = "PC_";

    private static PlatformConfigurationCacheManager platformConfigurationCacheManager;

    private PlatformConfigurationCacheManagerImpl() {
    }

    public static PlatformConfigurationCacheManager getInstance() {
        if (platformConfigurationCacheManager == null) {
            synchronized (PlatformConfigurationCacheManagerImpl.class) {
                if (platformConfigurationCacheManager == null) {
                    platformConfigurationCacheManager = new PlatformConfigurationCacheManagerImpl();
                }
            }
        }
        return platformConfigurationCacheManager;
    }

    @Override
    public void addPlatformConfigurationCache(int tenantId, PlatformConfiguration platformConfiguration) {
        Cache lCache = PlatformConfigurationCacheManagerImpl.getPlatformConfigCache();
        if (lCache != null) {
            PlatformConfigCacheKey cacheKey = getCacheKey(tenantId);
            if (lCache.containsKey(cacheKey)) {
                this.updatePlatformConfigurationCache(tenantId, platformConfiguration);
            } else {
                lCache.put(cacheKey, platformConfiguration);
            }
        }
    }

    @Override
    public void updatePlatformConfigurationCache(int tenantId, PlatformConfiguration platformConfiguration) {
        Cache lCache = PlatformConfigurationCacheManagerImpl.getPlatformConfigCache();
        if (lCache != null) {
            PlatformConfigCacheKey cacheKey = getCacheKey(tenantId);
            if (lCache.containsKey(cacheKey)) {
                this.updatePlatformConfigurationCache(tenantId, platformConfiguration);
            }
        }
    }

    @Override
    public PlatformConfiguration getPlatformConfigurationFromCache(int tenantId) {
        Cache<PlatformConfigCacheKey, PlatformConfiguration> lCache = PlatformConfigurationCacheManagerImpl.
                getPlatformConfigCache();
        if (lCache != null) {
            return lCache.get(getCacheKey(tenantId));
        }
        return null;
    }

    @Override
    public void removePlatformConfigurationFromCache(int tenantId) {
        Cache<PlatformConfigCacheKey, PlatformConfiguration> lCache = PlatformConfigurationCacheManagerImpl.
                getPlatformConfigCache();
        if (lCache != null) {
            if (lCache.containsKey(getCacheKey(tenantId))) {
                lCache.remove(getCacheKey(tenantId));
            }
        }
    }

    private static CacheManager getCacheManager() {
        return Caching.getCacheManagerFactory().
                getCacheManager(PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE_MANAGER);
    }

    public static Cache<PlatformConfigCacheKey, PlatformConfiguration> getPlatformConfigCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        CacheManager manager = getCacheManager();
        Cache<PlatformConfigCacheKey, PlatformConfiguration> PlatformConfigCache = null;
        if (config.getDeviceCacheConfiguration().isEnabled()) {
            if (!isPlatformConfigCacheInitialized) {
                initializePlatformConfigCache();
            }
            if (manager != null) {
                PlatformConfigCache = manager.getCache(
                        PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE);
            } else {
                PlatformConfigCache = Caching.getCacheManager(
                        PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE_MANAGER).
                        getCache(
                                PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE);
            }
        }
        return PlatformConfigCache;
    }

    public static void initializePlatformConfigCache() {
        DeviceManagementConfig config = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        int PlatformConfigCacheExpiry = config.getPlatformConfigCacheConfiguration().getExpiryTime();
        CacheManager manager = getCacheManager();
        //if (config.getPlatformConfigCacheConfiguration().isEnabled()) {
        if (!isPlatformConfigCacheInitialized) {
            isPlatformConfigCacheInitialized = true;
            if (manager != null) {
                if (PlatformConfigCacheExpiry > 0) {
                    manager.<PlatformConfigCacheKey, PlatformConfiguration>createCacheBuilder(
                            PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE).
                            setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, PlatformConfigCacheExpiry)).
                            setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, PlatformConfigCacheExpiry)).
                            setStoreByValue(true).build();
                } else {
                    manager.<PlatformConfigCacheKey, PlatformConfiguration>getCache
                            (PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE);
                }
            } else {
                if (PlatformConfigCacheExpiry > 0) {
                    Caching.getCacheManager().<PlatformConfigCacheKey, PlatformConfiguration>createCacheBuilder(
                            PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE).
                            setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, PlatformConfigCacheExpiry)).
                            setExpiry(CacheConfiguration.ExpiryType.ACCESSED,
                                    new CacheConfiguration.Duration(TimeUnit.SECONDS, PlatformConfigCacheExpiry)).
                            setStoreByValue(true).build();
                    log.debug("Platform Configuration cache expiration was set");
                } else {
                    Caching.getCacheManager().<PlatformConfigCacheKey, PlatformConfiguration>getCache
                            (PlatformConfigurationCacheManagerImpl.PLATFORM_CONFIG_CACHE);
                }
            }
            log.debug("Platform Configuration cache initialized");
        }
        //}
    }

    private PlatformConfigCacheKey getCacheKey(int tenantId) {
        PlatformConfigCacheKey cacheKey = new PlatformConfigCacheKey();
        cacheKey.setTenantId(tenantId);
        return cacheKey;
    }
}
