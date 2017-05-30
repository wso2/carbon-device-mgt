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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskException;
import org.wso2.carbon.device.mgt.core.status.task.DeviceStatusTaskManagerService;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of DeviceStatusTaskManagerService.
 */
public class DeviceStatusTaskManagerServiceImpl implements DeviceStatusTaskManagerService {

    private static Log log = LogFactory.getLog(DeviceStatusTaskManagerServiceImpl.class);

    public static final String DEVICE_STATUS_MONITORING_TASK_TYPE = "DEVICE_STATUS_MONITORING";
    static final String DEVICE_TYPE = "DEVICE_TYPE";
    static final String DEVICE_STATUS_TASK_CONFIG = "DEVICE_STATUS_TASK_CONFIG";
    private static String TASK_CLASS = DeviceStatusMonitoringTask.class.getName();

    @Override
    public void startTask(String deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        log.info("Device Status monitoring Task adding for " + deviceType);

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(DEVICE_STATUS_MONITORING_TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Device Status monitoring task is started for the device type " + deviceType);
                log.debug(
                        "Device Status monitoring task is at frequency of : " + deviceStatusTaskConfig.getFrequency());
            }

            TaskManager taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            //Convert to milli seconds
            triggerInfo.setIntervalMillis(deviceStatusTaskConfig.getFrequency()*1000);
            triggerInfo.setRepeatCount(-1);

            Gson gson = new Gson();
            String deviceStatusTaskConfigs = gson.toJson(deviceStatusTaskConfig);

            Map<String, String> properties = new HashMap<>();

            properties.put(DEVICE_TYPE, deviceType);
            properties.put(DEVICE_STATUS_TASK_CONFIG, deviceStatusTaskConfigs);

            String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType;

            if (!taskManager.isTaskScheduled(deviceType)) {
                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceStatusTaskException(
                        "Device Status monitoring task is already started for this device-type : " + deviceType);
            }
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while creating the Device Status monitoring task " +
                    "for device-type : " + deviceType, e);
        }
    }

    @Override
    public void stopTask(String deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            if (taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
                taskManager.deleteTask(deviceType);
            }
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while deleting the Device Status monitoring task " +
                    "for tenant " + getTenantId(), e);
        }
    }

    @Override
    public void updateTask(String deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        int tenantId = getTenantId();
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
            String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType + "_" + String.valueOf(tenantId);
            if (taskManager.isTaskScheduled(taskName)) {
                taskManager.deleteTask(taskName);
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setIntervalMillis(deviceStatusTaskConfig.getFrequency());
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put(DEVICE_TYPE, deviceType);

                Gson gson = new Gson();
                String deviceStatusTaskConfigs = gson.toJson(deviceStatusTaskConfig);
                properties.put(DEVICE_STATUS_TASK_CONFIG, deviceStatusTaskConfigs);

                TaskInfo taskInfo = new TaskInfo(deviceType, TASK_CLASS, properties, triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceStatusTaskException(
                        "Device details retrieving Device Status monitoring  task has not been started for this tenant " +
                                tenantId + ". Please start the task first.");
            }

        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while updating the Device Status monitoring  task for tenant " + tenantId,
                    e);
        }
    }

    @Override
    public boolean isTaskScheduled(String deviceType) throws DeviceStatusTaskException {
        int tenantId = getTenantId();
        String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType + "_" + String.valueOf(tenantId);
        TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
        TaskManager taskManager;
        try {
            taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
            return taskManager.isTaskScheduled(taskName);
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while checking Device Status monitoring task for tenant " +
                    tenantId, e);
        }
    }

    private int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}