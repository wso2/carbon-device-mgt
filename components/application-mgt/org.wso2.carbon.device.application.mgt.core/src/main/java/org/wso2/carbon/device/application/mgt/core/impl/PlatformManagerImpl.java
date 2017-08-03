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
            ConnectionManagerUtil.beginDBTransaction();
            List<Platform> platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
            List<String> platformIdentifiers = new ArrayList<>();
            for (Platform platform : platforms) {
                if (!platform.isEnabled() & platform.isDefaultTenantMapping()) {
                    platformIdentifiers.add(platform.getIdentifier());
                }
            }
            DAOFactory.getPlatformDAO().addMapping(tenantId, platformIdentifiers);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while initializing the " + "platforms for the tenant : "
                            + tenantId, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while initializing the " + "platforms for the tenant : " + tenantId,
                    e);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
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
            ConnectionManagerUtil.beginDBTransaction();
            platforms = DAOFactory.getPlatformDAO().getPlatforms(tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException | TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while getting the platforms for the tenant : " + tenantId, e);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
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
                ConnectionManagerUtil.beginDBTransaction();
                platform = DAOFactory.getPlatformDAO().getPlatform(tenantId, identifier);
                ConnectionManagerUtil.commitDBTransaction();
                if (platform != null) {
                    return platform;
                }
            } catch (DBConnectionException | TransactionManagementException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new PlatformManagementDAOException(
                        "Database Connection Exception while trying to get the " + "platform with the id :" + identifier
                                + " for the tenant : " + tenantId, e);
            } catch (PlatformManagementDAOException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw e;
            } finally {
                ConnectionManagerUtil.closeDBConnection();
            }
        } else {
            return new Platform(platform);
        }
        return null;
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
        validateBeforeRegister(tenantId, platform);
        try {
            ConnectionManagerUtil.beginDBTransaction();
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
                    ConnectionManagerUtil.rollbackDBTransaction();
                    throw new PlatformManagementException(
                            "Platform - " + platform.getIdentifier() + " is already registered!");
                }
            }
            if (platform.isDefaultTenantMapping()) {
                try {
                    if (platform.isShared()) {
                        sharePlatformWithOtherTenants(platform.getIdentifier());
                    }
                    DAOFactory.getPlatformDAO().addMapping(tenantId, getListOfString(platform.getIdentifier()));
                } catch (UserStoreException e) {
                    ConnectionManagerUtil.rollbackDBTransaction();
                    throw new PlatformManagementException("Error occurred while assigning the platforms for tenants!",
                            e);
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to register a " + "platform with id " + platform
                            .getIdentifier() + " for tenant " + tenantId);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to register a " + "platform with id " + platform
                            .getIdentifier() + " for tenant " + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void update(int tenantId, String oldPlatformIdentifier, Platform platform) throws
            PlatformManagementException {
        Platform oldPlatform = validateBeforeUpdate(tenantId, oldPlatformIdentifier, platform);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (platform.isFileBased()) {
                Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
                // File based configurations will be updated in the server start-up as well.So in that case, cache,
                // will be empty.
                if (tenantPlatforms != null) {
                    if (tenantPlatforms.get(oldPlatformIdentifier) == null) {
                        throw new PlatformManagementException(
                                "Cannot update platform with identifier " + oldPlatformIdentifier + " as it is not "
                                        + " existing already for the tenant " + tenantId);
                    }
                } else {
                    tenantPlatforms = new HashMap<>();
                    this.inMemoryStore.put(tenantId, tenantPlatforms);
                }
                DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
                platform.setId(oldPlatform.getId());
                tenantPlatforms.put(platform.getIdentifier(), platform);
            } else {
                DAOFactory.getPlatformDAO().update(tenantId, oldPlatformIdentifier, platform);
            }

            try {
                if (platform.isShared() && !oldPlatform.isShared()) {
                    sharePlatformWithOtherTenants(platform.getIdentifier());
                }
            } catch (UserStoreException e) {
                ConnectionManagerUtil.rollbackDBTransaction();
                throw new PlatformManagementException("Error occurred while assigning the platforms for tenants!",
                        e);
            }
            if (!platform.isShared() && oldPlatform.isShared()) {
                DAOFactory.getPlatformDAO().removeMappingTenants(platform.getIdentifier());
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to update " + "platform : " + oldPlatformIdentifier
                            + " of tenant :" + tenantId);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to update " + "platform : " + oldPlatformIdentifier
                            + " of tenant :" + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void unregister(int tenantId, String identifier, boolean isFileBased) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getPlatformDAO().unregister(tenantId, identifier, isFileBased);

            if (isFileBased) {
                Map<String, Platform> tenantPlatforms = this.inMemoryStore.get(tenantId);
                if (tenantPlatforms != null) {
                    tenantPlatforms.remove(identifier);
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Transaction Management Exception while trying to un-register " + "the platform with identifier : "
                            + identifier + " tenant :" + tenantId, e);
        } catch (DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to un-register " + "the platform with identifier : "
                            + identifier + " tenant :" + tenantId, e);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getPlatformDAO().addMapping(tenantId, platformIdentifiers);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException | TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to add tenant " + "mapping for tenant ID : "
                            + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
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
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getPlatformDAO().removeMapping(tenantId, platformIdentifier);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (DBConnectionException | TransactionManagementException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException(
                    "Database Connection Exception while trying to remove tenant mapping for tenant ID : " + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void updatePlatformStatus(int tenantId, String platformIdentifier, String status)
            throws PlatformManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Platform platform = DAOFactory.getPlatformDAO().getPlatform(tenantId, platformIdentifier);

            if (platform == null) {
                ConnectionManagerUtil.commitDBTransaction();
                throw new PlatformManagementException("Platform with identifier : " + platformIdentifier + " does not"
                        + " exist for the tenant with id " + tenantId);
            } else {
                boolean isEnabledNewStatus = status.equalsIgnoreCase("ENABLED");

                // If the platform is already in the same status. No need to enable the platform again
                if (isEnabledNewStatus == platform.isEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Platform with identifier : " + platformIdentifier + " is already in " +
                                (isEnabledNewStatus ? "Enabled" : "Disabled") + " status. No need to update.");
                    }
                    ConnectionManagerUtil.commitDBTransaction();
                    return;
                } else {
                    if (isEnabledNewStatus) {
                        DAOFactory.getPlatformDAO().addMapping(tenantId, getListOfString(platform.getIdentifier()));
                    } else {
                        DAOFactory.getPlatformDAO().removeMapping(tenantId, platform.getIdentifier());
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Platform with identifier : " + platformIdentifier + " successfully " +
                                (isEnabledNewStatus ? "Enabled" : "Disabled"));
                    }
                }
            }
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException | DBConnectionException ex) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException("Database exception while trying to update the status of platform "
                    + "with identifier '" + platformIdentifier + "' for the tenant" + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    @Override
    public void removePlatforms(int tenantId) throws PlatformManagementException {
        try {
            ConnectionManagerUtil.beginDBTransaction();
            DAOFactory.getPlatformDAO().removePlatforms(tenantId);
            ConnectionManagerUtil.commitDBTransaction();
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementDAOException("Database exception while trying to remove all the platforms for"
                    + " the tenant " + tenantId);
        } catch (PlatformManagementDAOException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw e;
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * To share the super-tenant platform with other tenants
     * @param platformIdentifier Identifier of the platform
     * @throws UserStoreException User Store Exception
     * @throws PlatformManagementDAOException Platform Management DAO Exception
     */
    private void sharePlatformWithOtherTenants(String platformIdentifier)
            throws UserStoreException, PlatformManagementDAOException {
        TenantManager tenantManager = DataHolder.getInstance().getRealmService().getTenantManager();
        Tenant[] tenants = tenantManager.getAllTenants();
        for (Tenant tenant : tenants) {
            DAOFactory.getPlatformDAO()
                    .addMapping(tenant.getId(), getListOfString(platformIdentifier));
        }
    }

    /**
     * Validation need to be done before registering the platform
     *
     * @param tenantId ID of the tenant which the platform need to registered to
     * @param platform Platform that need to be registered
     * @throws PlatformManagementException Platform Management Exception
     */
    private void validateBeforeRegister(int tenantId, Platform platform) throws PlatformManagementException {
        validatePlatformSharing(tenantId, platform);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            int existingPlatformId = DAOFactory.getPlatformDAO()
                    .getSuperTenantAndOwnPlatforms(platform.getIdentifier(), tenantId);
            ConnectionManagerUtil.commitDBTransaction();
            if (existingPlatformId != -1) {
                throw new PlatformManagementException(
                        "Another platform exists with the identifier " + platform.getIdentifier() + " in the tenant "
                                + tenantId + " or super-tenant. Please choose a "
                                + "different identifier for your platform");
            }
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementException(
                    "Error while checking pre-conditions before registering" + " platform identifier '" + platform
                            .getIdentifier() + "' for the tenant :" + tenantId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

    /**
     * Validations that need to be done before updating the platform
     *
     * @param tenantId              ID of the tenant
     * @param oldPlatformIdentifier Identifier of the old platform
     * @param platform              Updated platform
     * @return Old platform if all the validation succeeds
     * @throws PlatformManagementException Platform ManagementException
     */
    private Platform validateBeforeUpdate(int tenantId, String oldPlatformIdentifier, Platform platform) throws
            PlatformManagementException {
        validatePlatformSharing(tenantId, platform);
        try {
            ConnectionManagerUtil.beginDBTransaction();
            Platform oldPlatform = DAOFactory.getPlatformDAO().getTenantOwnedPlatform(tenantId, oldPlatformIdentifier);
            if (oldPlatform == null) {
                ConnectionManagerUtil.commitDBTransaction();
                throw new PlatformManagementException(
                        "Cannot update platform. Platform with identifier : " + oldPlatformIdentifier
                                + " does not exist for the tenant : " + tenantId);
            }
            if (platform.getIdentifier() != null && !platform.getIdentifier().equals(oldPlatformIdentifier)) {
                int existingPlatformID = DAOFactory.getPlatformDAO()
                        .getSuperTenantAndOwnPlatforms(platform.getIdentifier(), tenantId);
                ConnectionManagerUtil.commitDBTransaction();
                if (existingPlatformID == -1) {
                    throw new PlatformManagementException(
                            "Cannot update the identifier of the platform from '" + oldPlatformIdentifier + "' to '"
                                    + platform.getIdentifier() + "'. Another platform exists "
                                    + "already with the identifier '" + platform.getIdentifier() + "' for the tenant : "
                                    + tenantId + " or in super-tenant");
                }
            }
            return oldPlatform;
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementException(
                    "Database error while validating the platform update with the " + "platform identifier: "
                            + oldPlatformIdentifier + " for the tenant :" + tenantId);
        } finally {
            ConnectionManagerUtil.closeDBConnection();
        }
    }

        /**
     * To validate whether this platform can be shared or not before registering and updating the platform
     *
     * @param tenantId ID of the tenant
     * @param platform Platform to be validated for sharing
     */
    private void validatePlatformSharing(int tenantId, Platform platform) throws PlatformManagementException {
        if (platform.isShared() && tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new PlatformManagementException(
                    "Platform sharing is a restricted operation, therefore Platform - " + platform.getIdentifier()
                            + " cannot be shared by the tenant domain - " + tenantId);
        }
        try {
            ConnectionManagerUtil.beginDBTransaction();
            if (platform.isShared()) {
                int sharedPlatform = DAOFactory.getPlatformDAO().getMultiTenantPlatforms(platform.getIdentifier());
                ConnectionManagerUtil.commitDBTransaction();
                if (sharedPlatform != -1) {
                    throw new PlatformManagementException(
                            "Platform '" + platform.getIdentifier() + "' cannot be shared as some other tenants have "
                                    + "platforms with the same identifier.");
                }
            }
        } catch (TransactionManagementException | DBConnectionException e) {
            ConnectionManagerUtil.rollbackDBTransaction();
            throw new PlatformManagementException(
                    "Error while checking platform sharing conditions for " + " platform identifier '" + platform
                            .getIdentifier() + "' for the tenant :" + tenantId);
        } finally {
            ConnectionManagerUtil.rollbackDBTransaction();
        }
    }

    /**
     * To get the list of the given platform Identifier
     * @param platformIdentifier Identifier of the Platform
     * @return Platform Identifier as a list
     */
    private List<String> getListOfString(String platformIdentifier) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(platformIdentifier);
        return identifiers;
    }
}
