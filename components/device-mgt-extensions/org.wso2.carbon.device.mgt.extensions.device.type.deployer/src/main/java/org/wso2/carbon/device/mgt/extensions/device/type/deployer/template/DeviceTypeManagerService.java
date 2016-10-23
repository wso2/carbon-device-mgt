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
package org.wso2.carbon.device.mgt.extensions.device.type.deployer.template;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.DeviceManager;
import org.wso2.carbon.device.mgt.common.ProvisioningConfig;
import org.wso2.carbon.device.mgt.common.app.mgt.ApplicationManager;
import org.wso2.carbon.device.mgt.common.configuration.mgt.ConfigurationEntry;
import org.wso2.carbon.device.mgt.common.configuration.mgt.PlatformConfiguration;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.spi.DeviceManagementService;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.DeviceManagementConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.Property;
import org.wso2.carbon.device.mgt.extensions.device.type.deployer.config.PushNotificationConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the template for device type manager service. This will create and instance of device management service
 * through the configuration file.
 */
public class DeviceTypeManagerService implements DeviceManagementService {

    private static final Log log = LogFactory.getLog(DeviceTypeManagerService.class);

    private DeviceManager deviceManager;
    private PushNotificationConfig pushNotificationConfig;
    private ProvisioningConfig provisioningConfig;
    private String type;

    public DeviceTypeManagerService(DeviceTypeConfigIdentifier deviceTypeConfigIdentifier,
                                    DeviceManagementConfiguration deviceManagementConfiguration) {
        this.setProvisioningConfig(deviceTypeConfigIdentifier.getTenantDomain(), deviceManagementConfiguration);
        this.deviceManager = new DeviceTypeManager(deviceTypeConfigIdentifier, deviceManagementConfiguration);
        this.setType(deviceManagementConfiguration.getDeviceType());
        this.populatePushNotificationConfig(deviceManagementConfiguration.getPushNotificationConfiguration());
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void init() throws DeviceManagementException {
    }

    private void populatePushNotificationConfig(PushNotificationConfiguration sourceConfig) {
        if (sourceConfig != null) {
            if (sourceConfig.isFileBasedProperties()) {
                Map<String, String> staticProps = new HashMap<>();
                for (Property property : sourceConfig.getProperties().getProperty()) {
                    staticProps.put(property.getName(), property.getValue());
                }
                pushNotificationConfig = new PushNotificationConfig(sourceConfig.getPushNotificationProvider(),
                                                                    staticProps);
            } else {
                try {
                    PlatformConfiguration deviceTypeConfig = deviceManager.getConfiguration();
                    if (deviceTypeConfig != null) {
                        List<ConfigurationEntry> configuration = deviceTypeConfig.getConfiguration();
                        if (configuration.size() > 0) {
                            Map<String, String> properties = this.getConfigProperty(configuration);
                            pushNotificationConfig = new PushNotificationConfig(
                                    sourceConfig.getPushNotificationProvider(), properties);
                        }
                    }
                } catch (DeviceManagementException e) {
                    log.error("Unable to get the " + type + " platform configuration from registry.");
                }
            }
        }
    }

    @Override
    public DeviceManager getDeviceManager() {
        return deviceManager;
    }

    @Override
    public ApplicationManager getApplicationManager() {
        return null;
    }

    @Override
    public ProvisioningConfig getProvisioningConfig() {
        return provisioningConfig;
    }

    @Override
    public PushNotificationConfig getPushNotificationConfig() {
        return pushNotificationConfig;
    }

    private void setProvisioningConfig(String tenantDomain, DeviceManagementConfiguration deviceManagementConfiguration) {
        boolean sharedWithAllTenants = deviceManagementConfiguration
                .getManagementRepository().getProvisioningConfig().isSharedWithAllTenants();
        provisioningConfig = new ProvisioningConfig(tenantDomain, sharedWithAllTenants);
    }

    private void setType(String type) {
        this.type = type;
    }

    private Map<String, String> getConfigProperty(List<ConfigurationEntry> configs) {
        Map<String, String> propertMap = new HashMap<>();
        for (ConfigurationEntry entry : configs) {
            propertMap.put(entry.getName(), entry.getValue().toString());
        }
        return propertMap;
    }
}
