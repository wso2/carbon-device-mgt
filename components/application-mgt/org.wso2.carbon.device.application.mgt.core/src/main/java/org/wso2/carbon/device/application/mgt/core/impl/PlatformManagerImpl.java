/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.application.mgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlatformManagerImpl implements PlatformManager {
    private Map<String, Map<String, Platform>> inMemoryStore;
    private static Log log = LogFactory.getLog(PlatformManagerImpl.class);

    public PlatformManagerImpl() {
        this.inMemoryStore = new HashMap<>();
    }

    @Override
    public void initialize(String tenantDomain) throws PlatformManagementException {
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantDomain);
        List<String> platformIdentifiers = new ArrayList<>();
        for (Platform platform : platforms) {
            if (!platform.isEnabled() & platform.isDefaultTenantMapping()) {
                platformIdentifiers.add(platform.getIdentifier());
            }
        }
        addMapping(tenantDomain, platformIdentifiers);
    }

    @Override
    public List<Platform> getPlatforms(String tenantDomain) throws PlatformManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Request for getting platforms received for the tenant domain " + tenantDomain + " at "
                    + "PlatformManager level");
        }
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantDomain);
        int platformIndex = 0;

        if (log.isDebugEnabled()) {
            log.debug("Number of platforms received from DAO layer is  " + platforms.size() + " for the tenant "
                    + tenantDomain);
        }
        for (Platform platform : platforms) {
            if (platform.isFileBased()) {
                Map<String, Platform> superTenantPlatforms = this.inMemoryStore
                        .get(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                Platform registeredPlatform = superTenantPlatforms.get(platform.getIdentifier());
                if (registeredPlatform != null) {
                    platforms.set(platformIndex, new Platform(registeredPlatform));
                    if (log.isDebugEnabled()) {
                        log.debug("Platform Name - " + platform.getName() + ", IsRegistered - " + true);
                    }
                } else {
                    platforms.remove(platformIndex);
                    if (log.isDebugEnabled()) {
                        log.debug("Platform Name - " + platform.getName() + ", IsRegistered - " + false);
                    }
                }
            }
            platformIndex++;
        }
        if (log.isDebugEnabled()) {
            log.debug("Number of effective platforms for the tenant " + tenantDomain + " : " + platforms.size());
        }
        return platforms;
    }

    @Override
    public Platform getPlatform(String tenantDomain, String identifier) throws PlatformManagementException {
        Platform platform = getPlatformFromInMemory(tenantDomain, identifier);
        if (platform == null) {
            platform = DAOFactory.getPlatformDAO().getPlatform(tenantDomain, identifier);
            if (platform != null) {
                return platform;
            }
        } else {
            return new Platform(platform);
        }
        throw new PlatformManagementException("No platform was found for tenant - " + tenantDomain +
                " with Platform identifier - " + identifier);
    }

    private Platform getPlatformFromInMemory(String tenantDomain, String identifier) {
        Map<String, Platform> platformMap = this.inMemoryStore.get(tenantDomain);
        if (platformMap != null) {
            Platform platform = platformMap.get(identifier);
            if (platform != null) {
                return platform;
            }
        }
        if (!tenantDomain.equalsIgnoreCase(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            platformMap = this.inMemoryStore.get(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            if (platformMap != null) {
                Platform platform = platformMap.get(identifier);
                if (platform != null && platform.isShared()) {
                    return platform;
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void register(String tenantDomain, Platform platform) throws PlatformManagementException {
        if (platform.isShared() && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            throw new PlatformManagementException("Platform sharing is a restricted operation, therefore Platform - "
                    + platform.getIdentifier() + " cannot be shared by the tenant domain - " + tenantDomain);
        }
        int platformId = DAOFactory.getPlatformDAO().register(tenantDomain, platform);
        if (platform.isFileBased()) {
            platform.setId(platformId);
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantDomain);
            if (tenantPlatforms == null) {
                tenantPlatforms = new HashMap<>();
                this.inMemoryStore.put(tenantDomain, tenantPlatforms);
            }
            if (tenantPlatforms.get(platform.getIdentifier()) == null) {
                tenantPlatforms.put(platform.getIdentifier(), platform);
            } else {
                throw new PlatformManagementException("Platform - " + platform.getIdentifier() + " is already registered!");
            }
        }
        if (platform.isDefaultTenantMapping()) {
            try {
                if (platform.isShared()) {
                    TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                    Tenant[] tenants = tenantManager.getAllTenants();
                    for (Tenant tenant : tenants) {
                        addMapping(tenant.getDomain(), platform.getIdentifier());
                    }
                }
                addMapping(tenantDomain, platform.getIdentifier());
            } catch (UserStoreException e) {
                throw new PlatformManagementException("Error occured while assigning the platforms for tenants!", e);
            }
        }
    }

    @Override
    public void update(String tenantDomain, String oldPlatformIdentifier, Platform platform)
            throws PlatformManagementException {
        if (platform.isShared() && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            throw new PlatformManagementException("Platform sharing is a restricted operation, therefore Platform - "
                    + platform.getIdentifier() + " cannot be shared by the tenant domain - " + tenantDomain);
        }
        Platform oldPlatform;
        if (platform.isFileBased()) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantDomain);
            if (tenantPlatforms == null) {
                throw new PlatformManagementException("No platforms registered for the tenant - " + tenantDomain +
                        " with platform identifier - " + platform.getIdentifier());
            }
            oldPlatform = tenantPlatforms.get(oldPlatformIdentifier);
            if (oldPlatform == null) {
                throw new PlatformManagementException("No platforms registered for the tenant - " + tenantDomain +
                        " with platform identifier - " + platform.getIdentifier());
            } else {
                DAOFactory.getPlatformDAO().update(tenantDomain, oldPlatformIdentifier, platform);
                platform.setId(oldPlatform.getId());
                tenantPlatforms.put(platform.getIdentifier(), platform);
            }
        } else {
            oldPlatform = DAOFactory.getPlatformDAO().getPlatform(tenantDomain, oldPlatformIdentifier);
            DAOFactory.getPlatformDAO().update(tenantDomain, oldPlatformIdentifier, platform);
        }
        if (platform.isDefaultTenantMapping() && !oldPlatform.isDefaultTenantMapping()) {
            try {
                if (platform.isShared() && !oldPlatform.isShared()) {
                    TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                    Tenant[] tenants = tenantManager.getAllTenants();
                    for (Tenant tenant : tenants) {
                        addMapping(tenant.getDomain(), platform.getIdentifier());
                    }
                }
                addMapping(tenantDomain, platform.getIdentifier());
            } catch (UserStoreException e) {
                throw new PlatformManagementException("Error occured while assigning the platforms for tenants!", e);
            }
        }
        if (!platform.isShared() && oldPlatform.isShared()) {
            DAOFactory.getPlatformDAO().removeMappingTenants(platform.getIdentifier());
        }
    }

    @Override
    public void unregister(String tenantDomain, String identifier, boolean isFileBased) throws PlatformManagementException {
        if (isFileBased) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantDomain);
            if (tenantPlatforms != null) {
                this.inMemoryStore.remove(identifier);
            }
        } else {
            DAOFactory.getPlatformDAO().unregister(tenantDomain, identifier);
        }
    }

    @Override
    public void addMapping(String tenantDomain, List<String> platformIdentifiers) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().addMapping(tenantDomain, platformIdentifiers);
    }

    @Override
    public void addMapping(String tenantDomain, String platformIdentifier) throws PlatformManagementException {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(platformIdentifier);
        DAOFactory.getPlatformDAO().addMapping(tenantDomain, identifiers);
    }

    @Override
    public void removeMapping(String tenantDomain, String platformIdentifier) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().removeMapping(tenantDomain, platformIdentifier);
    }
}
