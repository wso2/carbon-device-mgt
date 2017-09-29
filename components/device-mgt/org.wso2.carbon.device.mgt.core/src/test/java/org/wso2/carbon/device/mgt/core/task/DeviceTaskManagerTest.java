/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.common.push.notification.NotificationStrategy;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.operation.TestNotificationStrategy;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceDetailsRetrieverTask;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeviceTaskManagerTest {

    private static final Log log = LogFactory.getLog(DeviceTaskManagerTest.class);
    private static final String NEW_DEVICE_TYPE = "NEW-DEVICE-TYPE";
    private static final String DEVICE_DETAIL_RETRIEVER_OPPCONFIG = "{\"isEnabled\":true,\"frequency\":60000," +
            "\"monitoringOperation\":[{\"taskName\":\"DEVICE_INFO\",\"recurrentTimes\":2}]}";
    private List<DeviceIdentifier> deviceIds;
    private DeviceTaskManager deviceTaskManager;
    private DeviceManagementProviderService deviceMgtProviderService;
    private OperationManager operationManager;

    @BeforeClass
    public void init() throws DeviceManagementException, RegistryException {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Device Task Manager Test Suite");
        this.deviceIds = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            deviceIds.add(new DeviceIdentifier(UUID.randomUUID().toString(), TestDataHolder.TEST_DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        this.deviceMgtProviderService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(this.deviceMgtProviderService);
        DeviceManagementDataHolder.getInstance()
                .setRegistryService(TestUtils.getRegistryService(DeviceTaskManagerTest.class));
        DeviceManagementDataHolder.getInstance()
                .setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance()
                .setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        NotificationStrategy notificationStrategy = new TestNotificationStrategy();
        this.operationManager = new OperationManagerImpl(TestDataHolder.TEST_DEVICE_TYPE, notificationStrategy);
        this.deviceMgtProviderService.registerDeviceType(
                new TestDeviceManagementService(TestDataHolder.TEST_DEVICE_TYPE, TestDataHolder.SUPER_TENANT_DOMAIN));
        for (Device device : devices) {
            this.deviceMgtProviderService.enrollDevice(device);
        }
        this.deviceTaskManager = new DeviceTaskManagerImpl(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 3));
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testGetTaskFrequency() {
        log.debug("Attempting to retrieve task frequency.");
        try {
            Assert.assertEquals(this.deviceTaskManager.getTaskFrequency(), 60000);
        } catch (DeviceMgtTaskException e) {
            Assert.fail("Exception occurred when obtaining task frequency.", e);
        }
        log.debug("Successfully retrieved task frequency.");
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testIsTaskEnabled() {
        log.debug("Attempting to retrieve task status.");
        try {
            Assert.assertTrue(this.deviceTaskManager.isTaskEnabled());
        } catch (DeviceMgtTaskException e) {
            Assert.fail("Exception occurred when checking whether the task is enabled.", e);
        }
        log.debug("Successfully retrieved task status.");
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testAddOperation() {
        log.debug("Attempting to add operations for devices.");
        try {
            this.deviceTaskManager.addOperations();
            for(DeviceIdentifier deviceId :  deviceIds) {
                List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
                Assert.assertNotNull(operationList);
                Assert.assertEquals(operationList.size(), 3);
            }
        } catch (DeviceMgtTaskException e) {
            Assert.fail("Exception occurred when adding operations to available devices.", e);
        } catch (OperationManagementException e) {
            Assert.fail("Exception occurred when retrieving operations.", e);
        }
        log.debug("Successfully added operations for devices.");
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testAddOperationsWithoutDevices() {
        try {
            this.deviceMgtProviderService.registerDeviceType(
                    new TestDeviceManagementService(NEW_DEVICE_TYPE, TestDataHolder.SUPER_TENANT_DOMAIN));
            DeviceTaskManager taskManager = new DeviceTaskManagerImpl(NEW_DEVICE_TYPE,
                    TestDataHolder.generateMonitoringTaskConfig(true, 50000, 3));
            taskManager.addOperations();
        } catch (DeviceManagementException e) {
            Assert.fail("Unexpected exception occurred", e);
        } catch (DeviceMgtTaskException e) {
            Assert.fail("Exception occurred when adding operations for the devices", e);
        }
    }

    @Test(groups = "Device Task Manager Test Group", dependsOnMethods = "testAddOperationsWithoutDevices")
    public void testAddOperationsWithoutOperations() {
        DeviceTaskManager taskManager = new DeviceTaskManagerImpl(NEW_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 50000, 3));
        try {
            taskManager.addOperations();
        } catch (DeviceMgtTaskException e) {
            Assert.fail("Exception occurred when adding operations for the devices", e);
        }
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testDeviceDetailRetrieverTaskExecute() {
        DeviceDetailsRetrieverTask deviceDetailsRetrieverTask = new DeviceDetailsRetrieverTask();
        Map<String, String> map = new HashMap<>();
        map.put("DEVICE_TYPE", TestDataHolder.TEST_DEVICE_TYPE);
        map.put("OPPCONFIG", DEVICE_DETAIL_RETRIEVER_OPPCONFIG);
        try {
            deviceDetailsRetrieverTask.setProperties(map);
            deviceDetailsRetrieverTask.execute();
            for(DeviceIdentifier deviceId :  deviceIds) {
                List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
                Assert.assertNotNull(operationList);
                Assert.assertEquals(operationList.size(), 4, "Expected number of operations is 4 " +
                        "after adding the device detail retriever operation");
                Assert.assertEquals(operationList.get(0).getCode(), "DEVICE_INFO", "Operation code of " +
                        "the device detail retriever task should be DEVICE_LOCATION");
            }
        } catch (Exception e) {
            Assert.fail("Exception occurred when adding operations for the devices", e);
        }
    }

    @Test(groups = "Device Task Manager Test Group")
    public void testDeviceDetailRetrieverTaskExecuteForAllTenants() {
        DeviceDetailsRetrieverTask deviceDetailsRetrieverTask = new DeviceDetailsRetrieverTask();
        System.setProperty("is.cloud", "true");
        Map<String, String> map = new HashMap<>();
        map.put("DEVICE_TYPE", TestDataHolder.TEST_DEVICE_TYPE);
        map.put("OPPCONFIG", DEVICE_DETAIL_RETRIEVER_OPPCONFIG);
        try {
            deviceDetailsRetrieverTask.setProperties(map);
            deviceDetailsRetrieverTask.execute();
            for(DeviceIdentifier deviceId :  deviceIds) {
                List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
                Assert.assertNotNull(operationList);
                Assert.assertEquals(operationList.size(), 4);
                Assert.assertEquals(operationList.get(0).getCode(), "DEVICE_INFO", "Operation code of " +
                        "the device detail retriever task should be DEVICE_LOCATION");
            }
        } catch (Exception e) {
            Assert.fail("Exception occurred when adding operations for the devices", e);
        }
    }

    @AfterClass
    public void cleanup() throws DeviceManagementException {
        for (DeviceIdentifier deviceId: deviceIds) {
            this.deviceMgtProviderService.disenrollDevice(deviceId);
        }
    }

}
