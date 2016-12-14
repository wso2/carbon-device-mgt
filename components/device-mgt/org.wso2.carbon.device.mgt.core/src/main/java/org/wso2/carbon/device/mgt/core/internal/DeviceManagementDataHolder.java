/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.device.mgt.core.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.authorization.DeviceAccessAuthorizationService;
import org.wso2.carbon.device.mgt.common.license.mgt.LicenseManager;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.app.mgt.config.AppManagementConfig;
import org.wso2.carbon.device.mgt.core.config.license.LicenseConfig;
import org.wso2.carbon.device.mgt.core.push.notification.mgt.PushNotificationProviderRepository;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.email.sender.core.service.EmailSenderService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.HashMap;
import java.util.Map;

public class DeviceManagementDataHolder {

    private static DeviceManagementDataHolder thisInstance = new DeviceManagementDataHolder();
    private RealmService realmService;
    private TenantManager tenantManager;
    private DeviceManagementProviderService deviceManagerProvider;
    private LicenseManager licenseManager;
    private RegistryService registryService;
    private LicenseConfig licenseConfig;
    private ApplicationManager appManager;
    private AppManagementConfig appManagerConfig;
    private OperationManager operationManager;
    private ConfigurationContextService configurationContextService;
    private HashMap<String,Boolean> requireDeviceAuthorization = new HashMap<>();
    private DeviceAccessAuthorizationService deviceAccessAuthorizationService;
    private GroupManagementProviderService groupManagementProviderService;
    private TaskService taskService;
    private EmailSenderService emailSenderService;
    private PushNotificationProviderRepository pushNotificationProviderRepository;
    private DeviceTaskManagerService deviceTaskManagerService;

    private Map<String, OperationMonitoringTaskConfig> map = new HashMap<>();


    public void addToMap(OperationMonitoringTaskConfig taskConfig) {
        this.map.put("aa", taskConfig);
    }

    public Map<String, OperationMonitoringTaskConfig> getMap(){
        return this.map;
    }

    private APIManagerConfiguration apiManagerConfiguration;

    private DeviceManagementDataHolder() {}

    public static DeviceManagementDataHolder getInstance() {
        return thisInstance;
    }

    public RealmService getRealmService() {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
        this.setTenantManager(realmService);
    }

    public TenantManager getTenantManager() {
        return tenantManager;
    }

    private void setTenantManager(RealmService realmService) {
        if (realmService == null) {
            throw new IllegalStateException("Realm service is not initialized properly");
        }
        this.tenantManager = realmService.getTenantManager();
    }

    public DeviceManagementProviderService getDeviceManagementProvider() {
        return deviceManagerProvider;
    }

    public void setDeviceManagementProvider(DeviceManagementProviderService deviceManagerProvider) {
        this.deviceManagerProvider = deviceManagerProvider;
    }

    public GroupManagementProviderService getGroupManagementProviderService() {
        return groupManagementProviderService;
    }

    public void setGroupManagementProviderService(
            GroupManagementProviderService groupManagementProviderService) {
        this.groupManagementProviderService = groupManagementProviderService;
    }

    public RegistryService getRegistryService() {
        if (registryService == null) {
            throw new IllegalStateException("Registry service is not initialized properly");
        }
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public LicenseManager getLicenseManager() {
        return licenseManager;
    }

    public void setLicenseManager(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    public LicenseConfig getLicenseConfig() {
        return licenseConfig;
    }

    public void setLicenseConfig(LicenseConfig licenseConfig) {
        this.licenseConfig = licenseConfig;
    }

    public ApplicationManager getAppManager() {
        return appManager;
    }

    public void setAppManager(ApplicationManager appManager) {
        this.appManager = appManager;
    }

    public AppManagementConfig getAppManagerConfig() {
        return appManagerConfig;
    }

    public void setAppManagerConfig(AppManagementConfig appManagerConfig) {
        this.appManagerConfig = appManagerConfig;
    }

    public OperationManager getOperationManager() {
        return operationManager;
    }

    public void setOperationManager(OperationManager operationManager) {
        this.operationManager = operationManager;
    }

    public ConfigurationContextService getConfigurationContextService() {
        if (configurationContextService == null) {
            throw new IllegalStateException("ConfigurationContext service is not initialized properly");
        }
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public void setRequireDeviceAuthorization(String pluginType, boolean requireAuthentication) {
        requireDeviceAuthorization.put(pluginType,requireAuthentication);
    }

    public boolean requireDeviceAuthorization(String pluginType) {
        return requireDeviceAuthorization.get(pluginType);
    }

    public DeviceAccessAuthorizationService getDeviceAccessAuthorizationService() {
        return deviceAccessAuthorizationService;
    }

    public void setDeviceAccessAuthorizationService(
            DeviceAccessAuthorizationService deviceAccessAuthorizationService) {
        this.deviceAccessAuthorizationService = deviceAccessAuthorizationService;
    }


    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public EmailSenderService getEmailSenderService() {
        return emailSenderService;
    }

    public void setEmailSenderService(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    public void setPushNotificationProviderRepository(
            PushNotificationProviderRepository pushNotificationProviderRepository) {
        this.pushNotificationProviderRepository = pushNotificationProviderRepository;
    }

    public PushNotificationProviderRepository getPushNotificationProviderRepository() {
        return pushNotificationProviderRepository;
    }

    public DeviceTaskManagerService getDeviceTaskManagerService() {
        return deviceTaskManagerService;
    }

    public void setDeviceTaskManagerService(DeviceTaskManagerService deviceTaskManagerService) {
        this.deviceTaskManagerService = deviceTaskManagerService;
    }
}
