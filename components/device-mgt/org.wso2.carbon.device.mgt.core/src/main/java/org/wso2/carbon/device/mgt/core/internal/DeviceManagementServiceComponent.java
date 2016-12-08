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
package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManagementException;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfigurationManagementService;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementService;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.permission.mgt.PermissionManagerService;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.DeviceManagementConstants;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagementProviderService;
import org.wso2.carbon.device.mgt.core.app.mgt.ApplicationManagerProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfigurationManager;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.config.tenant.PlatformConfigurationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.GroupManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.notification.mgt.NotificationManagementServiceImpl;
import org.wso2.carbon.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.push.notification.mgt.PushNotificationProviderRepository;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagementSchemaInitializer;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

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
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="email.sender.service"
 * interface="org.wso2.carbon.email.sender.core.service.EmailSenderService"
 * cardinality="0..1"
 * policy="dynamic"
 * bind="setEmailSenderService"
 * unbind="unsetEmailSenderService"
 */
public class DeviceManagementServiceComponent {

    private static final Object LOCK = new Object();
    private static Log log = LogFactory.getLog(DeviceManagementServiceComponent.class);
    private static List<PluginInitializationListener> listeners = new ArrayList<>();
    private static List<DeviceManagementService> deviceManagers = new ArrayList<>();
    private static List<DeviceManagerStartupListener> startupListeners = new ArrayList<>();

    public static void registerPluginInitializationListener(PluginInitializationListener listener) {
        synchronized (LOCK) {
            listeners.add(listener);
            for (DeviceManagementService deviceManagementService : deviceManagers) {
                listener.registerDeviceManagementService(deviceManagementService);
            }
        }
    }

    public static void registerStartupListener(DeviceManagerStartupListener startupListener) {
        startupListeners.add(startupListener);
    }

    public static void notifyStartupListeners() {
        for (DeviceManagerStartupListener startupListener : startupListeners) {
            startupListener.notifyObserver();
        }
    }

    @SuppressWarnings("unused")
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
            GroupManagementDAOFactory.init(dsConfig);
            NotificationManagementDAOFactory.init(dsConfig);
            OperationManagementDAOFactory.init(dsConfig);

            /* Initialize Operation Manager */
            this.initOperationsManager();

            PushNotificationProviderRepository pushNotificationRepo = new PushNotificationProviderRepository();
            List<String> pushNotificationProviders = config.getPushNotificationProviders();
            if (pushNotificationProviders != null) {
                for (String pushNoteProvider : pushNotificationProviders) {
                    pushNotificationRepo.addProvider(pushNoteProvider);
                }
            }
            DeviceManagementDataHolder.getInstance().setPushNotificationProviderRepository(pushNotificationRepo);

            /* If -Dsetup option enabled then create device management database schema */
            String setupOption =
                    System.getProperty(DeviceManagementConstants.Common.SETUP_PROPERTY);
            if (setupOption != null) {
                if (log.isDebugEnabled()) {
                    log.debug("-Dsetup is enabled. Device management repository schema initialization is about to " +
                            "begin");
                }
                this.setupDeviceManagementSchema(dsConfig);
            }

            /* Registering declarative service instances exposed by DeviceManagementServiceComponent */
            this.registerServices(componentContext);

            /* This is a workaround to initialize all Device Management Service Providers after the initialization
             * of Device Management Service component in order to avoid bundle start up order related complications */
            notifyStartupListeners();
            if (log.isDebugEnabled()) {
                log.debug("Device management core bundle has been successfully initialized");
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing device management core bundle", e);
        }
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {
        //do nothing
    }

    private void initOperationsManager() throws OperationManagementException {
        OperationManager operationManager = new OperationManagerImpl();
        DeviceManagementDataHolder.getInstance().setOperationManager(operationManager);
    }

    private void registerServices(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Registering OSGi service DeviceManagementProviderServiceImpl");
        }
        /* Registering Device Management Service */
        BundleContext bundleContext = componentContext.getBundleContext();
        DeviceManagementProviderService deviceManagementProvider = new DeviceManagementProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceManagementProvider);
        bundleContext.registerService(DeviceManagementProviderService.class.getName(), deviceManagementProvider, null);

        /* Registering Group Management Service */
        GroupManagementProviderService groupManagementProvider = new GroupManagementProviderServiceImpl();
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(groupManagementProvider);
        bundleContext.registerService(GroupManagementProviderService.class.getName(), groupManagementProvider, null);

	    /* Registering Tenant Configuration Management Service */
        PlatformConfigurationManagementService
                tenantConfiguration = new PlatformConfigurationManagementServiceImpl();
        bundleContext.registerService(PlatformConfigurationManagementService.class.getName(), tenantConfiguration, null);

        /* Registering Notification Service */
        NotificationManagementService notificationManagementService
                = new NotificationManagementServiceImpl();
        bundleContext.registerService(NotificationManagementService.class.getName(), notificationManagementService, null);

        /* Registering DeviceAccessAuthorization Service */
        DeviceAccessAuthorizationService deviceAccessAuthorizationService = new DeviceAccessAuthorizationServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(deviceAccessAuthorizationService);
        bundleContext.registerService(DeviceAccessAuthorizationService.class.getName(),
                deviceAccessAuthorizationService, null);

	     /* Registering App Management service */
        try {
            AppManagementConfigurationManager.getInstance().initConfig();
            AppManagementConfig appConfig =
                    AppManagementConfigurationManager.getInstance().getAppManagementConfig();
            bundleContext.registerService(ApplicationManagementProviderService.class.getName(),
                    new ApplicationManagerProviderServiceImpl(appConfig), null);
        } catch (ApplicationManagementException e) {
            log.error("Application management service not registered.", e);
        }

        /* Registering PermissionManager Service */
        PermissionManagerService permissionManagerService = PermissionManagerServiceImpl.getInstance();
        bundleContext.registerService(PermissionManagerService.class.getName(), permissionManagerService, null);
    }

    private void setupDeviceManagementSchema(DataSourceConfig config) throws DeviceManagementException {
        DeviceManagementSchemaInitializer initializer = new DeviceManagementSchemaInitializer(config);
        String checkSql = "select * from DM_DEVICE_TYPE";
        try {
            if (!initializer.isDatabaseStructureCreated(checkSql)) {
                log.info("Initializing device management repository database schema");
                initializer.createRegistryDatabase();
            } else {
                log.info("Device management database already exists. Not creating a new database.");
            }
        } catch (Exception e) {
            throw new DeviceManagementException(
                    "Error occurred while initializing Device Management database schema", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Device management metadata repository schema has been successfully initialized");
        }
    }

    /**
     * Sets Device Manager service.
     *
     * @param deviceManagementService An instance of DeviceManagementService
     */
    protected void setDeviceManagementService(DeviceManagementService deviceManagementService) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Setting Device Management Service Provider: '" +
                                  deviceManagementService.getType() + "'");
            }
            synchronized (LOCK) {
                deviceManagers.add(deviceManagementService);
                for (PluginInitializationListener listener : listeners) {
                    listener.registerDeviceManagementService(deviceManagementService);
                }
            }
            log.info("Device Type deployed successfully : " + deviceManagementService.getType() + " for tenant "
                             + deviceManagementService.getProvisioningConfig().getProviderTenantDomain());
        } catch (Throwable e) {
            log.error("Failed to register device management service for device type" + deviceManagementService.getType() +
                            " for tenant " + deviceManagementService.getProvisioningConfig().getProviderTenantDomain(), e);
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
                    deviceManagementService.getType() + "'");
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

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting ConfigurationContextService");
        }
        DeviceManagementDataHolder.getInstance().setConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting ConfigurationContextService");
        }
        DeviceManagementDataHolder.getInstance().setConfigurationContextService(null);
    }

    protected void setEmailSenderService(EmailSenderService emailSenderService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting Email Sender Service");
        }
        DeviceManagementDataHolder.getInstance().setEmailSenderService(emailSenderService);
    }

    protected void unsetEmailSenderService(EmailSenderService emailSenderService) {
        if (log.isDebugEnabled()) {
            log.debug("Un-setting Email Sender Service");
        }
        DeviceManagementDataHolder.getInstance().setEmailSenderService(null);
    }


    protected void setDeviceTaskManagerService(DeviceTaskManagerService emailSenderService) {
        if (log.isDebugEnabled()) {
        }
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(emailSenderService);
    }

    protected void unsetDeviceTaskManagerService(DeviceTaskManagerService emailSenderService) {
        if (log.isDebugEnabled()) {
        }
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
    }
}


