/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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


package org.wso2.carbon.device.mgt.core.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.OperationMonitoringTaskConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManagerService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class DeviceTaskManagerServiceImpl implements DeviceTaskManagerService {

    public static final String TASK_TYPE = "DEVICE_MONITORING";
    public static final String TENANT_ID = "TENANT_ID";
    private static String TASK_CLASS = "org.wso2.carbon.device.mgt.core.task.impl.DeviceDetailsRetrieverTask";


//    private DeviceTaskManager deviceTaskManager;

    private static Log log = LogFactory.getLog(DeviceTaskManagerServiceImpl.class);

    @Override
    public void startTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException {

        log.info("Task adding for " + deviceType);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Device details retrieving task is started for the tenant id " + tenantId);
                //                log.debug("Device details retrieving task is at frequency of : " + deviceTaskManager
                //                        .getTaskFrequency());
                log.debug(
                        "Device details retrieving task is at frequency of : " + operationMonitoringTaskConfig
                                .getFrequency());
            }

            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            //            triggerInfo.setIntervalMillis(deviceTaskManager.getTaskFrequency());
            triggerInfo.setIntervalMillis(operationMonitoringTaskConfig.getFrequency());
            triggerInfo.setRepeatCount(-1);

            Gson gson = new Gson();
            String operationConfigs = gson.toJson(operationMonitoringTaskConfig);

            Map<String, String> properties = new HashMap<>();

            properties.put(TENANT_ID, String.valueOf(tenantId));
            properties.put("DEVICE_TYPE", deviceType);
            properties.put("OPPCONFIG", operationConfigs);

            String taskName = deviceType + String.valueOf(tenantId);

            if (!taskManager.isTaskScheduled(deviceType)) {

                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceMgtTaskException(
                        "Device details retrieving task is already started for this tenant " + tenantId);
            }

        } catch (TaskException e) {
            throw new DeviceMgtTaskException("Error occurred while creating the task for tenant " + tenantId,
                    e);
        }

    }

    @Override
    public void stopTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException {

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            if (taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);
                taskManager.deleteTask(deviceType);
            }
        } catch (TaskException e) {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            throw new DeviceMgtTaskException("Error occurred while deleting the task for tenant " + tenantId,
                    e);
        }

    }

    @Override
    public void updateTask(String deviceType, OperationMonitoringTaskConfig operationMonitoringTaskConfig)
            throws DeviceMgtTaskException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        //        deviceTaskManager = new DeviceTaskManagerImpl();
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE);

            if (taskManager.isTaskScheduled(deviceType)) {

                taskManager.deleteTask(deviceType);
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setIntervalMillis(operationMonitoringTaskConfig.getFrequency());
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put(TENANT_ID, String.valueOf(tenantId));

                TaskInfo taskInfo = new TaskInfo(deviceType, TASK_CLASS, properties, triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceMgtTaskException(
                        "Device details retrieving task has not been started for this tenant " +
                                tenantId + ". Please start the task first.");
            }

        } catch (TaskException e) {
            throw new DeviceMgtTaskException("Error occurred while updating the task for tenant " + tenantId,
                    e);
        }
    }
}

