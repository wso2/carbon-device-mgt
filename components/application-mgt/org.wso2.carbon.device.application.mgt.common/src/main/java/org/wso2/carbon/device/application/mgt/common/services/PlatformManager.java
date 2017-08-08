/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.common.exception.PlatformManagementException;

import java.util.List;

/**
 * Platform manager is responsible for handling platforms, which will be used to as a registry of platforms.
 * And will be able to provide the platforms related information to other classes which requires.
 */
public interface PlatformManager {

    /**
     * To initialize the shared platforms for the tenant during the tenant initialization time.
     *
     * @param tenantId ID of the tenant
     * @throws PlatformManagementException Platform Management Exception
     */
    public void initialize(int tenantId) throws PlatformManagementException;

    /**
     * To get platforms of the specific tenant.
     *
     * @param tenantId ID of the tenant
     * @return List of platforms
     * @throws PlatformManagementException Platform Management Exception
     */
    public List<Platform> getPlatforms(int tenantId) throws PlatformManagementException;

    /**
     * To get platform with the given platform identifier and tenant ID.
     *
     * @param tenantId           ID of the tenant
     * @param platformIdentifier Unique identifier of the platform.
     * @return the Specific platform with the platform identifier and tenant
     * @throws PlatformManagementException Platform Management Exception
     */
    public Platform getPlatform(int tenantId, String platformIdentifier) throws PlatformManagementException;

    /**
     * To register a platform under particular tenant.
     *
     * @param tenantId ID of the tenant.
     * @param platform Platform to be registered
     * @throws PlatformManagementException Platform Management Exception
     */
    public void register(int tenantId, Platform platform) throws PlatformManagementException;

    /**
     * To update a platform.
     *
     * @param tenantId              ID of the tenant
     * @param oldPlatformIdentifier Old platform Identifier
     * @param platform              Platform to be updated
     * @throws PlatformManagementException Platform Management Exception
     */
    public void update(int tenantId, String oldPlatformIdentifier, Platform platform)
            throws PlatformManagementException;

    /**
     * To un-register the platform.
     *
     * @param tenantId           ID of the tenant.
     * @param platformIdentifier ID of the platform
     * @param isFileBased        To indicate whether a file based or not.
     * @throws PlatformManagementException Platform Management Exception.
     */
    public void unregister(int tenantId, String platformIdentifier, boolean isFileBased)
            throws PlatformManagementException;

    /**
     * To add mapping to platform identifiers with the tenant ID.
     *
     * @param tenantId            ID of the tenant
     * @param platformIdentifiers Platform Identifiers
     * @throws PlatformManagementException Platform Management Exception
     */
    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementException;

    /**
     * To add mapping to a platform for a tenant.
     *
     * @param tenantId           ID of the tenant.
     * @param platformIdentifier ID of the platform, the mapping should be added.
     * @throws PlatformManagementException Platform Management Exception.
     */
    public void addMapping(int tenantId, String platformIdentifier) throws PlatformManagementException;

    /**
     * To remove a mapping of a platform to a tenant.
     *
     * @param tenantId           ID of the tenant.
     * @param platformIdentifier ID of the platform.
     * @throws PlatformManagementException Platform Management Exception.
     */
    public void removeMapping(int tenantId, String platformIdentifier) throws PlatformManagementException;

    /**
     * To update the platform status(ENABLED / DISABLED).
     *
     * @param tenantId           Id of the tenant
     * @param platformIdentifier ID of the platform
     * @param status             Status to be updated.
     * @throws PlatformManagementException Platform Management Exception.
     */
    public void updatePlatformStatus(int tenantId, String platformIdentifier, String status)
            throws PlatformManagementException;

    /**
     * To remove platforms that belongs to particular tenant.
     *
     * @param tenantId ID of the tenant.
     * @throws PlatformManagementException Platform Management Exception.
     */
    public void removePlatforms(int tenantId) throws PlatformManagementException;

}
