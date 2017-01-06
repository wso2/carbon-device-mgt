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
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
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
import java.util.Properties;

public class MQTTNotificationStrategy implements NotificationStrategy {

    private static final String MQTT_ADAPTER_TOPIC = "mqtt.adapter.topic";
    private String mqttAdapterName;
    private static final Log log = LogFactory.getLog(MQTTNotificationStrategy.class);

    public MQTTNotificationStrategy(PushNotificationConfig config) {
        OutputEventAdapterConfiguration adapterConfig = new OutputEventAdapterConfiguration();
        adapterConfig.setType(MQTTAdapterConstants.MQTT_ADAPTER_TYPE);
        mqttAdapterName = config.getProperty(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_NAME);
        adapterConfig.setName(mqttAdapterName);
        adapterConfig.setMessageFormat(MessageType.TEXT);

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
            throw new InvalidConfigurationException("Error occurred while initializing MQTT output event adapter", e);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        Map<String, String> dynamicProperties = new HashMap<>();
        Operation operation = ctx.getOperation();
        Properties properties = operation.getProperties();
        if (properties != null && properties.get(MQTT_ADAPTER_TOPIC) != null) {
            dynamicProperties.put("topic", (String) properties.get(MQTT_ADAPTER_TOPIC));
        } else {
            String topic = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true) + "/"
                    + ctx.getDeviceId().getType() + "/" + ctx.getDeviceId().getId() + "/" + operation.getType()
                    .toString().toLowerCase() + "/" + operation.getCode();
            dynamicProperties.put("topic", topic);
            if (operation.getPayLoad() == null) {
                operation.setPayLoad("");
            }
        }

        MQTTDataHolder.getInstance().getOutputEventAdapterService().publish(mqttAdapterName, dynamicProperties,
                                                                            operation.getPayLoad());
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {
        MQTTDataHolder.getInstance().getOutputEventAdapterService().destroy(mqttAdapterName);
    }

}
