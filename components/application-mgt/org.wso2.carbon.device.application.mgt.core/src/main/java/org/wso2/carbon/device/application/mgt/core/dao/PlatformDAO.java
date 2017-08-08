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
package org.wso2.carbon.device.application.mgt.core.dao;

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;

import java.util.List;

/**
 * PlatformDAO defines set of DAO operations that are needed for Platform Management.
 */
public interface PlatformDAO {

    int register(int tenantId, Platform platform) throws PlatformManagementDAOException;

    void update(int tenantId, String oldPlatformIdentifier, Platform platform) throws PlatformManagementDAOException;

    void unregister(int tenantId, String platformIdentifier, boolean isFileBased) throws PlatformManagementDAOException;

    void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementDAOException;

    void removeMapping(int tenantId, String platformIdentifier) throws PlatformManagementDAOException;

    void removeMappingTenants(String platformIdentifier) throws PlatformManagementDAOException;

    List<Platform> getPlatforms(int tenantId) throws PlatformManagementDAOException;

    Platform getPlatform(String tenantDomain, String platformIdentifier) throws PlatformManagementDAOException;

    Platform getPlatform(int tenantId, String identifier) throws PlatformManagementDAOException;

    void removePlatforms(int tenantId) throws PlatformManagementDAOException;

    int getSuperTenantAndOwnPlatforms(String platformIdentifier, int tenantId) throws PlatformManagementDAOException;

    Platform getTenantOwnedPlatform(int tenantId, String platformIdentifier) throws PlatformManagementDAOException;

    int getMultiTenantPlatforms(String identifier) throws PlatformManagementDAOException;

}
