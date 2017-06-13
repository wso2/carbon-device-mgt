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

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
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

    public PlatformManagerImpl() {
        this.inMemoryStore = new HashMap<>();
    }

    @Override
    public void initialize(String tenantDomain) throws PlatformManagementException {
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantDomain);
        List<String> platformCodes = new ArrayList<>();
        for (Platform platform : platforms) {
            if (!platform.isEnabled() & platform.isDefaultTenantMapping()) {
                platformCodes.add(platform.getCode());
            }
        }
        addMapping(tenantDomain, platformCodes);
    }

    @Override
    public List<Platform> getPlatforms(String tenantDomain) throws PlatformManagementException {
        List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantDomain);
        int platformIndex = 0;
        for (Platform platform : platforms) {
            if (platform.isFileBased()) {
                Map<String, Platform> superTenantPlatforms = this.inMemoryStore.get(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                Platform registeredPlatform = superTenantPlatforms.get(platform.getCode());
                if (registeredPlatform != null) {
                    platforms.set(platformIndex, new Platform(registeredPlatform));
                } else {
                    platforms.remove(platformIndex);
                }
            }
            platformIndex++;
        }
        return platforms;
    }

    @Override
    public synchronized void register(String tenantDomain, Platform platform) throws PlatformManagementException {
        if (platform.isShared() && !tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            throw new PlatformManagementException("Platform sharing is a restricted operation, therefore Platform - "
                    + platform.getCode() + " cannot be shared by the tenant domain - " + tenantDomain);
        }
        if (platform.isFileBased()) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantDomain);
            if (tenantPlatforms == null) {
                tenantPlatforms = new HashMap<>();
                this.inMemoryStore.put(tenantDomain, tenantPlatforms);
            }
            if (tenantPlatforms.get(platform.getCode()) == null) {
                tenantPlatforms.put(platform.getCode(), platform);
            } else {
                throw new PlatformManagementException("Platform - " + platform.getCode() + " is already registered!");
            }
        } else {
            DAOFactory.getPlatformDAO().register(tenantDomain, platform);
        }
        if (platform.isDefaultTenantMapping()) {
            try {
                if (platform.isShared()) {
                    TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                    Tenant[] tenants = tenantManager.getAllTenants();
                    for (Tenant tenant : tenants) {
                        addMapping(tenant.getDomain(), platform.getCode());
                    }
                }
                addMapping(tenantDomain, platform.getCode());
            } catch (UserStoreException e) {
                throw new PlatformManagementException("Error occured while assigning the platforms for tenants!", e);
            }
        }
    }

    @Override
    public void unregister(String tenantDomain, String platformCode, boolean isFileBased) throws PlatformManagementException {
        if (isFileBased) {
            Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantDomain);
            if (tenantPlatforms != null) {
                this.inMemoryStore.remove(platformCode);
            }
        } else {
            DAOFactory.getPlatformDAO().unregister(tenantDomain, platformCode);
        }
    }

    @Override
    public void addMapping(String tenantDomain, List<String> platformCode) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().addMapping(tenantDomain, platformCode);
    }

    @Override
    public void addMapping(String tenantDomain, String platformCode) throws PlatformManagementException {
        List<String> codes = new ArrayList<>();
        codes.add(platformCode);
        DAOFactory.getPlatformDAO().addMapping(tenantDomain, codes);
    }

    @Override
    public void removeMapping(String tenantDomain, String platformCode) throws PlatformManagementException {
        DAOFactory.getPlatformDAO().removeMapping(tenantDomain, platformCode);
    }
}
