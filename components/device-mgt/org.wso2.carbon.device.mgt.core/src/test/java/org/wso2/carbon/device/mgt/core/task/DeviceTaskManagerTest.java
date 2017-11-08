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
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
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

/**
 * This is a test class to test the functionality in {@link DeviceTaskManager}.
 */
public class DeviceTaskManagerTest extends BaseDeviceManagementTest {

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

    @Test(groups = "Device Task Manager Test Group", description = "Getting the task frequency from the scheduler")
    public void testGetTaskFrequency() throws DeviceMgtTaskException {
        log.info("Attempting to retrieve task frequency.");
        Assert.assertEquals(this.deviceTaskManager.getTaskFrequency(), 60000);
        log.info("Successfully retrieved task frequency.");
    }

    @Test(groups = "Device Task Manager Test Group", description = "Testing if the task is enabled")
    public void testIsTaskEnabled() throws DeviceMgtTaskException {
        log.info("Attempting to retrieve task status.");
        Assert.assertTrue(this.deviceTaskManager.isTaskEnabled());
        log.info("Successfully retrieved task status.");
    }

    @Test(groups = "Device Task Manager Test Group", description = "Testing adding operations to devices.")
    public void testAddOperation() throws DeviceMgtTaskException, OperationManagementException {
        log.info("Attempting to add operations for devices.");
        this.deviceTaskManager.addOperations();
        for (DeviceIdentifier deviceId : deviceIds) {
            List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
            Assert.assertNotNull(operationList);
            Assert.assertEquals(operationList.size(), 3);
        }
        log.info("Successfully added operations for devices.");
    }

    @Test(groups = "Device Task Manager Test Group",
            description = "Testing adding operations when no devices are available")
    public void testAddOperationsWithoutDevices() throws DeviceManagementException, DeviceMgtTaskException {
        this.deviceMgtProviderService.registerDeviceType(
                new TestDeviceManagementService(NEW_DEVICE_TYPE, TestDataHolder.SUPER_TENANT_DOMAIN));
        DeviceTaskManager taskManager = new DeviceTaskManagerImpl(NEW_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 50000, 3));
        taskManager.addOperations();
    }

    @Test(groups = "Device Task Manager Test Group", dependsOnMethods = "testAddOperationsWithoutDevices",
            description = "Testing adding operations when no operations are scheduled")
    public void testAddOperationsWithoutOperations() throws DeviceMgtTaskException {
        DeviceTaskManager taskManager = new DeviceTaskManagerImpl(NEW_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 50000, 3));
        taskManager.addOperations();
    }

    @Test(groups = "Device Task Manager Test Group", description = "Testing device detail retriever task execution")
    public void testDeviceDetailRetrieverTaskExecute() throws OperationManagementException {
        DeviceDetailsRetrieverTask deviceDetailsRetrieverTask = new DeviceDetailsRetrieverTask();
        Map<String, String> map = new HashMap<>();
        map.put("DEVICE_TYPE", TestDataHolder.TEST_DEVICE_TYPE);
        map.put("OPPCONFIG", DEVICE_DETAIL_RETRIEVER_OPPCONFIG);
        deviceDetailsRetrieverTask.setProperties(map);
        deviceDetailsRetrieverTask.execute();
        for (DeviceIdentifier deviceId : deviceIds) {
            List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
            Assert.assertNotNull(operationList);
            Assert.assertEquals(operationList.size(), 4,
                    "Expected number of operations is 4 after adding the device detail retriever operation");
            Assert.assertEquals(operationList.get(0).getCode(), "DEVICE_INFO",
                    "Operation code of the device detail retriever task should be DEVICE_LOCATION");
        }
    }

    @Test(groups = "Device Task Manager Test Group",
            description = "Testing device detail retriever task execution for tenants")
    public void testDeviceDetailRetrieverTaskExecuteForAllTenants() throws OperationManagementException {
        DeviceDetailsRetrieverTask deviceDetailsRetrieverTask = new DeviceDetailsRetrieverTask();
        System.setProperty("is.cloud", "true");
        Map<String, String> map = new HashMap<>();
        map.put("DEVICE_TYPE", TestDataHolder.TEST_DEVICE_TYPE);
        map.put("OPPCONFIG", DEVICE_DETAIL_RETRIEVER_OPPCONFIG);
        deviceDetailsRetrieverTask.setProperties(map);
        deviceDetailsRetrieverTask.execute();
        for (DeviceIdentifier deviceId : deviceIds) {
            List<? extends Operation> operationList = this.operationManager.getOperations(deviceId);
            Assert.assertNotNull(operationList);
            Assert.assertEquals(operationList.size(), 4);
            Assert.assertEquals(operationList.get(0).getCode(), "DEVICE_INFO",
                    "Operation code of the device detail retriever task should be DEVICE_LOCATION");
        }
    }

    @AfterClass
    public void cleanup() throws DeviceManagementException {
        for (DeviceIdentifier deviceId: deviceIds) {
            this.deviceMgtProviderService.disenrollDevice(deviceId);
        }
    }
}
