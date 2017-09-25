/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.task.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.task.ArchivalTaskException;
import org.wso2.carbon.device.mgt.core.task.ArchivalTaskManager;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.HashMap;
import java.util.Map;

public class ArchivalTaskManagerImpl implements ArchivalTaskManager {
    private static final String TASK_TYPE_ARCHIVAL = "DATA_ARCHIVAL";
    private static final String TASK_TYPE_DELETION = "DATA_DELETION";

    private static final String TASK_NAME_ARCHIVAL = "DATA_ARCHIVAL_TASK";
    private static final String TASK_NAME_DELETION = "DATA_DELETION_TASK";

    private static final String TENANT_ID = "TENANT_ID";

    private static Log log = LogFactory.getLog(ArchivalTaskManagerImpl.class);

    public void scheduleArchivalTask() throws ArchivalTaskException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(TASK_TYPE_ARCHIVAL);

            if (log.isDebugEnabled()) {
                log.debug("Data archival task is started for the tenant id " + tenantId);
            }
            String taskClazz = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getArchivalConfiguration().getArchivalTaskConfiguration().getTaskClazz();
            String cronExpression = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getArchivalConfiguration().getArchivalTaskConfiguration().getCronExpression();

            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE_ARCHIVAL);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setCronExpression(cronExpression);
//            triggerInfo.setIntervalMillis(60000);
            triggerInfo.setRepeatCount(-1);
//            triggerInfo.setRepeatCount(0);
            triggerInfo.setDisallowConcurrentExecution(true);

            Map<String, String> properties = new HashMap<>();
            properties.put(TENANT_ID, String.valueOf(tenantId));

            if (!taskManager.isTaskScheduled(TASK_NAME_ARCHIVAL)) {
                TaskInfo taskInfo = new TaskInfo(TASK_NAME_ARCHIVAL, taskClazz, properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new ArchivalTaskException("Data archival task is already started for this tenant " +
                        tenantId);
            }

        } catch (TaskException e) {
            throw new ArchivalTaskException("Error occurred while creating the task for tenant " + tenantId, e);
        }
    }

    public void scheduleDeletionTask() throws ArchivalTaskException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            TaskService taskService = DeviceManagementDataHolder.getInstance().getTaskService();
            taskService.registerTaskType(TASK_TYPE_DELETION);

            String taskClazz = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getArchivalConfiguration().getArchivalTaskConfiguration()
                    .getPurgingTaskConfiguration().getTaskClazz();
            String cronExpression = DeviceConfigurationManager.getInstance().getDeviceManagementConfig()
                    .getArchivalConfiguration().getArchivalTaskConfiguration()
                    .getPurgingTaskConfiguration().getCronExpression();

            if (log.isDebugEnabled()) {
                log.debug("Data deletion task is started for the tenant id " + tenantId);
            }
            TaskManager taskManager = taskService.getTaskManager(TASK_TYPE_DELETION);

            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            triggerInfo.setCronExpression(cronExpression);
            triggerInfo.setRepeatCount(-1);
            triggerInfo.setDisallowConcurrentExecution(true);

            Map<String, String> properties = new HashMap<>();
            properties.put(TENANT_ID, String.valueOf(tenantId));

            if (!taskManager.isTaskScheduled(TASK_NAME_DELETION)) {
                TaskInfo taskInfo = new TaskInfo(TASK_NAME_DELETION, taskClazz, properties, triggerInfo);
                taskManager.registerTask(taskInfo);
                taskManager.rescheduleTask(taskInfo.getName());
            } else {
                throw new ArchivalTaskException("Data deletion task is already started for this tenant " +
                        tenantId);
            }
        } catch (TaskException e) {
            throw new ArchivalTaskException("Error occurred while creating the task for tenant " + tenantId, e);
        }

    }


}
