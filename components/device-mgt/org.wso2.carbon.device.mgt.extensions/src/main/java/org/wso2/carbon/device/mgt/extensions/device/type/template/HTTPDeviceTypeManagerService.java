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
package org.wso2.carbon.device.mgt.extensions.device.type.template;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.Feature;
import org.wso2.carbon.device.mgt.common.InitialOperationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeDefinitionProvider;
import org.wso2.carbon.device.mgt.common.type.mgt.DeviceTypeMetaDefinition;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Claimable;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.ConfigProperties;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceDetails;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DeviceTypeConfiguration;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Features;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.License;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PolicyMonitoring;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Properties;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Property;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.ProvisioningConfig;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PullNotificationSubscriberConfig;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PushNotificationProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This inherits the capabiliy that is provided through the file based device type manager service.
 * This will create and instance of device management service through a json payload.
 */
public class HTTPDeviceTypeManagerService extends DeviceTypeManagerService implements DeviceTypeDefinitionProvider {

    private DeviceTypeMetaDefinition deviceTypeMetaDefinition;
    private static final String DEFAULT_PULL_NOTIFICATION_CLASS_NAME = "org.wso2.carbon.device.mgt.extensions.pull.notification.PullNotificationSubscriberImpl";

    public HTTPDeviceTypeManagerService(String deviceTypeName, DeviceTypeMetaDefinition deviceTypeMetaDefinition) {
        super(getDeviceTypeConfigIdentifier(deviceTypeName), getDeviceTypeConfiguration(
                deviceTypeName, deviceTypeMetaDefinition));
        this.deviceTypeMetaDefinition = deviceTypeMetaDefinition;
    }

    private static DeviceTypeConfiguration getDeviceTypeConfiguration(String deviceTypeName, DeviceTypeMetaDefinition
            deviceTypeMetaDefinition) {
        DeviceTypeConfiguration deviceTypeConfiguration = new DeviceTypeConfiguration();

        if (deviceTypeMetaDefinition != null) {
            Claimable claimable = new Claimable();
            claimable.setEnabled(deviceTypeMetaDefinition.isClaimable());
            deviceTypeConfiguration.setClaimable(claimable);

            if (deviceTypeMetaDefinition.getProperties() != null &&
                    deviceTypeMetaDefinition.getProperties().size() > 0) {
                DeviceDetails deviceDetails = new DeviceDetails();
                Properties properties = new Properties();
                properties.addProperties(deviceTypeMetaDefinition.getProperties());
                deviceDetails.setProperties(properties);
                deviceTypeConfiguration.setDeviceDetails(deviceDetails);
            }
            if (deviceTypeMetaDefinition.getFeatures() != null && deviceTypeMetaDefinition.getFeatures().size() > 0) {
                Features features = new Features();
                List<org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature> featureList
                        = new ArrayList<>();
                for (Feature feature : deviceTypeMetaDefinition.getFeatures()) {
                    org.wso2.carbon.device.mgt.extensions.device.type.template.config.Feature configFeature = new org
                            .wso2.carbon.device.mgt.extensions.device.type.template.config.Feature();
                    if (feature.getCode() != null && feature.getName() != null) {
                        configFeature.setCode(feature.getCode());
                        configFeature.setDescription(feature.getDescription());
                        configFeature.setName(feature.getName());
                        if (feature.getMetadataEntries() != null && feature.getMetadataEntries().size() > 0) {
                            List<String> metaValues = new ArrayList<>();
                            for (Feature.MetadataEntry metadataEntry : feature.getMetadataEntries()) {
                                metaValues.add(metadataEntry.getValue().toString());
                            }
                            configFeature.setMetaData(metaValues);
                        }
                        featureList.add(configFeature);
                    }
                }
                features.addFeatures(featureList);
                deviceTypeConfiguration.setFeatures(features);
            }

            deviceTypeConfiguration.setName(deviceTypeName);
            //TODO: Add it to the license management service.
//            if (deviceTypeMetaDefinition.getLicense() != null) {
//                License license = new License();
//                license.setLanguage(deviceTypeMetaDefinition.getLicense().getLanguage());
//                license.setText(deviceTypeMetaDefinition.getLicense().getText());
//                license.setVersion(deviceTypeMetaDefinition.getLicense().getVersion());
//                deviceTypeConfiguration.setLicense(license);
//            }
            PolicyMonitoring policyMonitoring = new PolicyMonitoring();
            policyMonitoring.setEnabled(deviceTypeMetaDefinition.isPolicyMonitoringEnabled());
            deviceTypeConfiguration.setPolicyMonitoring(policyMonitoring);

            ProvisioningConfig provisioningConfig = new ProvisioningConfig();
            provisioningConfig.setSharedWithAllTenants(false);
            deviceTypeConfiguration.setProvisioningConfig(provisioningConfig);

            PushNotificationConfig pushNotificationConfig = deviceTypeMetaDefinition.getPushNotificationConfig();
            if (pushNotificationConfig != null) {
                PushNotificationProvider pushNotificationProvider = new PushNotificationProvider();
                pushNotificationProvider.setType(pushNotificationConfig.getType());
                //default schedule value will be true.
                pushNotificationProvider.setScheduled(true);
                if (pushNotificationConfig.getProperties() != null &&
                        pushNotificationConfig.getProperties().size() > 0) {
                    ConfigProperties configProperties = new ConfigProperties();
                    List<Property> properties = new ArrayList<>();
                    for (Map.Entry<String, String> entry : pushNotificationConfig.getProperties().entrySet()) {
                        Property property = new Property();
                        property.setName(entry.getKey());
                        property.setValue(entry.getValue());
                        properties.add(property);
                    }
                    configProperties.addProperties(properties);
                    pushNotificationProvider.setConfigProperties(configProperties);
                }
                pushNotificationProvider.setFileBasedProperties(true);
                deviceTypeConfiguration.setPushNotificationProvider(pushNotificationProvider);
            }

//            This is commented until the task registration handling issue is solved
//            OperationMonitoringTaskConfig operationMonitoringTaskConfig = deviceTypeMetaDefinition.getTaskConfig();
//            if (operationMonitoringTaskConfig != null) {
//                TaskConfiguration taskConfiguration = new TaskConfiguration();
//                taskConfiguration.setEnabled(operationMonitoringTaskConfig.isEnabled());
//                taskConfiguration.setFrequency(operationMonitoringTaskConfig.getFrequency());
//                if (operationMonitoringTaskConfig.getMonitoringOperation() != null) {
//                    List<TaskConfiguration.Operation> operations = new ArrayList<>();
//                    for (MonitoringOperation monitoringOperation : operationMonitoringTaskConfig
//                            .getMonitoringOperation()) {
//                        TaskConfiguration.Operation operation = new TaskConfiguration.Operation();
//                        operation.setOperationName(monitoringOperation.getTaskName());
//                        operation.setRecurrency(monitoringOperation.getRecurrentTimes());
//                        operations.add(operation);
//                    }
//                    taskConfiguration.setOperations(operations);
//                }
//                deviceTypeConfiguration.setTaskConfiguration(taskConfiguration);
//            }

            if (deviceTypeMetaDefinition.getInitialOperationConfig() != null) {
                InitialOperationConfig initialOperationConfig = deviceTypeMetaDefinition.getInitialOperationConfig();
                deviceTypeConfiguration.setOperations(initialOperationConfig.getOperations());
            }
        }
        PullNotificationSubscriberConfig pullNotificationSubscriber = new PullNotificationSubscriberConfig();
        pullNotificationSubscriber.setClassName(DEFAULT_PULL_NOTIFICATION_CLASS_NAME);
        deviceTypeConfiguration.setPullNotificationSubscriberConfig(pullNotificationSubscriber);
        return deviceTypeConfiguration;
    }

    private static DeviceTypeConfigIdentifier getDeviceTypeConfigIdentifier(String deviceType) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return new DeviceTypeConfigIdentifier(deviceType, tenantDomain);
    }

    @Override
    public DeviceTypeMetaDefinition getDeviceTypeMetaDefinition() {
        return deviceTypeMetaDefinition;
    }
}
