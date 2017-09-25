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

/**
 * SQL Queries specific to Platform.
 */
public class SQLQueries {
    static String queryToGetSupertenantAndOwnPlatforms = "SELECT ID from APPM_PLATFORM where IDENTIFIER "
            + "= ? AND (TENANT_ID = ? OR (TENANT_ID = ? AND IS_SHARED = true))";
    static String queryToGetPlatformId =
            "SELECT ID FROM APPM_PLATFORM WHERE (TENANT_ID=? AND IDENTIFIER=?) OR (IS_SHARED = TRUE AND "
                    + "IDENTIFIER=?)";
    static String queryToGetPlatform =
            "SELECT MAPPING.ID, PLATFORM.IDENTIFIER, PLATFORM.FILE_BASED, PLATFORM.ID, PLATFORM.NAME, PLATFORM"
                    + ".DESCRIPTION, PLATFORM.ICON_NAME, PLATFORM.IS_SHARED, PLATFORM.IS_DEFAULT_TENANT_MAPPING FROM "
                    + "(SELECT * FROM APPM_PLATFORM WHERE IDENTIFIER= ? AND (TENANT_ID=? OR IS_SHARED = TRUE)) "
                    + "PLATFORM LEFT JOIN APPM_PLATFORM_TENANT_MAPPING MAPPING ON PLATFORM.ID = MAPPING.PLATFORM_ID "
                    + "AND MAPPING.TENANT_ID = ?";
    static String queryToGetPlatforms = "SELECT MAPPING.ID, PLATFORM.IDENTIFIER FROM (SELECT * FROM APPM_PLATFORM "
            + "WHERE TENANT_ID=? OR IS_SHARED = TRUE ) PLATFORM LEFT JOIN APPM_PLATFORM_TENANT_MAPPING "
            + "MAPPING ON PLATFORM.ID = MAPPING.PLATFORM_ID AND MAPPING.TENANT_ID = ?";
    static String queryToGetPlatformTags = "SELECT * FROM APPM_PLATFORM_TAG WHERE PLATFORM_ID=?";
}
