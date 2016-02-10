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
package org.wso2.carbon.device.mgt.analytics.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsService;
import org.wso2.carbon.device.mgt.analytics.service.DeviceAnalyticsServiceImpl;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;

/**
 * @scr.component name="org.wso2.carbon.device.mgt.analytics.internal.DeviceAnalyticsServiceComponent"
 * immediate="true"
 * @scr.reference name="device.analytics.api"
 * interface="org.wso2.carbon.analytics.api.AnalyticsDataAPI"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setAnalyticsDataAPI"
 * unbind="unsetAnalyticsDataAPI"
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
public class DeviceAnalyticsServiceComponent {

    private static Log log = LogFactory.getLog(DeviceAnalyticsServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing device analytics bundle");
        }

        BundleContext bundleContext = componentContext.getBundleContext();

        bundleContext.registerService(DeviceAnalyticsService.class, new DeviceAnalyticsServiceImpl(), null);

        if (log.isDebugEnabled()) {
            log.debug("Device management analytics bundle has been successfully initialized");
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    /**
     * Sets AnalyticsDataAPI Service.
     *
     * @param analyticsDataAPI An instance of AnalyticsDataAPI
     */
    protected void setAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        if (log.isDebugEnabled()) {
            log.debug("Setting AnalyticsDataAPI Service");
        }
        DeviceAnalyticsDataHolder.getInstance().setAnalyticsDataAPI(analyticsDataAPI);
    }

    /**
     * Un sets AnalyticsDataAPI Service.
     *
     * @param analyticsDataAPI An instance of AnalyticsDataAPI
     */
    protected void unsetAnalyticsDataAPI(AnalyticsDataAPI analyticsDataAPI) {
        if (log.isDebugEnabled()) {
            log.debug("Un-Setting AnalyticsDataAPI Service");
        }
        DeviceAnalyticsDataHolder.getInstance().setAnalyticsDataAPI(null);
    }
    protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        DeviceAnalyticsDataHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        DeviceAnalyticsDataHolder.getInstance().setRegistryService(null);
    }

    protected void setTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DeviceAnalyticsDataHolder.getInstance().setTenantRegistryLoader(tenantRegistryLoader);
    }

    protected void unsetTenantRegistryLoader(TenantRegistryLoader tenantRegistryLoader) {
        DeviceAnalyticsDataHolder.getInstance().setTenantRegistryLoader(null);
    }

    protected void setIndexLoader(TenantIndexingLoader indexLoader) {
        if (indexLoader != null && log.isDebugEnabled()) {
            log.debug("IndexLoader service initialized");
        }
        DeviceAnalyticsDataHolder.getInstance().setIndexLoaderService(indexLoader);
    }

    protected void unsetIndexLoader(TenantIndexingLoader indexLoader) {
        DeviceAnalyticsDataHolder.getInstance().setIndexLoaderService(null);
    }
}
