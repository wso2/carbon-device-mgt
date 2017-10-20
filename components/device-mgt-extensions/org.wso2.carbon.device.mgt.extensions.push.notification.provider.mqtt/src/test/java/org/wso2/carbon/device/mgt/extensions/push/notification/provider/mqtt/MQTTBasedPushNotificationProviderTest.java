/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.MQTTDataHolder;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.util.MQTTAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.CarbonOutputEventAdapterService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/*
    Unit tests for MQTTBasedPushNotificationProvider class
 */
public class MQTTBasedPushNotificationProviderTest {
    private MQTTBasedPushNotificationProvider mqttBasedPushNotificationProvider;
    private CarbonOutputEventAdapterService carbonOutputEventAdapterService;
    private static final String ADAPTER_NAME = "SampleMqttAdapterName";
    private static final String BROKER_URL = "SampleBrokerUrl";

    @BeforeClass
    public void init() throws NoSuchFieldException, IllegalAccessException, IOException, RegistryException,
            OutputEventAdapterException {
        initializeCarbonContext();
        mqttBasedPushNotificationProvider = Mockito.mock(MQTTBasedPushNotificationProvider.class,
                Mockito.CALLS_REAL_METHODS);
        carbonOutputEventAdapterService = Mockito.mock(CarbonOutputEventAdapterService.class,
                Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(true).when(carbonOutputEventAdapterService).isPolled(ADAPTER_NAME);
    }

    private void initializeCarbonContext() throws IOException, RegistryException {
        if (System.getProperty("carbon.home") == null) {
            File file = new File("src/test/resources");
            if (file.exists()) {
                System.setProperty("carbon.home", file.getAbsolutePath());
            }
        }
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_ID);
    }

    @Test(description = "test getType method")
    public void testGetType() {
        String type = mqttBasedPushNotificationProvider.getType();
        Assert.assertEquals(type, "MQTT");
    }

    @Test(description = "test get notification strategy method")
    public void getNotificationStrategy() {
        Map<String, String> properties = new HashMap<>();
        properties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL, BROKER_URL);
        properties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_NAME, ADAPTER_NAME);
        PushNotificationConfig pushNotificationConfig = new PushNotificationConfig("MQTT", true, properties);
        MQTTDataHolder mqttDataHolder = MQTTDataHolder.getInstance();
        mqttDataHolder.setOutputEventAdapterService(carbonOutputEventAdapterService);
        NotificationStrategy notificationStrategy = mqttBasedPushNotificationProvider.
                getNotificationStrategy(pushNotificationConfig);
        Assert.assertNotNull(notificationStrategy,"null notificationStrategyReceived");
    }
}
