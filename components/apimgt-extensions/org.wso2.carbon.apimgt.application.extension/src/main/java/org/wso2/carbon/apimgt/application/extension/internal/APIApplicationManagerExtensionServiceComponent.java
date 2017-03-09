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
package org.wso2.carbon.apimgt.application.extension.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderService;
import org.wso2.carbon.apimgt.application.extension.APIManagementProviderServiceImpl;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.apimgt.application.extension.internal.APIApplicationManagerExtensionServiceComponent"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="tenant.indexloader"
 * interface="org.wso2.carbon.registry.indexing.service.TenantIndexingLoader"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setIndexLoader"
 * unbind="unsetIndexLoader"
 * @scr.reference name="realm.service"
 * immediate="true"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="integration.client.service"
 * interface="org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setIntegrationClientService"
 * unbind="unsetIntegrationClientService"
 */
public class APIApplicationManagerExtensionServiceComponent {

    private static Log log = LogFactory.getLog(APIApplicationManagerExtensionServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing device extension bundle");
        }
        APIManagementProviderService apiManagementProviderService = new APIManagementProviderServiceImpl();
        APIApplicationManagerExtensionDataHolder.getInstance().setAPIManagementProviderService(apiManagementProviderService);
        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.registerService(APIManagementProviderService.class.getName(), apiManagementProviderService, null);
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        APIApplicationManagerExtensionDataHolder.getInstance().setIndexLoaderService(null);
    }

    protected void setIntegrationClientService(IntegrationClientService integrationClientService) {
        if (integrationClientService != null && log.isDebugEnabled()) {
            log.debug("integrationClientService initialized");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setIntegrationClientService(integrationClientService);
    }

    protected void unsetIntegrationClientService(IntegrationClientService integrationClientService) {
        APIApplicationManagerExtensionDataHolder.getInstance().setIntegrationClientService(null);
    }

    /**
     * Sets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * Unsets Realm Service.
     *
     * @param realmService An instance of RealmService
     */
    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        APIApplicationManagerExtensionDataHolder.getInstance().setRealmService(null);
    }
}
