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
package org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.InvalidConfigurationException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.MQTTDataHolder;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.util.MQTTAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MQTTNotificationStrategy implements NotificationStrategy {

    private static final String MQTT_ADAPTER_TOPIC = "mqtt.adapter.topic";
    private String mqttAdapterName;
    private static final Log log = LogFactory.getLog(MQTTNotificationStrategy.class);
    private final PushNotificationConfig config;
    private final String providerTenantDomain;
    private static final Object lockObj = new Object();

    public MQTTNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
        OutputEventAdapterConfiguration adapterConfig = new OutputEventAdapterConfiguration();
        adapterConfig.setType(MQTTAdapterConstants.MQTT_ADAPTER_TYPE);
        adapterConfig.setMessageFormat(MessageType.TEXT);

        Map<String, String> configProperties = new HashMap<String, String>();
        if (config.getProperties() != null && config.getProperties().size() > 0) {
            String brokerUrl = config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL);
            if (brokerUrl != null && !brokerUrl.isEmpty()) {
                configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL, brokerUrl);
            }
            mqttAdapterName = config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_NAME);
            configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_USERNAME,
                                 config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_USERNAME));
            configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_PASSWORD,
                                 config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_PASSWORD));
            configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_CLEAR_SESSION,
                                 config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_CLEAR_SESSION));
            configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_SCOPES,
                                 config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_SCOPES));
            configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_MESSAGE_QOS,
                                 config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_MESSAGE_QOS));
        } else {
            mqttAdapterName = "mqtt.adapter." + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                    .toLowerCase();
        }
        adapterConfig.setName(mqttAdapterName);
        adapterConfig.setStaticProperties(configProperties);
        try {
            synchronized (lockObj) {
                try {
                    MQTTDataHolder.getInstance().getOutputEventAdapterService().isPolled(mqttAdapterName);
                } catch (OutputEventAdapterException e) {
                    //event adapter not created
                    MQTTDataHolder.getInstance().getOutputEventAdapterService().create(adapterConfig);
                }
            }
        } catch (OutputEventAdapterException e) {
            throw new InvalidConfigurationException("Error occurred while initializing MQTT output event adapter", e);
        }
        providerTenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                .toLowerCase();
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        String adapterName = mqttAdapterName;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        if (!providerTenantDomain.equals(tenantDomain)) {
            //this is to handle the device type shared with all tenant mode.

            adapterName = "mqtt.adapter." + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain()
                    .toLowerCase();
            try {
                MQTTDataHolder.getInstance().getOutputEventAdapterService().isPolled(adapterName);
            } catch (OutputEventAdapterException e) {
                //event adapter not created
                synchronized (lockObj) {
                    OutputEventAdapterConfiguration adapterConfig = new OutputEventAdapterConfiguration();
                    adapterConfig.setType(MQTTAdapterConstants.MQTT_ADAPTER_TYPE);
                    adapterConfig.setMessageFormat(MessageType.TEXT);
                    adapterConfig.setName(adapterName);
                    Map<String, String> configProperties = new HashMap<String, String>();
                    adapterConfig.setStaticProperties(configProperties);
                    try {
                        MQTTDataHolder.getInstance().getOutputEventAdapterService().create(adapterConfig);
                    } catch (OutputEventAdapterException e1) {
                        throw new PushNotificationExecutionFailedException
                                ("Error occurred while initializing MQTT output event adapter for shared tenant: "
                                         + tenantDomain, e);
                    }
                }
            }

        }

        Operation operation = ctx.getOperation();
        Properties properties = operation.getProperties();
        if (properties != null && properties.get(MQTT_ADAPTER_TOPIC) != null) {
            Map<String, String> dynamicProperties = new HashMap<>();
            dynamicProperties.put("topic", (String) properties.get(MQTT_ADAPTER_TOPIC));
            MQTTDataHolder.getInstance().getOutputEventAdapterService().publish(adapterName, dynamicProperties,
                    operation.getPayLoad());
        } else {
            if (PolicyOperation.POLICY_OPERATION_CODE.equals(operation.getCode())) {
                PolicyOperation policyOperation = (PolicyOperation) operation;
                List<ProfileOperation> profileOperations = policyOperation.getProfileOperations();
                String deviceType = ctx.getDeviceId().getType();
                String deviceId = ctx.getDeviceId().getId();
                for (ProfileOperation profileOperation : profileOperations) {
                    Map<String, String> dynamicProperties = new HashMap<>();
                    String topic = tenantDomain + "/"
                            + deviceType + "/" + deviceId + "/operation/" + profileOperation.getType()
                            .toString().toLowerCase() + "/" + profileOperation.getCode().toLowerCase();
                    dynamicProperties.put("topic", topic);
                    MQTTDataHolder.getInstance().getOutputEventAdapterService().publish(adapterName, dynamicProperties,
                            profileOperation.getPayLoad());
                }

            } else {
                Map<String, String> dynamicProperties = new HashMap<>();
                String topic = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true) + "/"
                        + ctx.getDeviceId().getType() + "/" + ctx.getDeviceId().getId() + "/operation/"
                        + operation.getType().toString().toLowerCase() + "/" + operation.getCode() + "/" + operation.getId();
                dynamicProperties.put("topic", topic);
                Object payload;
                if ("command".equals(operation.getType().toString().toLowerCase())) {
                    payload = operation.getCode();
                } else {
                    payload = operation.getPayLoad();
                }
                MQTTDataHolder.getInstance().getOutputEventAdapterService().publish(adapterName, dynamicProperties,
                                                                                    payload);

            }

        }


    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {
        MQTTDataHolder.getInstance().getOutputEventAdapterService().destroy(mqttAdapterName);
    }

    @Override
    public PushNotificationConfig getConfig() {
        return config;
    }

}

