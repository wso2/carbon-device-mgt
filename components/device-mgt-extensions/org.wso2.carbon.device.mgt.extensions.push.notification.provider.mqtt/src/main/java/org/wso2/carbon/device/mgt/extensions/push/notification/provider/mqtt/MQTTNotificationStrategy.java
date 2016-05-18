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

import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.MQTTDataHolder;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.util.MQTTAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.MessageType;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;

import java.util.HashMap;
import java.util.Map;

public class MQTTNotificationStrategy implements NotificationStrategy {

    private static final String MQTT_ADAPTER_PROPERTY_NAME = "mqtt.adapter.name";
    private static final String MQTT_ADAPTER_TOPIC = "mqtt.adapter.topic";
    private static final String MQTT_ADAPTER_NAME = "mqtt.push.notification.publisher";

    public MQTTNotificationStrategy(PushNotificationConfig config) {
        OutputEventAdapterConfiguration adapterConfig = new OutputEventAdapterConfiguration();
        adapterConfig.setType(MQTTAdapterConstants.MQTT_ADAPTER_TYPE);
        adapterConfig.setName(MQTT_ADAPTER_NAME);
        adapterConfig.setMessageFormat(MessageType.JSON);

        Map<String, String> configProperties = new HashMap<String, String>();
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL));
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_USERNAME,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_USERNAME));
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_DCR_URL,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_DCR_URL));
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_CLEAR_SESSION,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_CLEAR_SESSION));
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_SCOPES,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_SCOPES));
        configProperties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_MESSAGE_QOS,
                config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_MESSAGE_QOS));
        adapterConfig.setStaticProperties(configProperties);
        try {
            MQTTDataHolder.getInstance().getOutputEventAdapterService().create(adapterConfig);
        } catch (OutputEventAdapterException e) {
            throw new RuntimeException("Error occurred while initializing MQTT output event adapter", e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        Map<String, String> dynamicProperties = ctx.getProperties();
        dynamicProperties.put("topic", (String) ctx.getOperation().getProperties().get(MQTT_ADAPTER_TOPIC));
        MQTTDataHolder.getInstance().getOutputEventAdapterService().publish(MQTT_ADAPTER_NAME, dynamicProperties,
                ctx.getOperation().getPayLoad());
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

}
