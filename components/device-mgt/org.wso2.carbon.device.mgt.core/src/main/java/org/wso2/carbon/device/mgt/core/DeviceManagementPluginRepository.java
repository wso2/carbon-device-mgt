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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceTypeIdentifier;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationProvider;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagerStartupListener;
import org.wso2.carbon.device.mgt.core.internal.DeviceMonitoringOperationDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerRepository;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DeviceManagementPluginRepository implements DeviceManagerStartupListener {

    private Map<DeviceTypeIdentifier, DeviceManagementService> providers;
    private boolean isInited;
    private static final Log log = LogFactory.getLog(DeviceManagementPluginRepository.class);
    private OperationManagerRepository operationManagerRepository;

    public DeviceManagementPluginRepository() {
        this.operationManagerRepository = new OperationManagerRepository();
        providers = Collections.synchronizedMap(new HashMap<DeviceTypeIdentifier, DeviceManagementService>());
        DeviceManagementServiceComponent.registerStartupListener(this);
    }

    public void addDeviceManagementProvider(DeviceManagementService provider) throws DeviceManagementException {
        String deviceType = provider.getType().toLowerCase();

        ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
        String tenantDomain = provisioningConfig.getProviderTenantDomain();
        boolean isSharedWithAllTenants = provisioningConfig.isSharedWithAllTenants();
        int tenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        if (tenantId == -1) {
            throw new DeviceManagementException("No tenant available for tenant domain " + tenantDomain);
        }
        synchronized (providers) {
            try {
                if (isInited) {
                    /* Initializing Device Management Service Provider */
                    provider.init();
                    DeviceManagerUtil.registerDeviceType(deviceType, tenantId, isSharedWithAllTenants);
                    DeviceManagementDataHolder.getInstance().setRequireDeviceAuthorization(deviceType,
                                                                                           provider.getDeviceManager()
                                                                                                   .requireDeviceAuthorization());
                    registerPushNotificationStrategy(provider);
                    registerMonitoringTask(provider);
                }
            } catch (DeviceManagementException e) {
                throw new DeviceManagementException("Error occurred while adding device management provider '" +
                                                            deviceType + "'", e);
            }
            if (isSharedWithAllTenants) {
                DeviceTypeIdentifier deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType);
                providers.put(deviceTypeIdentifier, provider);
            } else {
                DeviceTypeIdentifier deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType, tenantId);
                providers.put(deviceTypeIdentifier, provider);
            }
        }
    }

    public void removeDeviceManagementProvider(DeviceManagementService provider)
            throws DeviceManagementException {
            String deviceTypeName = provider.getType().toLowerCase();
            DeviceTypeIdentifier deviceTypeIdentifier;
            ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
            if (provisioningConfig.isSharedWithAllTenants()) {
                deviceTypeIdentifier = new DeviceTypeIdentifier(deviceTypeName);
                providers.remove(deviceTypeIdentifier);
            } else {
                int providerTenantId = DeviceManagerUtil.getTenantId(provisioningConfig.getProviderTenantDomain());
                deviceTypeIdentifier = new DeviceTypeIdentifier(deviceTypeName, providerTenantId);
                providers.remove(deviceTypeIdentifier);
            }
            unregisterPushNotificationStrategy(deviceTypeIdentifier);
            unregisterMonitoringTask(provider);
    }

    private void unregisterPushNotificationStrategy(DeviceTypeIdentifier deviceTypeIdentifier) {
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
        DeviceTypeIdentifier deviceTypeIdentifier = new DeviceTypeIdentifier(type.toLowerCase(), tenantId);
        DeviceManagementService provider = providers.get(deviceTypeIdentifier);
        if (provider == null) {
            deviceTypeIdentifier = new DeviceTypeIdentifier(type.toLowerCase());
            provider = providers.get(deviceTypeIdentifier);
        }
        return provider;
    }

    public Map<DeviceTypeIdentifier, DeviceManagementService> getAllDeviceManagementServices(int tenantId) {
        Map<DeviceTypeIdentifier, DeviceManagementService> tenantProviders = new HashMap<>();
        for (DeviceTypeIdentifier identifier : providers.keySet()) {
            if (identifier.getTenantId() == tenantId || identifier.isSharedWithAllTenant()) {
                tenantProviders.put(identifier, providers.get(identifier));
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
        DeviceTypeIdentifier deviceTypeIdentifier;
        if (isSharedWithAllTenants) {
            deviceTypeIdentifier = new DeviceTypeIdentifier(deviceManagementService.getType());
        } else {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            deviceTypeIdentifier = new DeviceTypeIdentifier(deviceManagementService.getType(), tenantId);
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
            DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance()
                    .getDeviceTaskManagerService();

            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceManagementService
                    .getOperationMonitoringConfig();

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
            DeviceTaskManagerService deviceTaskManagerService = DeviceManagementDataHolder.getInstance()
                    .getDeviceTaskManagerService();
            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceManagementService
                    .getOperationMonitoringConfig();
            if (operationMonitoringTaskConfig != null) {
                deviceTaskManagerService.stopTask(deviceManagementService.getType(),
                        deviceManagementService.getOperationMonitoringConfig());
            }
        } catch (DeviceMgtTaskException e) {
            throw new DeviceManagementException("Error occurred while removing task service for '" +
                    deviceManagementService.getType() + "'", e);
        }
    }

    public OperationManager getOperationManager(String deviceType, int tenantId) {
        //Priority need to be given to the tenant before public.
        DeviceTypeIdentifier deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType.toLowerCase(), tenantId);
        OperationManager operationManager = operationManagerRepository.getOperationManager(deviceTypeIdentifier);
        if (operationManager == null) {
            deviceTypeIdentifier = new DeviceTypeIdentifier(deviceType.toLowerCase());
            operationManager = operationManagerRepository.getOperationManager(deviceTypeIdentifier);
        }
        return operationManager;
    }

    @Override
    public void notifyObserver() {
        String deviceTypeName;
        synchronized (providers) {
            for (DeviceManagementService provider : providers.values()) {
                try {
                    provider.init();
                    deviceTypeName = provider.getType().toLowerCase();
                    ProvisioningConfig provisioningConfig = provider.getProvisioningConfig();
                    int tenantId = DeviceManagerUtil.getTenantId(provisioningConfig.getProviderTenantDomain());
                    DeviceManagerUtil.registerDeviceType(deviceTypeName, tenantId,
                                                         provisioningConfig.isSharedWithAllTenants());
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
            this.isInited = true;
        }
    }
}
