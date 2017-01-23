/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.application.extension.api.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;
import java.util.StringTokenizer;

/**
 * This class provides utility functions used by REST-API.
 */
public class APIUtil {

    private static Log log = LogFactory.getLog(APIUtil.class);
    private static final String DEFAULT_CDMF_API_TAG = "device_management";
    private static final String DEFAULT_CERT_API_TAG = "scep_management";
    public static final String PERMISSION_PROPERTY_NAME = "name";

    public static String getAuthenticatedUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        String username = threadLocalCarbonContext.getUsername();
        String tenantDomain = threadLocalCarbonContext.getTenantDomain();
        if (username.endsWith(tenantDomain)) {
            return username.substring(0, username.lastIndexOf("@"));
        }
        return username;
    }

    public static String getTenantDomainOftheUser() {
        PrivilegedCarbonContext threadLocalCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        return threadLocalCarbonContext.getTenantDomain();
    }

    public static APIManagementProviderService getAPIManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        APIManagementProviderService apiManagementProviderService =
                (APIManagementProviderService) ctx.getOSGiService(APIManagementProviderService.class, null);
        if (apiManagementProviderService == null) {
            String msg = "API management provider service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return apiManagementProviderService;
    }

    public static RealmService getRealmService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService =
                (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return realmService;
    }

    public static DeviceManagementProviderService getDeviceManagementProviderService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        DeviceManagementProviderService deviceManagementProviderService =
                (DeviceManagementProviderService) ctx.getOSGiService(DeviceManagementProviderService.class, null);
        if (deviceManagementProviderService == null) {
            String msg = "Device Management service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return deviceManagementProviderService;
    }

    public static List<String> getAllowedApisTags() throws DeviceManagementException {
        //Todo get allowed cdmf service tags from config.
        List<String> allowedApisTags = getDeviceManagementProviderService().getAvailableDeviceTypes();
        allowedApisTags.add(DEFAULT_CDMF_API_TAG);
        allowedApisTags.add(DEFAULT_CERT_API_TAG);
        return allowedApisTags;
    }

    public static void putPermission(String permission) {
        try {
            StringTokenizer tokenizer = new StringTokenizer(permission, "/");
            String lastToken = "", currentToken, tempPath;
            while (tokenizer.hasMoreTokens()) {
                currentToken = tokenizer.nextToken();
                tempPath = lastToken + "/" + currentToken;
                if (!checkResourceExists(tempPath)) {
                    createRegistryCollection(tempPath, currentToken);

                }
                lastToken = tempPath;
            }
        } catch (org.wso2.carbon.registry.api.RegistryException e) {
            log.error("Failed to creation permission in registry" + permission, e);
        }
    }

    public static void createRegistryCollection(String path, String resourceName)
            throws org.wso2.carbon.registry.api.RegistryException {
        Resource resource = getGovernanceRegistry().newCollection();
        resource.addProperty(PERMISSION_PROPERTY_NAME, resourceName);
        getGovernanceRegistry().beginTransaction();
        getGovernanceRegistry().put(path, resource);
        getGovernanceRegistry().commitTransaction();
    }

    public static boolean checkResourceExists(String path)
            throws RegistryException {
        return getGovernanceRegistry().resourceExists(path);
    }

    public static Registry getGovernanceRegistry() throws RegistryException {
        return getRegistryService().getGovernanceSystemRegistry(MultitenantConstants.SUPER_TENANT_ID);
    }

    public static RegistryService getRegistryService() {
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RegistryService registryService =
                (RegistryService) ctx.getOSGiService(RegistryService.class, null);
        if (registryService == null) {
            String msg = "registry service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        return registryService;
    }
}
