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
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;
import org.wso2.carbon.device.application.mgt.common.services.PlatformManager;
import org.wso2.carbon.device.application.mgt.core.dao.common.DAOFactory;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;
import org.wso2.carbon.device.application.mgt.core.internal.DataHolder;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
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
        try {
            ConnectionManagerUtil.beginTransaction();
            List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
            List<String> platformIdentifiers = new ArrayList<>();
            for (Platform platform : platforms) {
                if (!platform.isEnabled() & platform.isDefaultTenantMapping()) {
                    platformIdentifiers.add(platform.getIdentifier());
                }
            }
            DAOFactory.getPlatformDAO().addMapping(tenantId, platformIdentifiers);
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while initializing the " + "platforms for the tenant : "
                            + tenantId, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while initializing the " + "platforms for the tenant : " + tenantId,
                    e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public List<Platform> getPlatforms(int tenantId) throws PlatformManagementException {
        int platformIndex = 0;
        List<Platform> platforms;
        if (log.isDebugEnabled()) {
            log.debug("Request for getting platforms received for the tenant ID " + tenantId + " at "
                    + "PlatformManager level");
        }
        try {
            ConnectionManagerUtil.openConnection();
            platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while getting the platforms for the tenant : " + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
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
            log.debug("Number of effective platforms for the tenant " + tenantId + " : " + platforms.size());
        }
        return platforms;
    }

    @Override
    public Platform getPlatform(int tenantId, String identifier) throws PlatformManagementException {
        Platform platform = getPlatformFromInMemory(tenantId, identifier);
        if (platform == null) {
            try {
                ConnectionManagerUtil.openConnection();
                platform = DAOFactory.getPlatformDAO().getPlatform(tenantId, identifier);
                if (platform != null) {
                    return platform;
                }
            } catch (DBConnectionException e) {
                throw new PlatformManagementDAOException(
                        "Database Connection Exception while trying to get the " + "platform with the id :" + identifier
                                + " for the tenant : " + tenantId, e);
            } finally {
                ConnectionManagerUtil.closeConnection();
            }
        } else {
            return new Platform(platform);
        }
        throw new PlatformManagementException(
                "No platform was found for tenant - " + tenantId + " with platform identifier - " + identifier);
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
        try {
            ConnectionManagerUtil.beginTransaction();
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
                    ConnectionManagerUtil.rollbackTransaction();
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
                            DAOFactory.getPlatformDAO()
                                    .addMapping(tenant.getId(), getListOfString(platform.getIdentifier()));
                        }
                    }
                    DAOFactory.getPlatformDAO().addMapping(tenantId, getListOfString(platform.getIdentifier()));
                } catch (UserStoreException e) {
                    ConnectionManagerUtil.rollbackTransaction();
                    throw new PlatformManagementException("Error occurred while assigning the platforms for tenants!",
                            e);
                }
            }
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to register a " + "platform with id " + platform
                            .getIdentifier() + " for tenant " + tenantId);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to register a " + "platform with id " + platform
                            .getIdentifier() + " for tenant " + tenantId);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public void update(int tenantId, String oldPlatformIdentifier, Platform platform) throws
            PlatformManagementException {
        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformManagementException(
                    "Platform sharing is a restricted operation, therefore Platform - " + platform.getIdentifier()
                            + " cannot be shared by the tenant domain - " + tenantId);
        }
        try {
            ConnectionManagerUtil.beginTransaction();
            Platform oldPlatform = DAOFactory.getPlatformDAO().getPlatform(tenantId, oldPlatformIdentifier);

            if (oldPlatform == null) {
                ConnectionManagerUtil.rollbackTransaction();
                throw new PlatformManagementException(
                        "Cannot update platform. Platform with identifier : " + oldPlatformIdentifier
                                + " does not exist.");
            }
            if (platform.getIdentifier() != null && !platform.getIdentifier().equals(oldPlatformIdentifier)) {
                Platform existingPlatform = DAOFactory.getPlatformDAO().getPlatform(tenantId, platform.getIdentifier());

                if (existingPlatform != null) {
                    ConnectionManagerUtil.rollbackTransaction();
                    throw new PlatformManagementException(
                            "Cannot update the identifier of the platform from '" + oldPlatformIdentifier + "' to '"
                                    + platform.getIdentifier() + "'. Another platform exists "
                                    + "already with the identifier '" + platform.getIdentifier() + "' for the tenant : "
                                    + tenantId);
                }
            }

            if (platform.isFileBased()) {
                Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);

                // File based configurations will be updated in the server start-up as well.So in that case, cache,
                // will be empty.
                if (tenantPlatforms == null) {
                    tenantPlatforms = new HashMap<>();
                    this.inMemoryStore.put(tenantId, tenantPlatforms);
                }
                if (tenantPlatforms.get(platform.getIdentifier()) == null) {
                    tenantPlatforms.put(platform.getIdentifier(), platform);
                }
                DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
                platform.setId(oldPlatform.getId());
                tenantPlatforms.put(platform.getIdentifier(), platform);

            } else {
                DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
            }
            if (platform.isDefaultTenantMapping() && !oldPlatform.isDefaultTenantMapping()) {
                try {
                    if (platform.isShared() && !oldPlatform.isShared()) {
                        TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
                        Tenant[] tenants = tenantManager.getAllTenants();
                        for (Tenant tenant : tenants) {
                            DAOFactory.getPlatformDAO()
                                    .addMapping(tenant.getId(), getListOfString(platform.getIdentifier()));

                        }
                    }
                    DAOFactory.getPlatformDAO().addMapping(tenantId, getListOfString(platform.getIdentifier()));
                } catch (UserStoreException e) {
                    ConnectionManagerUtil.rollbackTransaction();
                    throw new PlatformManagementException("Error occurred while assigning the platforms for tenants!",
                            e);
                }
            }
            if (!platform.isShared() && oldPlatform.isShared()) {
                DAOFactory.getPlatformDAO().removeMappingTenants(platform.getIdentifier());
            }
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to update " + "platform : " + oldPlatformIdentifier
                            + " of tenant :" + tenantId);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to update " + "platform : " + oldPlatformIdentifier
                            + " of tenant :" + tenantId);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    private List<String> getListOfString(String platformIdentifier) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(platformIdentifier);
        return identifiers;
    }

    @Override
    public void unregister(int tenantId, String identifier, boolean isFileBased) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.beginTransaction();
            DAOFactory.getPlatformDAO().unregister(tenantId, identifier, isFileBased);

            if (isFileBased) {
                Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
                if (tenantPlatforms != null) {
                    tenantPlatforms.remove(identifier);
                }
            }
            ConnectionManagerUtil.commitTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to un-register " + "the platform with identifier : "
                            + identifier + " tenant :" + tenantId, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to un-register " + "the platform with identifier : "
                            + identifier + " tenant :" + tenantId, e);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.openConnection();
            DAOFactory.getPlatformDAO().addMapping(tenantId, platformIdentifiers);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to add tenant " + "mapping for tenant ID : "
                            + tenantId);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }

    @Override
    public void addMapping(int tenantId, String platformIdentifier) throws PlatformManagementException {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(platformIdentifier);
        addMapping(tenantId, identifiers);
    }

    @Override
    public void removeMapping(int tenantId, String platformIdentifier) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.openConnection();
            DAOFactory.getPlatformDAO().removeMapping(tenantId, platformIdentifier);
        } catch (DBConnectionException e) {
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to remove tenant mapping for tenant ID : " + tenantId);
        } finally {
            ConnectionManagerUtil.closeConnection();
        }
    }
}
