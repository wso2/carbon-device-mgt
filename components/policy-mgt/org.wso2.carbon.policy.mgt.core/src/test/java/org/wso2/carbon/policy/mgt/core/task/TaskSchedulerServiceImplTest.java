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

package org.wso2.carbon.policy.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.reflect.Field;

public class TaskSchedulerServiceImplTest {
    private static final Log log = LogFactory.getLog(TaskSchedulerServiceImplTest.class);
    private static final String TEST_DEVICE_TYPE = "TEST-DEVICE-TYPE";
    private TaskScheduleService policyTaskSchedulerService;
    private TaskService taskService;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Device Task Manager Service Test Suite");
        this.taskService = new TestTaskServiceImpl();
        PolicyManagementDataHolder.getInstance().setTaskService(this.taskService);
        Field taskServiceField = TasksDSComponent.class.getDeclaredField("taskService");
        taskServiceField.setAccessible(true);
        taskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));
        PolicyConfiguration policyConfiguration = new PolicyConfiguration();
        policyConfiguration.setMonitoringEnable(true);
        DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                .setPolicyConfiguration(policyConfiguration);
        this.policyTaskSchedulerService = new TaskScheduleServiceImpl();
    }

    @Test(groups = "Policy Task Schedule Service Test Group")
    public void testStartTask() {
        try {
            log.debug("Attempting to start task from testStartTask");
            this.policyTaskSchedulerService.startTask(60000);
            TaskManager taskManager = this.taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertNotNull(taskManager.getTask(PolicyManagementConstants.MONITORING_TASK_NAME + "_" + String
                    .valueOf(MultitenantConstants.SUPER_TENANT_ID)));
            log.debug("Task Successfully started");
        } catch (PolicyMonitoringTaskException | TaskException e) {
            Assert.fail("Exception occurred when starting the task", e);
        }
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testStartTask")
    public void testIsTaskScheduled() {
        try {
            Assert.assertTrue(this.policyTaskSchedulerService.isTaskScheduled());
        } catch (PolicyMonitoringTaskException e) {
            Assert.fail("Exception occurred when trying to check if task is scheduled.");
        }
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testStartTask")
    public void testUpdateTask() {
        try {
            log.debug("Attempting to update task from testStartTask");
            TaskManager taskManager = this.taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
            this.policyTaskSchedulerService.updateTask(30000);
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertEquals(taskManager.getAllTasks().size(), 1);
            log.debug("Task Successfully updated");
        } catch (PolicyMonitoringTaskException | TaskException e) {
            Assert.fail("Exception occurred when updating the task", e);
        }
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateTask")
    public void testStopTask() {
        log.debug("Attempting to stop task from testStopTask");
        try {
            this.policyTaskSchedulerService.stopTask();
            TaskManager taskManager = this.taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
            Assert.assertEquals(taskManager.getAllTasks().size(), 0);
        } catch (PolicyMonitoringTaskException | TaskException e) {
            Assert.fail("Exception occurred when stopping the task", e);
        }
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testStopTask", expectedExceptions = {
            PolicyMonitoringTaskException.class })
    public void testUpdateUnscheduledTask() throws PolicyMonitoringTaskException {
        log.debug("Attempting to update unscheduled task");
        this.policyTaskSchedulerService.updateTask(50000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testStartTaskWhenUnableToRetrieveTaskManager() throws PolicyMonitoringTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to get TaskManager", TaskException.Code.UNKNOWN)).when(taskService)
                .getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.startTask(10000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testUpdateTaskWhenUnableToRetrieveTaskManager() throws PolicyMonitoringTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to get TaskManager", TaskException.Code.UNKNOWN)).when(taskService)
                .getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.updateTask(20000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testStartTaskWhenFailedToRegisterTaskType() throws PolicyMonitoringTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to register task type", TaskException.Code.UNKNOWN)).when(taskService)
                .registerTaskType(PolicyManagementConstants.MONITORING_TASK_TYPE);
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.startTask(20000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testStartTaskWhenFailedToRegisterTask() throws PolicyMonitoringTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to register task", TaskException.Code.UNKNOWN)).when(taskManager)
                .registerTask(Mockito.any(TaskInfo.class));
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.startTask(30000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testUpdateTaskWhenFailedToRegisterTask() throws PolicyMonitoringTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to register task", TaskException.Code.UNKNOWN)).when(taskManager)
                .registerTask(Mockito.any(TaskInfo.class));
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.updateTask(18000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testUpdateTaskWhenFailedToRescheduleTask() throws PolicyMonitoringTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to reschedule task", TaskException.Code.UNKNOWN)).when(taskManager)
                .rescheduleTask(Mockito.any(String.class));
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.updateTask(40000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testUpdateTaskWhenFailedToDeleteTask() throws PolicyMonitoringTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to delete task", TaskException.Code.UNKNOWN)).when(taskManager)
                .deleteTask(Mockito.any(String.class));
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.updateTask(12000);
    }

    @Test(groups = "Policy Task Schedule Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {PolicyMonitoringTaskException.class })
    public void testStopTaskWhenFailedToDeleteTask() throws PolicyMonitoringTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to delete task", TaskException.Code.UNKNOWN)).when(taskManager)
                .deleteTask(Mockito.any(String.class));
        PolicyManagementDataHolder.getInstance().setTaskService(taskService);
        this.policyTaskSchedulerService.stopTask();
    }
}
