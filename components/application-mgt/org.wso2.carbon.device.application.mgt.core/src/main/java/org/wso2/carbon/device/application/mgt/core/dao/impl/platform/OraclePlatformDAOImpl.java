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

package org.wso2.carbon.device.application.mgt.core.dao.impl.platform;

import org.wso2.carbon.device.application.mgt.common.Platform;
import org.wso2.carbon.device.application.mgt.core.exception.PlatformManagementDAOException;

import java.util.List;

/**
 * Oracle specific implementation for Platform DAO.
 */
public class OraclePlatformDAOImpl extends GenericPlatformDAOImpl {

    @Override
    public int getSuperTenantAndOwnPlatforms(String platformIdentifier, int tenantId)
            throws PlatformManagementDAOException {
        SQLQueries.queryToGetSupertenantAndOwnPlatforms = "SELECT ID from APPM_PLATFORM where IDENTIFIER "
                + "= ? AND (TENANT_ID = ? OR (TENANT_ID = ? AND IS_SHARED = 1))";
        return super.getSuperTenantAndOwnPlatforms(platformIdentifier, tenantId);
    }

    @Override
    public int register(int tenantId, Platform platform) throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatformId =
                "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_ID=? AND IDENTIFIER=?) OR (IS_SHARED = 1 AND "
                        + "IDENTIFIER=?)";
        return super.register(tenantId, platform);
    }

    @Override
    public Platform getPlatform(int tenantId, String identifier) throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatform =
                "SELECT MAPPING.ID, PLATFORM.IDENTIFIER, PLATFORM.FILE_BASED, PLATFORM.ID, PLATFORM.NAME, "
                        + "PLATFORM.DESCRIPTION, PLATFORM.ICON_NAME, PLATFORM.IS_SHARED, "
                        + "PLATFORM.IS_DEFAULT_TENANT_MAPPING FROM (SELECT * FROM APPM_PLATFORM WHERE IDENTIFIER= ? "
                        + "AND (TENANT_ID=? OR IS_SHARED = 1)) PLATFORM LEFT JOIN APPM_PLATFORM_TENANT_MAPPING "
                        + "MAPPING ON PLATFORM.ID = MAPPING.PLATFORM_ID AND MAPPING.TENANT_ID = ?";
        return super.getPlatform(tenantId, identifier);
    }

    public void removeMappingTenants(String platformIdentifier) throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatformId =
                "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_ID=? AND IDENTIFIER=?) OR (IS_SHARED = 1 AND "
                        + "IDENTIFIER=?)";
        super.removeMappingTenants(platformIdentifier);
    }

    public void update(int tenantId, String oldPlatformIdentifier, Platform platform)
            throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatformId =
                "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_ID=? AND IDENTIFIER=?) OR (IS_SHARED = 1 AND "
                        + "IDENTIFIER=?)";
        super.update(tenantId, oldPlatformIdentifier, platform);
    }

    public void addMapping(int tenantId, List<String> platformIdentifiers) throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatformId =
                "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_ID=? AND IDENTIFIER=?) OR (IS_SHARED = 1 AND "
                        + "IDENTIFIER=?)";
        super.addMapping(tenantId, platformIdentifiers);
    }

    public List<Platform> getPlatforms(int tenantId) throws PlatformManagementDAOException {
        SQLQueries.queryToGetPlatforms = "SELECT MAPPING.ID, PLATFORM.IDENTIFIER FROM (SELECT * FROM APPM_PLATFORM "
                + "WHERE TENANT_ID=? OR IS_SHARED = 1) PLATFORM LEFT JOIN APPM_PLATFORM_TENANT_MAPPING "
                + "MAPPING ON PLATFORM.ID = MAPPING.PLATFORM_ID AND MAPPING.TENANT_ID = ?";
        return super.getPlatforms(tenantId);
    }
}
