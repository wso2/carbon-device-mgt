/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.webapp.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherService;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherServiceImpl;
import org.wso2.carbon.apimgt.webapp.publisher.APIPublisherStartupHandler;
import org.wso2.carbon.apimgt.webapp.publisher.config.WebappPublisherConfig;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="org.wso2.carbon.apimgt.webapp.publisher" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * interface="org.wso2.carbon.apimgt.integration.client.service.IntegrationClientService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setIntegrationClientService"
 * unbind="unsetIntegrationClientService"
 */
public class APIPublisherServiceComponent {

    private static Log log = LogFactory.getLog(APIPublisherServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing webapp publisher bundle");
            }

            if (log.isDebugEnabled()) {
                log.debug("Loading webapp publisher configurations");
            }
            /* Initializing webapp publisher configuration */
            WebappPublisherConfig.init();

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(componentContext);

            if (log.isDebugEnabled()) {
                log.debug("Webapp publisher bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing webapp publisher bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        /* Registering Device Management Service */
        BundleContext bundleContext = componentContext.getBundleContext();

        APIPublisherService publisher = new APIPublisherServiceImpl();
        APIPublisherDataHolder.getInstance().setApiPublisherService(publisher);
        bundleContext.registerService(APIPublisherService.class, publisher, null);
        bundleContext.registerService(ServerStartupObserver.class, new APIPublisherStartupHandler(), null);
    }

    protected void setRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Realm Service");
        }
        APIPublisherDataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting Realm Service");
        }
        APIPublisherDataHolder.getInstance().setRealmService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        APIPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        APIPublisherDataHolder.getInstance().setRegistryService(null);
    }

    protected void setIntegrationClientService(IntegrationClientService integrationClientService) {
        if (integrationClientService != null && log.isDebugEnabled()) {
            log.debug("integrationClientService initialized");
        }
        APIPublisherDataHolder.getInstance().setIntegrationClientService(integrationClientService);
    }

    protected void unsetIntegrationClientService(IntegrationClientService integrationClientService) {
        APIPublisherDataHolder.getInstance().setIntegrationClientService(null);
    }
}
