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
import org.wso2.carbon.device.mgt.common.DeviceStatusTaskPluginConfig;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
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

    private static final Log log = LogFactory.getLog(DeviceStatusTaskManagerServiceImpl.class);

    public static final String DEVICE_STATUS_MONITORING_TASK_TYPE = "DEVICE_STATUS_MONITORING";
    static final String DEVICE_TYPE = "DEVICE_TYPE";
    static final String DEVICE_TYPE_ID = "DEVICE_TYPE_ID";
    static final String DEVICE_STATUS_TASK_CONFIG = "DEVICE_STATUS_TASK_CONFIG";
    private static final String TASK_CLASS = DeviceStatusMonitoringTask.class.getName();

    @Override
    public void startTask(DeviceType deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        log.info("Device Status monitoring Task adding for " + deviceType.getName());

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(DEVICE_STATUS_MONITORING_TASK_TYPE);

            if (log.isDebugEnabled()) {
                log.debug("Device Status monitoring task is started for the device type " + deviceType.getName());
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

            properties.put(DEVICE_TYPE, deviceType.getName());
            properties.put(DEVICE_TYPE_ID, deviceType.getId() + "");
            properties.put(DEVICE_STATUS_TASK_CONFIG, deviceStatusTaskConfigs);

            String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType.getName() + "_" + deviceType.getId();

            if (!taskManager.isTaskScheduled(taskName)) {
                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceStatusTaskException(
                        "Device Status monitoring task is already started for this device-type : " + deviceType.getName());
            }
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while creating the Device Status monitoring task " +
                    "for device-type : " + deviceType.getName(), e);
        }
    }

    @Override
    public void stopTask(DeviceType deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType.getName() + "_" + deviceType.getId();
            if (taskService.isServerInit()) {
                TaskManager taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
                taskManager.deleteTask(taskName);
            }
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while deleting the Device Status monitoring task " +
                    "for device-type : " + deviceType.getName(), e);
        }
    }

    @Override
    public void updateTask(DeviceType deviceType, DeviceStatusTaskPluginConfig deviceStatusTaskConfig)
            throws DeviceStatusTaskException {
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
            String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType + "_" + deviceType.getId();
            if (taskManager.isTaskScheduled(taskName)) {
                taskManager.deleteTask(taskName);
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
                triggerInfo.setIntervalMillis(deviceStatusTaskConfig.getFrequency());
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put(DEVICE_TYPE, deviceType.getName());
                properties.put(DEVICE_TYPE_ID, deviceType.getId() + "");

                Gson gson = new Gson();
                String deviceStatusTaskConfigs = gson.toJson(deviceStatusTaskConfig);
                properties.put(DEVICE_STATUS_TASK_CONFIG, deviceStatusTaskConfigs);

                TaskInfo taskInfo = new TaskInfo(taskName, TASK_CLASS, properties, triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new DeviceStatusTaskException(
                        "Device details retrieving Device Status monitoring task has not been started for this device-type " +
                                deviceType.getName() + ". Please start the task first.");
            }

        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while updating the Device Status monitoring  " +
                    "task for device-type : " + deviceType.getName(), e);
        }
    }

    @Override
    public boolean isTaskScheduled(DeviceType deviceType) throws DeviceStatusTaskException {
        String taskName = DEVICE_STATUS_MONITORING_TASK_TYPE + "_" + deviceType.getName() + "_" + deviceType.getId();
        TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
        TaskManager taskManager;
        try {
            taskManager = taskService.getTaskManager(DEVICE_STATUS_MONITORING_TASK_TYPE);
            return taskManager.isTaskScheduled(taskName);
        } catch (TaskException e) {
            throw new DeviceStatusTaskException("Error occurred while checking Device Status monitoring task for device-type : " +
                    deviceType.getName(), e);
        }
    }
}