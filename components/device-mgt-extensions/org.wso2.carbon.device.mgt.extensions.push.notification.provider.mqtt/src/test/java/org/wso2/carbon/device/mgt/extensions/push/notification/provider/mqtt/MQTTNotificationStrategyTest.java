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
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationContext;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationConfig;
import org.wso2.carbon.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.ProfileOperation;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.MQTTDataHolder;
import org.wso2.carbon.device.mgt.extensions.push.notification.provider.mqtt.internal.util.MQTTAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.CarbonOutputEventAdapterService;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import static org.wso2.carbon.device.mgt.core.operation.mgt.PolicyOperation.POLICY_OPERATION_CODE;

/*
    Unit tests for MQTTNotificationStrategy class
 */
public class MQTTNotificationStrategyTest {
    private MQTTNotificationStrategy mqttNotificationStrategy;
    private CarbonOutputEventAdapterService carbonOutputEventAdapterService;
    private static final String ADAPTER_NAME = "SampleMqttAdapterName";
    private static final String BROKER_URL = "SampleBrokerUrl";
    private PushNotificationConfig pushNotificationConfig;
    private static final String MQTT_ADAPTER_TOPIC = "mqtt.adapter.topic";
    private DeviceIdentifier deviceIdentifier;
    private Operation operation;
    private Map<String, String> propertiesMap;
    private NotificationContext notificationContext;

    @BeforeClass
    public void init() throws NoSuchFieldException, IllegalAccessException, IOException, RegistryException,
            OutputEventAdapterException {
        initializeCarbonContext();
        mqttNotificationStrategy = Mockito.mock(MQTTNotificationStrategy.class, Mockito.CALLS_REAL_METHODS);
        carbonOutputEventAdapterService = Mockito.mock(CarbonOutputEventAdapterService.class,
                Mockito.CALLS_REAL_METHODS);
        Mockito.doReturn(true).when(carbonOutputEventAdapterService).isPolled(Mockito.any());
        Mockito.doNothing().when(carbonOutputEventAdapterService).publish(Mockito.any(), Mockito.any(), Mockito.any());
        Mockito.doNothing().when(carbonOutputEventAdapterService).destroy(ADAPTER_NAME);
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

    @Test(description = "Testing the constructor of MQTTNotificationStrategy class")
    public void getNotificationStrategy() {
        Map<String, String> properties = new HashMap<>();
        properties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_BROKER_URL, BROKER_URL);
        properties.put(MQTTAdapterConstants.MQTT_ADAPTER_PROPERTY_NAME, ADAPTER_NAME);
        pushNotificationConfig = new PushNotificationConfig("MQTT", true, properties);
        MQTTDataHolder mqttDataHolder = MQTTDataHolder.getInstance();
        mqttDataHolder.setOutputEventAdapterService(carbonOutputEventAdapterService);
        mqttNotificationStrategy = new MQTTNotificationStrategy(pushNotificationConfig);
        Assert.assertNotNull(mqttNotificationStrategy, "Null MQTTNotificationStrategy after initializing");
    }

    @Test(dependsOnMethods = {"getNotificationStrategy"}, description = "Testing getConfig method")
    public void getConfigTest() {
        PushNotificationConfig temp = mqttNotificationStrategy.getConfig();
        Assert.assertEquals(temp, pushNotificationConfig, "Not matching pushNotificationConfig received");
    }

    @Test(description = "testing un-deploy method")
    public void testUndeploy() {
        mqttNotificationStrategy.undeploy();
    }

    @Test(description = "testing build context method")
    public void testBuildContext() {
        Assert.assertNull(mqttNotificationStrategy.buildContext(), "not null buildContext received");
    }

    @Test(description = "testing execute method without properties and operation type command")
    public void testExecuteWithoutProperties() throws PushNotificationExecutionFailedException {
        deviceIdentifier = new DeviceIdentifier();
        deviceIdentifier.setId("1");
        deviceIdentifier.setType("SampleDeviceType");
        operation = new Operation();
        operation.setPayLoad(new Object());
        operation.setType(Operation.Type.COMMAND);
        operation.setCode("SampleCode");
        operation.setId(1);
        propertiesMap = new HashMap<>();
        propertiesMap.put(MQTT_ADAPTER_TOPIC, "SampleTopic");
        notificationContext = new NotificationContext(deviceIdentifier, operation);
        notificationContext.setProperties(propertiesMap);
        mqttNotificationStrategy.execute(notificationContext);
    }

    @Test(dependsOnMethods = "testExecuteWithoutProperties", description = "testing execute method without properties " +
            "and operation type config")
    public void testExecutionWithoutPropertiesNonCommandType() throws PushNotificationExecutionFailedException {
        operation.setType(Operation.Type.CONFIG);
        operation.setProperties(null);
        notificationContext = new NotificationContext(deviceIdentifier, operation);
        mqttNotificationStrategy.execute(notificationContext);
    }

    @Test(dependsOnMethods = {"testExecutionWithoutPropertiesNonCommandType"}, description = "test execute method " +
            "with a profile operation")
    public void testExecutePolicyOperation() throws PushNotificationExecutionFailedException {
        PolicyOperation policyOperation = new PolicyOperation();
        policyOperation.setCode(POLICY_OPERATION_CODE);
        policyOperation.setProperties(null);
        ProfileOperation profileOperation = new ProfileOperation();
        profileOperation.setActivityId("1");
        profileOperation.setCode("SampleCode");
        List<ProfileOperation> profileOperationList = new ArrayList<>();
        profileOperationList.add(profileOperation);
        policyOperation.setProfileOperations(profileOperationList);
        notificationContext = new NotificationContext(deviceIdentifier, policyOperation);
        mqttNotificationStrategy.execute(notificationContext);
    }

    @Test(dependsOnMethods = "testExecuteWithoutProperties", description = "testing execute method with properties")
    public void testExecute() throws PushNotificationExecutionFailedException {
        Properties properties = new Properties();
        properties.setProperty(MQTT_ADAPTER_TOPIC, "SampleTopic");
        operation.setProperties(properties);
        notificationContext = new NotificationContext(deviceIdentifier, operation);
        notificationContext.setProperties(propertiesMap);
        mqttNotificationStrategy.execute(notificationContext);
    }

    @Test(dependsOnMethods = {"testExecute"}, description = "testing execute method without the default tenant domain")
    public void testExecutionWithoutTenantDomain() throws NoSuchFieldException, IllegalAccessException,
            PushNotificationExecutionFailedException {
        Field providerTenantDomain = MQTTNotificationStrategy.class.getDeclaredField("providerTenantDomain");
        providerTenantDomain.setAccessible(true);
        providerTenantDomain.set(mqttNotificationStrategy, "SampleTenantDomain");
        mqttNotificationStrategy.execute(notificationContext);
    }
}
