/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.webapp.publisher.lifecycle.util;

import org.wso2.carbon.apimgt.webapp.publisher.config.PermissionManagementException;
import org.wso2.carbon.apimgt.webapp.publisher.internal.APIPublisherDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import java.util.StringTokenizer;

/**
 * Utility class which holds necessary utility methods required for persisting permissions in
 * registry.
 */
public class PermissionUtils {

    public static final String ADMIN_PERMISSION_REGISTRY_PATH = "/permission/admin";
    public static final String PERMISSION_PROPERTY_NAME = "name";

    public static Registry getGovernanceRegistry() throws PermissionManagementException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            return APIPublisherDataHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(
                            tenantId);
        } catch (RegistryException e) {
            throw new PermissionManagementException(
                    "Error in retrieving governance registry instance: " +
                            e.getMessage(), e);
        }
    }

    public static void addPermission(String permission) throws PermissionManagementException {
        String resourcePermission = getAbsolutePermissionPath(permission);
        try {
            StringTokenizer tokenizer = new StringTokenizer(resourcePermission, "/");
            String lastToken = "", currentToken, tempPath;
            while (tokenizer.hasMoreTokens()) {
                currentToken = tokenizer.nextToken();
                tempPath = lastToken + "/" + currentToken;
                if (!checkResourceExists(tempPath)) {
                    createRegistryCollection(tempPath, currentToken);
                }
                lastToken = tempPath;
            }
        } catch (RegistryException e) {
            throw new PermissionManagementException("Error occurred while persisting permission : " +
                    resourcePermission, e);
        }
    }

    public static void createRegistryCollection(String path, String resourceName)
            throws PermissionManagementException,
            RegistryException {
        Resource resource = PermissionUtils.getGovernanceRegistry().newCollection();
        resource.addProperty(PERMISSION_PROPERTY_NAME, resourceName);
        PermissionUtils.getGovernanceRegistry().beginTransaction();
        PermissionUtils.getGovernanceRegistry().put(path, resource);
        PermissionUtils.getGovernanceRegistry().commitTransaction();
    }

    public static boolean checkResourceExists(String path)
            throws PermissionManagementException,
            org.wso2.carbon.registry.core.exceptions.RegistryException {
        return PermissionUtils.getGovernanceRegistry().resourceExists(path);
    }

    private static String getAbsolutePermissionPath(String permissionPath) {
        return PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH + permissionPath;
    }

}
