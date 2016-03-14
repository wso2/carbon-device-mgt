/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.extensions.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.extensions.api.mgt.APIManagementProviderService;
import org.wso2.carbon.device.mgt.extensions.api.mgt.APIManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.extensions.jwt.client.util.JWTClientUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.IOException;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.analytics.internal.DeviceExtensionServiceComponent"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="tenant.registryloader"
 * interface="org.wso2.carbon.registry.core.service.TenantRegistryLoader"
 * cardinality="1..1" policy="dynamic"
 * bind="setTenantRegistryLoader"
 * unbind="unsetTenantRegistryLoader"
 * @scr.reference name="tenant.indexloader"
 * interface="org.wso2.carbon.registry.indexing.service.TenantIndexingLoader"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setIndexLoader"
 * unbind="unsetIndexLoader"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class DeviceExtensionServiceComponent {

    private static Log log = LogFactory.getLog(DeviceExtensionServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing device extension bundle");
        }
        try {
            JWTClientUtil.initialize();
            /* Registering API Management service */
            APIManagementProviderService apiManagementProviderService = new APIManagementProviderServiceImpl();
            DeviceExtensionDataHolder.getInstance().setAPIManagementProviderService(apiManagementProviderService);
            BundleContext bundleContext = componentContext.getBundleContext();
            bundleContext.registerService(APIManagementProviderService.class.getName(), apiManagementProviderService, null);
        } catch (RegistryException e) {
            log.error("Failed loading the jwt config from registry.", e);
        } catch (IOException e) {
            log.error("Failed loading the jwt config from the file system.", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        DeviceExtensionDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        DeviceExtensionDataHolder.getInstance().setRegistryService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DeviceExtensionDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DeviceExtensionDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        DeviceExtensionDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        DeviceExtensionDataHolder.getInstance().setIndexLoaderService(null);
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
        DeviceExtensionDataHolder.getInstance().setRealmService(realmService);
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
        DeviceExtensionDataHolder.getInstance().setRealmService(null);
    }
}
