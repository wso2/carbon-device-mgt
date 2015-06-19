/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementPluginRepository;
import org.wso2.carbon.device.mgt.core.api.mgt.APIPublisherService;
import org.wso2.carbon.device.mgt.core.api.mgt.APIPublisherServiceImpl;
import org.wso2.carbon.device.mgt.core.api.mgt.APIRegistrationStartupObserver;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.app.mgt.RemoteApplicationManager;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfigurationManager;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.license.mgt.LicenseManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.util.DeviceManagementSchemaInitializer;
import org.wso2.carbon.device.mgt.user.core.UserManager;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * @scr.component name="org.wso2.carbon.device.manager" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="device.manager.service"
 * interface="org.wso2.carbon.device.mgt.common.spi.DeviceManagementService"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="api.manager.config.service"
 * interface="org.wso2.carbon.apimgt.impl.APIManagerConfigurationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setAPIManagerConfigurationService"
 * unbind="unsetAPIManagerConfigurationService"
 * @scr.reference name="org.wso2.carbon.ndatasource"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 */
public class DeviceManagementServiceComponent {

    private static Log log = LogFactory.getLog(DeviceManagementServiceComponent.class);
    private DeviceManagementPluginRepository pluginRepository = new DeviceManagementPluginRepository();

    private static final Object LOCK = new Object();
    private static List<PluginInitializationListener> listeners = new ArrayList<PluginInitializationListener>();

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing device management core bundle");
            }
            /* Initializing Device Management Configuration */
            DeviceConfigurationManager.getInstance().initConfig();
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();

            DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
            DeviceManagementDAOFactory.init(dsConfig);

            /* Initializing license manager */
            this.initLicenseManager();
            /*Initialize Operation Manager*/
            this.initOperationsManager();
            /* Initializing app manager connector */
            this.initAppManagerConnector();

            OperationManagementDAOFactory.init(dsConfig);
            /* If -Dsetup option enabled then create device management database schema */
            String setupOption =
                    System.getProperty(DeviceManagementConstants.Common.PROPERTY_SETUP);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Device management repository schema initialization is about to " +
                            "begin");
                }
                this.setupDeviceManagementSchema(dsConfig);
                this.setupDefaultLicenses(DeviceManagementDataHolder.getInstance().getLicenseConfig());
            }

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(componentContext);

            if (log.isDebugEnabled()) {
                log.debug("Device management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device management core bundle", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    public static void registerPluginInitializationListener(PluginInitializationListener listener) {
        listeners.add(listener);
    }

    private void initLicenseManager() throws LicenseManagementException {
        LicenseConfigurationManager.getInstance().initConfig();
        LicenseConfig licenseConfig =
                LicenseConfigurationManager.getInstance().getLicenseConfig();

        LicenseManager licenseManager = new LicenseManagerImpl();
        DeviceManagementDataHolder.getInstance().setLicenseManager(licenseManager);
        DeviceManagementDataHolder.getInstance().setLicenseConfig(licenseConfig);
    }

    private void initOperationsManager() throws OperationManagementException {
        OperationManager operationManager = new OperationManagerImpl();
        DeviceManagementDataHolder.getInstance().setOperationManager(operationManager);
    }

    private void initAppManagerConnector() throws ApplicationManagementException {
        AppManagementConfigurationManager.getInstance().initConfig();
        AppManagementConfig appConfig =
                AppManagementConfigurationManager.getInstance().getAppManagementConfig();
        DeviceManagementDataHolder.getInstance().setAppManagerConfig(appConfig);
        RemoteApplicationManager appManager = new RemoteApplicationManager(appConfig, this.getPluginRepository());
        DeviceManagementDataHolder.getInstance().setAppManager(appManager);
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        /* Registering Device Management Service */
        BundleContext bundleContext = componentContext.getBundleContext();
        DeviceManagementProviderService deviceManagementProvider =
                new DeviceManagementProviderServiceImpl(this.getPluginRepository());
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProvider);
        bundleContext.registerService(DeviceManagementProviderService.class.getName(),
                deviceManagementProvider, null);

        APIPublisherService publisher = new APIPublisherServiceImpl();
        DeviceManagementDataHolder.getInstance().setApiPublisherService(publisher);
        bundleContext.registerService(APIPublisherService.class, publisher, null);

        bundleContext.registerService(ServerStartupObserver.class, new APIRegistrationStartupObserver(), null);

	     /* Registering App Management service */
        bundleContext.registerService(ApplicationManager.class.getName(), new ApplicationManagementServiceImpl(), null);
    }

    private void setupDeviceManagementSchema(DataSourceConfig config)
            throws DeviceManagementException {
        DeviceManagementSchemaInitializer initializer =
                new DeviceManagementSchemaInitializer(config);
        log.info("Initializing device management repository database schema");

        try {
            initializer.createRegistryDatabase();
        } catch (Exception e) {
            throw new DeviceManagementException(
                    "Error occurred while initializing Device Management database schema", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Device management metadata repository schema has been successfully initialized");
        }
    }

    private void setupDefaultLicenses(LicenseConfig licenseConfig)
            throws LicenseManagementException {
        LicenseManager licenseManager = DeviceManagementDataHolder.getInstance().getLicenseManager();
        for (License license : licenseConfig.getLicenses()) {
            License extLicense = licenseManager.getLicense(license.getName(), license.getLanguage());
            if (extLicense == null) {
                licenseManager.addLicense(license.getName(), license);
            }
        }
    }

    /**
     * Sets Device Manager service.
     *
     * @param deviceManagementService An instance of DeviceManagementService
     */
    protected void setDeviceManagementService(DeviceManagementService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service Provider: '" +
                    deviceManagementService.getProviderType() + "'");
        }
        for (PluginInitializationListener listener : listeners) {
            listener.registerDeviceManagementService(deviceManagementService);
        }
    }

    /**
     * Unsets Device Management service.
     *
     * @param deviceManagementService An Instance of DeviceManagementService
     */
    protected void unsetDeviceManagementService(DeviceManagementService deviceManagementService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Device Management Service Provider : '" +
                    deviceManagementService.getProviderType() + "'");
        }
        for (PluginInitializationListener listener : listeners) {
            listener.unregisterDeviceManagementService(deviceManagementService);
        }
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
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
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
        DeviceManagementDataHolder.getInstance().setRealmService(null);
    }

    /**
     * Sets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void setRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Registry Service");
        }
        DeviceManagementDataHolder.getInstance().setRegistryService(registryService);
    }

    /**
     * Unsets Registry Service.
     *
     * @param registryService An instance of RegistryService
     */
    protected void unsetRegistryService(RegistryService registryService) {
        if (log.isDebugEnabled()) {
            log.debug("Un setting Registry Service");
        }
        DeviceManagementDataHolder.getInstance().setRegistryService(null);
    }

    private DeviceManagementPluginRepository getPluginRepository() {
        return pluginRepository;
    }

    protected void setAPIManagerConfigurationService(APIManagerConfigurationService service) {
        //do nothing
    }

    protected void unsetAPIManagerConfigurationService(APIManagerConfigurationService service) {
        //do nothing
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        /* This is to avoid mobile device management component getting initialized before the underlying datasources
        are registered */
        if (log.isDebugEnabled()) {
            log.debug("Data source service set to mobile service component");
        }
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
        //do nothing
    }

}
