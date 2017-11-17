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

package org.wso2.carbon.device.mgt.core.internal;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.manager.DeviceTypeManager;
import org.wso2.carbon.device.mgt.core.manager.DeviceTypeManagerImpl;
import org.wso2.carbon.device.mgt.core.spi.DeviceManagement;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Provides device management functionalities through set of managers.
 */
@Component(name = "org.wso2.device.mgt.core.internal.DeviceTypeManagerImpl",
           immediate = true,
           service = {DeviceManagement.class},
           property = {
                   "componentName=wso2-devicemgt-component"
           }
)
public class DeviceManagementComponent implements DeviceManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagementComponent.class);
    private ConfigProvider configProvider;
    private JNDIContextManager jndiContextManager;

    @Activate
    protected void activate(BundleContext bundleContext) {
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DataSource dataSource = (DataSource) ctx.lookup("java:comp/env/jdbc/WSO2CarbonDeviceMgtDB");
            DeviceManagementDAOFactory.init(dataSource);
            LOGGER.debug("Device Management Component started");
        } catch (NamingException e) {
            LOGGER.error("Error while starting Device Management Component", e);
        }
    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        //this is required to enforce a dependency on datasources
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "onJNDIUnregister"
    )
    protected void onJNDIReady(JNDIContextManager jndiContextManager) {
        this.jndiContextManager = jndiContextManager;

    }

    protected void onJNDIUnregister(JNDIContextManager jndiContextManager) {
        this.jndiContextManager = null;
    }

    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        LOGGER.debug("Unregistering carbon device management data source");
    }

    @Override
    public DeviceTypeManager getDeviceTypeManager() {
        return new DeviceTypeManagerImpl(DeviceManagementDAOFactory.getDeviceTypeDAO());
    }

    /**
     * Get the ConfigProvider service. This is the bind method that gets called for ConfigProvider service registration
     * that satisfy the policy.
     *
     * @param configProvider the ConfigProvider service that is registered as a service.
     */
    @Reference(name = "carbon.config.provider",
               service = ConfigProvider.class,
               cardinality = ReferenceCardinality.MANDATORY,
               policy = ReferencePolicy.DYNAMIC,
               unbind = "unregisterConfigProvider")
    protected void registerConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
        LOGGER.debug("An instance of class '{}' unregistered as a Config Provider.",
                     configProvider.getClass().getName());
    }

    /**
     * This is the unbind method for the above reference that gets called for ConfigProvider instance un-registrations.
     *
     * @param configProvider the ConfigProvider service that get unregistered.
     */
    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        this.configProvider = null;
        LOGGER.debug("An instance of class '{}' unregistered as a Config Provider.",
                     configProvider.getClass().getName());
    }

    @Override
    public ConfigProvider getConfigProvider() {
        return this.configProvider;
    }

}
