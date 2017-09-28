/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.device.mgt.core.operation;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.DeviceManagementException;
import org.wso2.carbon.device.mgt.common.InvalidDeviceException;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManager;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.TestTaskServiceImpl;
import org.wso2.carbon.device.mgt.core.common.BaseDeviceManagementTest;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.operation.mgt.CommandOperation;
import org.wso2.carbon.device.mgt.core.operation.mgt.OperationManagerImpl;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.device.mgt.core.operation.OperationManagementTests.getOperation;


public class ScheduledTaskOperationTests extends BaseDeviceManagementTest {
    private static final String DEVICE_TYPE = "OP_SCHEDULE_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "OP-SCHEDULED_TEST-DEVICE-ID-";
    private static final String COMMAND_OPERATON_CODE = "COMMAND-TEST";
    private static final int NO_OF_DEVICES = 5;
    private static final String DS_TASK_COMPONENT_FIELD = "taskService";
    private static final String CDM_CONFIG_LOCATION = "src" + File.separator + "test" + File.separator + "resources" +
            File.separator + "config" + File.separator + "operation" + File.separator + "cdm-config.xml";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private OperationManager operationMgtService;

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
        initTaskService();
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, COMMAND_OPERATON_CODE));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        DeviceConfigurationManager.getInstance().initConfig(CDM_CONFIG_LOCATION);
        TestNotificationStrategy notificationStrategy = new TestNotificationStrategy();
        this.operationMgtService = new OperationManagerImpl(DEVICE_TYPE, notificationStrategy);
    }


    private void initTaskService() throws NoSuchFieldException, IllegalAccessException {
        TaskService taskService = new TestTaskServiceImpl();
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        DeviceTaskManagerService deviceTaskManager = new DeviceTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(deviceTaskManager);
        Field taskServiceField = TasksDSComponent.class.getDeclaredField(DS_TASK_COMPONENT_FIELD);
        taskServiceField.setAccessible(true);
        taskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));

    }

    @Test
    public void addCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException, NoSuchFieldException {
        Activity activity = this.operationMgtService.addOperation(getOperation(new CommandOperation(), Operation.Type.COMMAND, COMMAND_OPERATON_CODE),
                this.deviceIds);
        Assert.assertEquals(activity.getActivityStatus(), null);
        Assert.assertEquals(activity.getType(), Activity.Type.COMMAND);
    }



}
