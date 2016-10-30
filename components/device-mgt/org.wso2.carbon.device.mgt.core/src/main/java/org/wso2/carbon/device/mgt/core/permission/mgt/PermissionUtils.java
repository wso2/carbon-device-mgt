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

package org.wso2.carbon.device.mgt.core.permission.mgt;

import org.w3c.dom.Document;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.permission.mgt.Permission;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagementException;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
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
            return DeviceManagementDataHolder.getInstance().getRegistryService()
                    .getGovernanceSystemRegistry(
                            tenantId);
        } catch (RegistryException e) {
            throw new PermissionManagementException(
                    "Error in retrieving governance registry instance: " +
                            e.getMessage(), e);
        }
    }

    public static String getAbsolutePermissionPath(String permissionPath) {
        return PermissionUtils.ADMIN_PERMISSION_REGISTRY_PATH + permissionPath;
    }

    public static String getAbsoluteContextPathOfAPI(String contextPath, String version, String url) {
        if ((version != null) && !version.isEmpty()) {
            return contextPath + "/" + version + url;
        }
        return contextPath + url;
    }

    public static Permission getPermission(String path) throws PermissionManagementException {
        try {
            Resource resource = PermissionUtils.getGovernanceRegistry().get(path);
            Permission permission = new Permission();
            permission.setName(resource.getProperty(PERMISSION_PROPERTY_NAME));
            permission.setPath(resource.getPath());
            return permission;
        } catch (RegistryException e) {
            throw new PermissionManagementException("Error in retrieving registry resource : " +
                    e.getMessage(), e);
        }
    }

    public static boolean putPermission(Permission permission) throws PermissionManagementException {
        boolean status;
        try {
            StringTokenizer tokenizer = new StringTokenizer(permission.getPath(), "/");
            String lastToken = "", currentToken, tempPath;
            while (tokenizer.hasMoreTokens()) {
                currentToken = tokenizer.nextToken();
                tempPath = lastToken + "/" + currentToken;
                if (!checkResourceExists(tempPath)) {
                    createRegistryCollection(tempPath, currentToken);
                }
                lastToken = tempPath;
            }
            status = true;
        } catch (RegistryException e) {
            throw new PermissionManagementException("Error occurred while persisting permission : " +
                    permission.getName(), e);
        }
        return status;
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

    public static Document convertToDocument(File file) throws PermissionManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new PermissionManagementException("Error occurred while parsing file, while converting " +
                    "to a org.w3c.dom.Document", e);
        }
    }

}
