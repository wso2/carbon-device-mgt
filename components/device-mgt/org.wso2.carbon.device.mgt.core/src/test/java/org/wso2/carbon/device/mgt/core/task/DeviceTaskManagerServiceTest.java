package org.wso2.carbon.device.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opensaml.xml.signature.P;
import org.powermock.api.mockito.PowerMockito;
import org.quartz.impl.StdSchedulerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.MonitoringOperation;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.core.TestTaskServiceImpl;
import org.wso2.carbon.device.mgt.core.TestUtils;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.permission.mgt.PermissionUtils;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskUtils;
import org.wso2.carbon.ntask.core.impl.QuartzCachedThreadPool;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class DeviceTaskManagerServiceTest {
    private static final Log log = LogFactory.getLog(DeviceTaskManagerService.class);
    private static final String TASK_TYPE = "DEVICE_MONITORING";
    private DeviceTaskManagerService deviceTaskManagerService;
    @Mock private TaskService taskService;

    @BeforeClass public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Device Task Manager Service Test Suite");
        this.taskService = new TestTaskServiceImpl();
        DeviceManagementDataHolder.getInstance().setTaskService(this.taskService);
        this.deviceTaskManagerService = new DeviceTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(this.deviceTaskManagerService);
        Field taskServiceField = TasksDSComponent.class.getDeclaredField("taskService");
        taskServiceField.setAccessible(true);
        taskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));
    }

    @Test(groups = "Device Task Manager")
    public void testStartTask() {
        try {
            log.debug("Attempting to start task from testStartTask");
            this.deviceTaskManagerService
                    .startTask(TestDataHolder.TEST_DEVICE_TYPE, generateValidMonitoringTaskConfig("DEVICE_INFO"));
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertNotNull(taskManager
                    .getTask(TestDataHolder.TEST_DEVICE_TYPE + String.valueOf(TestDataHolder.SUPER_TENANT_ID)));
            log.debug("Task Successfully started");
        } catch (DeviceMgtTaskException | TaskException e) {
            Assert.fail("Exception occurred when starting the task", e);
        }
    }

    @Test(groups = "Device Task Manager", dependsOnMethods = "testStartTask")
    public void testUpdateTask() {
        try {
            log.debug("Attempting to update task from testStartTask");
            this.deviceTaskManagerService
                    .updateTask(TestDataHolder.TEST_DEVICE_TYPE, generateValidMonitoringTaskConfig("DEVICE_LOCATION"));
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            Assert.assertEquals(taskManager.getAllTasks().size(), 1);
            log.debug("Task Successfully updated");
        } catch (DeviceMgtTaskException | TaskException e) {
            Assert.fail("Exception occurred when updating the task", e);
        }
    }

    @Test(groups = "Device Task Manager", dependsOnMethods = "testUpdateTask")
    public void testStopTask() {
        log.debug("Attempting to stop task from testStopTask");
        try {
            this.deviceTaskManagerService
                    .stopTask(TestDataHolder.TEST_DEVICE_TYPE, generateValidMonitoringTaskConfig("DEVICE_LOCATION"));
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            Assert.assertEquals(taskManager.getAllTasks().size(), 0);
        } catch (DeviceMgtTaskException | TaskException e) {
            Assert.fail("Exception occurred when stopping the task", e);
        }
    }



    private OperationMonitoringTaskConfig generateValidMonitoringTaskConfig(String operationConfig) {
        OperationMonitoringTaskConfig validTaskConfig = new OperationMonitoringTaskConfig();
        List<MonitoringOperation> operationList = new ArrayList<>();
        MonitoringOperation operation = new MonitoringOperation();
        operation.setTaskName(operationConfig);
        operation.setRecurrentTimes(1);
        operationList.add(operation);

        validTaskConfig.setEnabled(true);
        validTaskConfig.setFrequency(60000);
        validTaskConfig.setMonitoringOperation(operationList);

        return validTaskConfig;
    }

    private Properties getStandardQuartzProps() {
        Properties result = new Properties();
        result.put("org.quartz.scheduler.skipUpdateCheck", "true");
        result.put("org.quartz.threadPool.class", QuartzCachedThreadPool.class.getName());
        return result;
    }
}
