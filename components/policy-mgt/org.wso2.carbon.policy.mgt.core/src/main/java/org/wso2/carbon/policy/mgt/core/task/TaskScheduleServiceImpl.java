/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.policy.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.policy.PolicyConfiguration;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.policy.mgt.common.PolicyMonitoringTaskException;
import org.wso2.carbon.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.wso2.carbon.policy.mgt.core.util.PolicyManagementConstants;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;

import java.util.HashMap;
import java.util.Map;

public class TaskScheduleServiceImpl implements TaskScheduleService {

    private static Log log = LogFactory.getLog(TaskScheduleServiceImpl.class);
    private PolicyConfiguration policyConfiguration;


    public TaskScheduleServiceImpl() {
        this.policyConfiguration = DeviceConfigurationManager.getInstance().getDeviceManagementConfig().
                getDeviceManagementConfigRepository().getPolicyConfiguration();
    }

    @Override
    public void startTask(int monitoringFrequency) throws PolicyMonitoringTaskException {
        int tenantId = getTenantId();
        if (policyConfiguration.getMonitoringEnable()) {

            if (monitoringFrequency <= 0) {
                throw new PolicyMonitoringTaskException("Time interval cannot be 0 or less than 0.");
            }
            try {
                TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();
                taskService.registerTaskType(PolicyManagementConstants.MONITORING_TASK_TYPE);

                if (log.isDebugEnabled()) {
                    log.debug("Monitoring task is started for the tenant id " + tenantId);
                }

                TaskManager taskManager = taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);

                TriggerInfo triggerInfo = new TriggerInfo();
                triggerInfo.setIntervalMillis(monitoringFrequency);
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put(PolicyManagementConstants.TENANT_ID, String.valueOf(tenantId));

                String taskName = PolicyManagementConstants.MONITORING_TASK_NAME + "_" + String.valueOf(tenantId);

                if (!taskManager.isTaskScheduled(taskName)) {

                    TaskInfo taskInfo = new TaskInfo(taskName, PolicyManagementConstants.MONITORING_TASK_CLAZZ,
                            properties, triggerInfo);

                    taskManager.registerTask(taskInfo);
                    taskManager.rescheduleTask(taskInfo.getName());
                } else {
                    throw new PolicyMonitoringTaskException("Monitoring task is already started for this tenant " +
                            tenantId);
                }


            } catch (TaskException e) {
                throw new PolicyMonitoringTaskException("Error occurred while creating the task for tenant " +
                                                        tenantId, e);
            }
        } else {
            throw new PolicyMonitoringTaskException("Policy monitoring is not enabled in the cdm-config.xml.");
        }

    }

    @Override
    public void stopTask() throws PolicyMonitoringTaskException {
        int tenantId = getTenantId();
        try {
            String taskName = PolicyManagementConstants.MONITORING_TASK_NAME + "_" + String.valueOf(tenantId);
            TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
            taskManager.deleteTask(taskName);
        } catch (TaskException e) {
            throw new PolicyMonitoringTaskException("Error occurred while deleting the task for tenant " +
                                                    tenantId, e);
        }
    }

    @Override
    public void updateTask(int monitoringFrequency) throws PolicyMonitoringTaskException {
        int tenantId = getTenantId();
        try {
            String taskName = PolicyManagementConstants.MONITORING_TASK_NAME + "_" + String.valueOf(tenantId);
            TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();
            TaskManager taskManager = taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);

            if (taskManager.isTaskScheduled(taskName)) {

                taskManager.deleteTask(taskName);
                TriggerInfo triggerInfo = new TriggerInfo();
                triggerInfo.setIntervalMillis(monitoringFrequency);
                triggerInfo.setRepeatCount(-1);

                Map<String, String> properties = new HashMap<>();
                properties.put("tenantId", String.valueOf(tenantId));

                TaskInfo taskInfo = new TaskInfo(taskName, PolicyManagementConstants.MONITORING_TASK_CLAZZ, properties,
                        triggerInfo);

                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new PolicyMonitoringTaskException("Monitoring task has not been started for this tenant " +
                        tenantId + ". Please start the task first.");
            }

        } catch (TaskException e) {
            throw new PolicyMonitoringTaskException("Error occurred while updating the task for tenant " + tenantId, e);
        }

    }

    @Override
    public boolean isTaskScheduled() throws PolicyMonitoringTaskException {
        int tenantId = getTenantId();
        String taskName = PolicyManagementConstants.MONITORING_TASK_NAME + "_" + String.valueOf(tenantId);
        TaskService taskService = PolicyManagementDataHolder.getInstance().getTaskService();
        TaskManager taskManager;
        try {
            taskManager = taskService.getTaskManager(PolicyManagementConstants.MONITORING_TASK_TYPE);
            return taskManager.isTaskScheduled(taskName);
        } catch (TaskException e) {
            throw new PolicyMonitoringTaskException("Error occurred while checking task for tenant " +
                                                    tenantId, e);
        }
    }

    private int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}
