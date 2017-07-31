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

/**
 * Implementation of {@link PlatformManager}, which manages the CRUD operations on Application platforms.
 */
public class PlatformManagerImpl implements PlatformManager {
    private Map<Integer, Map<String, Platform>> inMemoryStore;
    private static Log log = LogFactory.getLog(PlatformManagerImpl.class);

    public PlatformManagerImpl() {
        this.inMemoryStore = new HashMap<>();
    }

    @Override
    public void initialize(int tenantId) throws PlatformManagementException {
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
        List<String> platformIdentifiers = new ArrayList<>();
        for (Platform platform : platforms) {
            if (!platform.isEnabled() & platform.isDefaultTenantMapping()) {
                platformIdentifiers.add(platform.getIdentifier());
            }
        }
        addMapping(tenantId, platformIdentifiers);
    }

    @Override
    public List<Platform> getPlatforms(int tenantId) throws PlatformManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Request for getting platforms received for the tenant ID " + tenantId + " at "
                    + "PlatformManager level");
        }
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
        int platformIndex = 0;

        if (log.isDebugEnabled()) {
            log.debug("Number of platforms received from DAO layer is  " + platforms.size() + " for the tenant "
                    + tenantId);
        }
        for (Platform platform : platforms) {
            if (platform.isFileBased()) {
                Map<String, Platform> superTenantPlatforms = this.inMemoryStore
                        .get(MultitenantConstants.SUPER_TENANT_ID);
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
            log.debug("Number of effective platforms for the tenant " + tenantId
                    + " : " + platforms.size());
        }
        return platforms;
    }

    @Override
    public Platform getPlatform(int tenantId, String identifier) throws PlatformManagementException {
        Platform platform = getPlatformFromInMemory(tenantId, identifier);
        if (platform == null) {
            platform = DAOFactory.getPlatformDAO().getPlatform(tenantId, identifier);
            if (platform != null) {
                return platform;
            }
        } else {
            return new Platform(platform);
        }
        throw new PlatformManagementException("No platform was found for tenant - " + tenantId +
                " with Platform identifier - " + identifier);
    }

    private Platform getPlatformFromInMemory(int tenantId, String identifier) {
        Map<String, Platform> platformMap = this.inMemoryStore.get(tenantId);
        if (platformMap != null) {
            Platform platform = platformMap.get(identifier);
            if (platform != null) {
                return platform;
            }
        }
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            platformMap = this.inMemoryStore.get(MultitenantConstants.SUPER_TENANT_ID);
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
    public synchronized void register(int tenantId, Platform platform) throws PlatformManagementException {
        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformManagementException(
                    "Platform sharing is a restricted operation, therefore Platform - " + platform.getIdentifier()
                            + " cannot be shared by the tenant domain - " + tenantId);
        }
        int platformId = DAOFactory.getPlatformDAO().register(tenantId, platform);
        if (platform.isFileBased()) {
            platform.setId(platformId);
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
            if (tenantPlatforms == null) {
                tenantPlatforms = new HashMap<>();
                this.inMemoryStore.put(tenantId, tenantPlatforms);
            }
            if (tenantPlatforms.get(platform.getIdentifier()) == null) {
                tenantPlatforms.put(platform.getIdentifier(), platform);
            } else {
                throw new PlatformManagementException(
                        "Platform - " + platform.getIdentifier() + " is already registered!");
            }
        }
        if (platform.isDefaultTenantMapping()) {
            try {
                if (platform.isShared()) {
                    TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                    Tenant[] tenants = tenantManager.getAllTenants();
                    for (Tenant tenant : tenants) {
                        addMapping(tenant.getId(), platform.getIdentifier());
                    }
                }
                addMapping(tenantId, platform.getIdentifier());
            } catch (UserStoreException e) {
                throw new PlatformManagementException("Error occured while assigning the platforms for tenants!", e);
            }
        }
    }

    @Override
    public void update(int tenantId, String oldPlatformIdentifier, Platform platform)
            throws PlatformManagementException {
        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformManagementException(
                    "Platform sharing is a restricted operation, therefore Platform - " + platform.getIdentifier()
                            + " cannot be shared by the tenant domain - " + tenantId);
        }
        Platform oldPlatform;
        if (platform.isFileBased()) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
            if (tenantPlatforms == null) {
                throw new PlatformManagementException(
                        "No platforms registered for the tenant - " + tenantId + " with platform identifier - "
                                + platform.getIdentifier());
            }
            oldPlatform = tenantPlatforms.get(oldPlatformIdentifier);
            if (oldPlatform == null) {
                throw new PlatformManagementException(
                        "No platforms registered for the tenant - " + tenantId + " with platform identifier - "
                                + platform.getIdentifier());
            } else {
                DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
                platform.setId(oldPlatform.getId());
                tenantPlatforms.put(platform.getIdentifier(), platform);
            }
        } else {
            oldPlatform = DAOFactory.getPlatformDAO().getPlatform(tenantId, oldPlatformIdentifier);
            DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
        }
        if (platform.isDefaultTenantMapping() && !oldPlatform.isDefaultTenantMapping()) {
            try {
                if (platform.isShared() && !oldPlatform.isShared()) {
                    TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                    Tenant[] tenants = tenantManager.getAllTenants();
                    for (Tenant tenant : tenants) {
                        addMapping(tenant.getId(), platform.getIdentifier());
                    }
                }
                addMapping(tenantId, platform.getIdentifier());
            } catch (UserStoreException e) {
                throw new PlatformManagementException("Error occurred while assigning the platforms for tenants!", e);
            }
        }
        if (!platform.isShared() && oldPlatform.isShared()) {
            DAOFactory.getPlatformDAO().removeMappingTenants(platform.getIdentifier());
        }
    }

    @Override
    public void unregister(int tenantId, String identifier, boolean isFileBased) throws
            PlatformManagementException {
        if (isFileBased) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
            if (tenantPlatforms != null) {
                this.inMemoryStore.remove(identifier);
            }
        }
        DAOFactory.getPlatformDAO().unregister(tenantId, identifier);
    }

    @Override
    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().addMapping(tenantId, platformIdentifiers);
    }

    @Override
    public void addMapping(int tenantId, String platformIdentifier) throws PlatformManagementException {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(platformIdentifier);
        DAOFactory.getPlatformDAO().addMapping(tenantId, identifiers);
    }

    @Override
    public void removeMapping(int tenantId, String platformIdentifier) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().removeMapping(tenantId, platformIdentifier);
    }
}
