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
package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.task.ArchivalTaskManager;
import org.wso2.carbon.device.mgt.core.task.impl.ArchivalTaskManagerImpl;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="org.wso2.carbon.activity.data.archival" immediate="true"
 * @scr.reference name="device.ntask.component"
 * interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setTaskService"
 * unbind="unsetTaskService"
 */
public class ActivityDataPurgingServiceComponent {
    private static Log log = LogFactory.getLog(ActivityDataPurgingServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing activity data archival task manager bundle.");
            }
            ArchivalTaskManager archivalTaskManager = new ArchivalTaskManagerImpl();

            // This will start the data archival task
            boolean purgingTaskEnabled =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                            .getArchivalTaskConfiguration().isEnabled();

            if (purgingTaskEnabled) {
                archivalTaskManager.scheduleArchivalTask();
            } else {
                log.warn("Data archival task has been disabled. It is recommended to enable archival task to prune the " +
                        "transactional databases tables time to time.");
            }
            // This will start the data deletion task.
            boolean deletionTaskEnabled =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                            .getArchivalTaskConfiguration().getPurgingTaskConfiguration().isEnabled();
            if (deletionTaskEnabled) {
                archivalTaskManager.scheduleDeletionTask();
            }
        } catch (Throwable e) {
            log.error("Error occurred while initializing activity data archival task manager service.", e);
        }
    }

    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Removing the task service.");
        }
        DeviceManagementDataHolder.getInstance().setTaskService(null);
    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {

    }

}
