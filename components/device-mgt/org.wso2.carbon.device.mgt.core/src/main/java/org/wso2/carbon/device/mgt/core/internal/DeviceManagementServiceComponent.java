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
import org.wso2.carbon.device.mgt.common.license.mgt.License;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManagementException;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.spi.DeviceMgtService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.DeviceManagementRepository;
import org.wso2.carbon.device.mgt.core.DeviceManagementServiceProviderImpl;
import org.wso2.carbon.device.mgt.core.api.mgt.APIPublisherService;
import org.wso2.carbon.device.mgt.core.api.mgt.APIPublisherServiceImpl;
import org.wso2.carbon.device.mgt.core.api.mgt.APIRegistrationStartupObserver;
import org.wso2.carbon.device.mgt.core.app.mgt.AppManagementServiceImpl;
import org.wso2.carbon.device.mgt.common.app.mgt.AppManagerConnector;
import org.wso2.carbon.device.mgt.common.app.mgt.AppManagerConnectorException;
import org.wso2.carbon.device.mgt.core.app.mgt.RemoteAppManagerConnector;
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
import org.wso2.carbon.device.mgt.core.service.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.util.DeviceManagementSchemaInitializer;
import org.wso2.carbon.device.mgt.user.core.UserManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
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
 * interface="org.wso2.carbon.device.mgt.common.spi.DeviceMgtService"
 * cardinality="0..n"
 * policy="dynamic"
 * bind="setDeviceManager"
 * unbind="unsetDeviceManager"
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
 * @scr.reference name="org.wso2.carbon.device.mgt.usermanager.service"
 * interface="org.wso2.carbon.device.mgt.user.core.UserManager"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setUserManager"
 * unbind="unsetUserManager"
 */
public class DeviceManagementServiceComponent {

	private static Log log = LogFactory.getLog(DeviceManagementServiceComponent.class);
	private DeviceManagementRepository pluginRepository = new DeviceManagementRepository();

    private static final Object LOCK = new Object();
    private boolean isInitialized;
    private List<DeviceMgtService> deviceManagers = new ArrayList<DeviceMgtService>();

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

            DeviceManagementService deviceManagementProvider =
                    new DeviceManagementServiceProviderImpl(this.getPluginRepository());
            DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProvider);
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

            synchronized (LOCK) {
                for (DeviceMgtService deviceManager : deviceManagers) {
                    this.registerDeviceManagementProvider(deviceManager);
                }
                this.isInitialized = true;
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

    private void initAppManagerConnector() throws AppManagerConnectorException {
        AppManagementConfigurationManager.getInstance().initConfig();
        AppManagementConfig appConfig =
                AppManagementConfigurationManager.getInstance().getAppManagementConfig();
        DeviceManagementDataHolder.getInstance().setAppManagerConfig(appConfig);
        RemoteAppManagerConnector appManager = new RemoteAppManagerConnector(appConfig,this.getPluginRepository());
        DeviceManagementDataHolder.getInstance().setAppManager(appManager);
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementServiceImpl");
        }
        /* Registering Device Management Service */
        BundleContext bundleContext = componentContext.getBundleContext();
        bundleContext.registerService(DeviceManagementService.class.getName(),
                new DeviceManagementServiceImpl(), null);

        APIPublisherService publisher = new APIPublisherServiceImpl();
        DeviceManagementDataHolder.getInstance().setApiPublisherService(publisher);
        bundleContext.registerService(APIPublisherService.class, publisher, null);

        bundleContext.registerService(ServerStartupObserver.class, new APIRegistrationStartupObserver(), null);

	     /* Registering App Management service */
	    bundleContext.registerService(AppManagerConnector.class.getName(), new AppManagementServiceImpl(), null);
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

    private void registerDeviceManagementProvider(DeviceMgtService deviceManager) {
        try {
            this.getPluginRepository().addDeviceManagementProvider(deviceManager);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while adding device management provider '" +
                    deviceManager.getProviderType() + "'");
        }
    }

	/**
	 * Sets Device Manager service.
	 *
	 * @param deviceManager An instance of DeviceManager
	 */
	protected void setDeviceManager(DeviceMgtService deviceManager) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Device Management Service Provider: '" + deviceManager.getProviderType() + "'");
        }
        synchronized (LOCK) {
            if (isInitialized) {
                this.registerDeviceManagementProvider(deviceManager);
            }
            deviceManagers.add(deviceManager);
        }
    }

	/**
	 * Unsets Device Management service.
	 *
	 * @param deviceManager An Instance of DeviceManager
	 */
	protected void unsetDeviceManager(DeviceMgtService deviceManager) {
		if (log.isDebugEnabled()) {
			log.debug("Un setting Device Management Service Provider : '" + deviceManager.getProviderType() + "'");
		}
        try {
            this.getPluginRepository().removeDeviceManagementProvider(deviceManager);
        } catch (DeviceManagementException e) {
            log.error("Error occurred while removing device management provider '" +
                    deviceManager.getProviderType() + "'");
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

    /**
     * Sets UserManager Service.
     *
     * @param userMgtService An instance of UserManager
     */
    protected void setUserManager(UserManager userMgtService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting UserManager Service");
        }
        DeviceManagementDataHolder.getInstance().setUserManager(userMgtService);
    }

    /**
     * Unsets UserManager Service.
     *
     * @param userMgtService An instance of UserManager
     */
    protected void unsetUserManager(UserManager userMgtService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting UserManager Service");
        }
        DeviceManagementDataHolder.getInstance().setUserManager(null);
    }

	private DeviceManagementRepository getPluginRepository() {
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
