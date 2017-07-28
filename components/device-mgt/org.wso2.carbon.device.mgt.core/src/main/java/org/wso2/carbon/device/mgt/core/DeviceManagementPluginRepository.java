/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.pull.notification.PullNotificationSubscriber;
import org.wso2.carbon.device.mgt.core.dto.DeviceManagementServiceHolder;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeServiceIdentifier;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationProvider;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeDefinitionProvider;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagerStartupListener;
import org.wso2.carbon.device.mgt.core.internal.DeviceMonitoringOperationDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerRepository;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskManagerService;
import org.wso2.carbon.device.mgt.core.status.task.impl.DeviceStatusTaskManagerServiceImpl;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceManagementPluginRepository implements DeviceManagerStartupListener {

    private Map<DeviceTypeServiceIdentifier, DeviceManagementServiceHolder> providers;
    private boolean isInitiated;
    private static final Log log = LogFactory.getLog(DeviceManagementPluginRepository.class);
    private OperationManagerRepository operationManagerRepository;
    private static final long DEFAULT_UPDATE_TIMESTAMP = 900000L;

    public DeviceManagementPluginRepository() {
        this.operationManagerRepository = new OperationManagerRepository();
        providers = Collections.synchronizedMap(new HashMap<DeviceTypeServiceIdentifier, DeviceManagementServiceHolder>());
        DeviceManagementServiceComponent.registerStartupListener(this);
    }

    public void addDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
        String deviceType = provider.getType();
        ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
        String tenantDomain = provisioningConfig.getProviderTenantDomain();
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance().getDeviceManagementConfig();
        boolean isSharedWithAllTenants = provisioningConfig.isSharedWithAllTenants();
        int tenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        if (tenantId == -1) {
            throw new DeviceManagementException("No tenant available for tenant domain " + tenantDomain);
        }
        synchronized (providers) {
            if (isInitiated) {
                /* Initializing Device Management Service Provider */
                provider.init();
                DeviceTypeMetaDefinition deviceTypeDefinition = null;
                if (provider instanceof DeviceTypeDefinitionProvider) {
                    DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(
                            provider.getType());
                    DeviceManagementServiceHolder existingProvider = providers.get(deviceTypeIdentifier);
                    deviceTypeDefinition = ((DeviceTypeDefinitionProvider) provider).getDeviceTypeMetaDefinition();
                    if (existingProvider != null && !(existingProvider.getDeviceManagementService()
                            instanceof DeviceTypeDefinitionProvider)) {
                        throw new DeviceManagementException("Definition of device type " + provider.getType()
                                                                    + " is already available through sharing.");
                    }

                    deviceTypeIdentifier = new DeviceTypeServiceIdentifier(provider.getType(), tenantId);
                    existingProvider = providers.get(deviceTypeIdentifier);
                    if (existingProvider != null) {
                        removeDeviceManagementProvider(provider);
                    }
                }

                DeviceManagerUtil.registerDeviceType(deviceType, tenantId, isSharedWithAllTenants, deviceTypeDefinition);
                DeviceManagementDataHolder.getInstance().setRequireDeviceAuthorization(deviceType,
                                                                                       provider.getDeviceManager()
                                                                                               .requireDeviceAuthorization());
                registerPushNotificationStrategy(provider);
                registerMonitoringTask(provider);
                if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
                    DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(deviceType, tenantId);
                    registerDeviceStatusMonitoringTask(deviceTypeObj, provider);
                }
            }
            DeviceManagementServiceHolder deviceManagementServiceHolder = new DeviceManagementServiceHolder(provider);
            if (isSharedWithAllTenants) {
                DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceType);
                providers.put(deviceTypeIdentifier, deviceManagementServiceHolder);
            } else {
                DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceType, tenantId);
                providers.put(deviceTypeIdentifier, deviceManagementServiceHolder);
            }
        }
    }

    public void removeDeviceManagementProvider(DeviceManagementService provider)
            throws DeviceManagementException {
        String deviceTypeName = provider.getType();
        DeviceManagementConfig deviceManagementConfig = DeviceConfigurationManager.getInstance()
                .getDeviceManagementConfig();
        DeviceTypeServiceIdentifier deviceTypeIdentifier;
        ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
        if (provisioningConfig.isSharedWithAllTenants()) {
            deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceTypeName);
        } else {
            int providerTenantId = DeviceManagerUtil.getTenantId(provisioningConfig.getProviderTenantDomain());
            deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceTypeName, providerTenantId);
        }
        PullNotificationSubscriber pullNotificationSubscriber = provider.getPullNotificationSubscriber();
        if (pullNotificationSubscriber != null) {
            pullNotificationSubscriber.clean();
        }
        providers.remove(deviceTypeIdentifier);
        unregisterPushNotificationStrategy(deviceTypeIdentifier);
        unregisterMonitoringTask(provider);
        if (deviceManagementConfig != null && deviceManagementConfig.getDeviceStatusTaskConfig().isEnabled()) {
            DeviceType deviceTypeObj = DeviceManagerUtil.getDeviceType(deviceTypeIdentifier.getDeviceType(),
                                                                       deviceTypeIdentifier.getTenantId());
            unregisterDeviceStatusMonitoringTask(deviceTypeObj, provider);
        }
    }

    private void unregisterPushNotificationStrategy(DeviceTypeServiceIdentifier deviceTypeIdentifier) {
        OperationManager operationManager = operationManagerRepository.getOperationManager(
                deviceTypeIdentifier);
        if (operationManager != null) {
            NotificationStrategy notificationStrategy = operationManager.getNotificationStrategy();
            if (notificationStrategy != null) {
                notificationStrategy.undeploy();
            }
            operationManagerRepository.removeOperationManager(deviceTypeIdentifier);
        }
    }

    public DeviceManagementService getDeviceManagementService(String type, int tenantId) {
        //Priority need to be given to the tenant before public.
        DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(type, tenantId);
        DeviceManagementServiceHolder provider = providers.get(deviceTypeIdentifier);
        if (provider == null) {
            deviceTypeIdentifier = new DeviceTypeServiceIdentifier(type);
            provider = providers.get(deviceTypeIdentifier);
            if (provider == null) {
                try {
                    DeviceType deviceType = DeviceManagerUtil.getDeviceType(type, tenantId);
                    if (deviceType == null) {
                        return null;
                    }
                    DeviceTypeMetaDefinition deviceTypeMetaDefinition = deviceType.getDeviceTypeMetaDefinition();
                    if (deviceTypeMetaDefinition != null) {
                        DeviceManagementService deviceTypeManagerService = DeviceManagementDataHolder.getInstance()
                                .getDeviceTypeGeneratorService().populateDeviceManagementService(type, deviceTypeMetaDefinition);
                        if (deviceTypeManagerService == null) {
                            log.error("Failing to retrieve the device type service for " + type);
                            return null;
                        }
                        addDeviceManagementProvider(deviceTypeManagerService);
                        deviceTypeIdentifier = new DeviceTypeServiceIdentifier(type, tenantId);
                        provider = providers.get(deviceTypeIdentifier);
                    }
                } catch (DeviceManagementException e) {
                    log.error("Failing to retrieve the device type service for " + type, e);
                    return null;
                }
            }
            if (provider == null) {
                log.error("Device Type Definition not found for " + type);
                return null;
            }
        } else {
            // retrieves per tenant device type management service
            if (provider.getDeviceManagementService() instanceof DeviceTypeDefinitionProvider) {
                //handle updates.
                long updatedTimestamp = provider.getTimestamp();
                if (System.currentTimeMillis() - updatedTimestamp > DEFAULT_UPDATE_TIMESTAMP) {
                    try {
                        DeviceType deviceType = DeviceManagerUtil.getDeviceType(type,tenantId);
                        DeviceTypeMetaDefinition deviceTypeMetaDefinition = deviceType.getDeviceTypeMetaDefinition();
                        if (deviceTypeMetaDefinition != null) {
                            Gson gson = new Gson();
                            String dbStoredDefinition = gson.toJson(deviceTypeMetaDefinition);
                            deviceTypeMetaDefinition = ((DeviceTypeDefinitionProvider)
                                    provider.getDeviceManagementService()).getDeviceTypeMetaDefinition();
                            String cachedDefinition = gson.toJson(deviceTypeMetaDefinition);
                            if (!cachedDefinition.equals(dbStoredDefinition)) {
                                DeviceManagementService deviceTypeManagerService = DeviceManagementDataHolder.getInstance()
                                        .getDeviceTypeGeneratorService().populateDeviceManagementService(type, deviceTypeMetaDefinition);
                                if (deviceTypeManagerService == null) {
                                    log.error("Failing to retrieve the device type service for " + type);
                                    return null;
                                }
                                addDeviceManagementProvider(deviceTypeManagerService);
                                deviceTypeIdentifier = new DeviceTypeServiceIdentifier(type, tenantId);
                                provider = providers.get(deviceTypeIdentifier);
                            } else {
                                provider.setTimestamp(System.currentTimeMillis());
                            }
                        }
                    } catch (DeviceManagementException e) {
                        log.error("Failing to retrieve the device type service for " + type, e);
                        return null;
                    }
                }
            }
        }
        return provider.getDeviceManagementService();
    }

    public Map<DeviceTypeServiceIdentifier, DeviceManagementService> getAllDeviceManagementServices(int tenantId) {
        Map<DeviceTypeServiceIdentifier, DeviceManagementService> tenantProviders = new HashMap<>();
        for (DeviceTypeServiceIdentifier identifier : providers.keySet()) {
            if (identifier.getTenantId() == tenantId || identifier.isSharedWithAllTenant()) {
                tenantProviders.put(identifier, providers.get(identifier).getDeviceManagementService());
            }
        }
        return tenantProviders;
    }

    private void registerPushNotificationStrategy(DeviceManagementService deviceManagementService)
            throws DeviceManagementException {
        PushNotificationConfig pushNoteConfig = deviceManagementService.getPushNotificationConfig();
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                deviceManagementService.getProvisioningConfig().getProviderTenantDomain(), true);
        try {
            boolean isSharedWithAllTenants = deviceManagementService.getProvisioningConfig().isSharedWithAllTenants();
            DeviceTypeServiceIdentifier deviceTypeIdentifier;
            if (isSharedWithAllTenants) {
                deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceManagementService.getType());
            } else {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceManagementService.getType(), tenantId);
            }

            if (pushNoteConfig != null) {
                PushNotificationProvider provider = DeviceManagementDataHolder.getInstance()
                        .getPushNotificationProviderRepository().getProvider(pushNoteConfig.getType());
                if (provider == null) {
                    throw new DeviceManagementException(
                            "No registered push notification provider found for the type: '" +
                                    pushNoteConfig.getType() + "'.");
                }
                NotificationStrategy notificationStrategy = provider.getNotificationStrategy(pushNoteConfig);
                operationManagerRepository.addOperationManager(deviceTypeIdentifier,
                        new OperationManagerImpl(deviceTypeIdentifier.getDeviceType(), notificationStrategy));
            } else {
                operationManagerRepository.addOperationManager(deviceTypeIdentifier,
                        new OperationManagerImpl(deviceTypeIdentifier.getDeviceType()));
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void registerMonitoringTask(DeviceManagementService deviceManagementService)
            throws DeviceManagementException {
        try {
            DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance().
                    getDeviceTaskManagerService();
            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceManagementService.
                    getOperationMonitoringConfig();
            if (operationMonitoringTaskConfig != null && operationMonitoringTaskConfig.isEnabled()) {

                if (deviceTaskManagerService == null) {
                    DeviceMonitoringOperationDataHolder.getInstance().addOperationMonitoringConfigToMap(
                            deviceManagementService.getType(), operationMonitoringTaskConfig);
                } else {
                    deviceTaskManagerService.startTask(deviceManagementService.getType(),
                            operationMonitoringTaskConfig);
                }
            }
        } catch (DeviceMgtTaskException e) {
            throw new DeviceManagementException("Error occurred while adding task service for '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    private void unregisterMonitoringTask(DeviceManagementService deviceManagementService)
            throws DeviceManagementException {
        try {
            DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance().
                    getDeviceTaskManagerService();
            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceManagementService.
                    getOperationMonitoringConfig();
            if (operationMonitoringTaskConfig != null) {
                deviceTaskManagerService.stopTask(deviceManagementService.getType(),
                        deviceManagementService.getOperationMonitoringConfig());
            }
        } catch (DeviceMgtTaskException e) {
            throw new DeviceManagementException("Error occurred while removing task service for '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    private void registerDeviceStatusMonitoringTask(DeviceType deviceType, DeviceManagementService deviceManagementService) throws
            DeviceManagementException {
        DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance().
                getDeviceTaskManagerService();
        DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig = deviceManagementService.getDeviceStatusTaskPluginConfig();
        if (deviceStatusTaskPluginConfig != null && deviceStatusTaskPluginConfig.isRequireStatusMonitoring()) {
            if (deviceTaskManagerService == null) {
                DeviceManagementDataHolder.getInstance().addDeviceStatusTaskPluginConfig(deviceType,
                        deviceStatusTaskPluginConfig);
            } else {
                try {
                    new DeviceStatusTaskManagerServiceImpl().startTask(deviceType, deviceStatusTaskPluginConfig);
                } catch (DeviceStatusTaskException e) {
                    throw new DeviceManagementException("Error occurred while adding Device Status task service for '" +
                            deviceManagementService.getType() + "'", e);
                }
            }
        }
    }

    private void unregisterDeviceStatusMonitoringTask(DeviceType deviceType, DeviceManagementService deviceManagementService) throws
            DeviceManagementException {
        DeviceStatusTaskManagerService deviceStatusTaskManagerService = DeviceManagementDataHolder.getInstance().
                getDeviceStatusTaskManagerService();
        DeviceStatusTaskPluginConfig deviceStatusTaskPluginConfig = deviceManagementService.getDeviceStatusTaskPluginConfig();
        if (deviceStatusTaskPluginConfig != null && deviceStatusTaskPluginConfig.isRequireStatusMonitoring()) {
            try {
                DeviceManagementDataHolder.getInstance().removeDeviceStatusTaskPluginConfig(deviceType);
                deviceStatusTaskManagerService.stopTask(deviceType, deviceStatusTaskPluginConfig);
            } catch (DeviceStatusTaskException e) {
                throw new DeviceManagementException("Error occurred while stopping Device Status task service for '" +
                        deviceManagementService.getType() + "'", e);
            }
        }
    }

    public OperationManager getOperationManager(String deviceType, int tenantId) {
        //Priority need to be given to the tenant before public.
        DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceType, tenantId);
        if (getDeviceManagementService(deviceType, tenantId) == null) {
            return null;
        }
        OperationManager operationManager = operationManagerRepository.getOperationManager(deviceTypeIdentifier);
        if (operationManager == null) {
            deviceTypeIdentifier = new DeviceTypeServiceIdentifier(deviceType);
            operationManager = operationManagerRepository.getOperationManager(deviceTypeIdentifier);
        }
        return operationManager;
    }

    @Override
    public void notifyObserver() {
        String deviceTypeName;
        synchronized (providers) {
            for (DeviceManagementServiceHolder deviceManagementServiceHolder : providers.values()) {
                DeviceManagementService  provider= deviceManagementServiceHolder.getDeviceManagementService();
                try {
                    provider.init();
                    deviceTypeName = provider.getType();
                    ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
                    int tenantId = DeviceManagerUtil.getTenantId(provisioningConfig.getProviderTenantDomain());
                    DeviceTypeMetaDefinition deviceTypeDefinition = null;
                    if (provider instanceof DeviceTypeDefinitionProvider) {
                        deviceTypeDefinition = ((DeviceTypeDefinitionProvider) provider).getDeviceTypeMetaDefinition();

                        DeviceTypeServiceIdentifier deviceTypeIdentifier = new DeviceTypeServiceIdentifier(
                                provider.getType(), tenantId);
                        DeviceManagementServiceHolder existingProvider = providers.get(deviceTypeIdentifier);
                        if (existingProvider != null) {
                            removeDeviceManagementProvider(provider);
                        }
                    }
                    DeviceManagerUtil.registerDeviceType(deviceTypeName, tenantId
                            , provisioningConfig.isSharedWithAllTenants(), deviceTypeDefinition);
                    registerPushNotificationStrategy(provider);
                    registerMonitoringTask(provider);

                    //TODO:
                    //This is a temporory fix.
                    //windows and IOS cannot resolve user info by extracting certs
                    //until fix that, use following variable to enable and disable of checking user authorization.

                    DeviceManagementDataHolder.getInstance().setRequireDeviceAuthorization(provider.getType(),
                            provider.getDeviceManager()
                                    .requireDeviceAuthorization());
                } catch (Throwable e) {
                    /* Throwable is caught intentionally as failure of one plugin - due to invalid start up parameters,
                        etc - should not block the initialization of other device management providers */
                    log.error("Error occurred while initializing device management provider '" +
                            provider.getType() + "'", e);
                }
            }
            this.isInitiated = true;
        }
    }
}
