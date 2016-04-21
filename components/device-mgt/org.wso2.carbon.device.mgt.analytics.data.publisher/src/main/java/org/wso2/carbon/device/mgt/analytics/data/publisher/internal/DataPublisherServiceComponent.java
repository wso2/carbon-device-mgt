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
package org.wso2.carbon.device.mgt.analytics.data.publisher.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.analytics.data.publisher.config.AnalyticsConfiguration;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherService;
import org.wso2.carbon.device.mgt.analytics.data.publisher.service.EventsPublisherServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.analytics.data.publisher.internal.DataPublisherServiceComponent"
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
 */
public class DataPublisherServiceComponent {

    private ServiceRegistration analyticsServiceRef;
    private static Log log = LogFactory.getLog(DataPublisherServiceComponent.class);

    protected void activate(ComponentContext componentCtx) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device analytics bundle");
            }
            AnalyticsConfiguration.init();

            BundleContext bundleCtx = componentCtx.getBundleContext();
            this.analyticsServiceRef =
                    bundleCtx.registerService(EventsPublisherService.class, new EventsPublisherServiceImpl(), null);

            if (log.isDebugEnabled()) {
                log.debug("Device management analytics bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device analytics bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentCtx) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating device analytics bundle");
        }
        if (analyticsServiceRef != null) {
            analyticsServiceRef.unregister();
        }
        if (log.isDebugEnabled()) {
            log.debug("Device analytics bundle has been successfully deactivated");
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        DataPublisherDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        DataPublisherDataHolder.getInstance().setRegistryService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DataPublisherDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DataPublisherDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        DataPublisherDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        DataPublisherDataHolder.getInstance().setIndexLoaderService(null);
    }

}
