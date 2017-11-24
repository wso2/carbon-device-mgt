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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import org.wso2.carbon.device.mgt.core.archival.dao.ArchivalSourceDAOFactory;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.config.DeviceManagementConfig;
import org.wso2.carbon.device.mgt.core.config.datasource.DataSourceConfig;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
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
 * @scr.reference name="org.wso2.carbon.device.manager"
 * interface="org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDeviceManagementService"
 * unbind="unsetDeviceManagementService"
 */
public class ActivityDataPurgingServiceComponent {
    private static Log log = LogFactory.getLog(ActivityDataPurgingServiceComponent.class);

    protected void activate(ComponentContext componentContext) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Initializing activity data archival task manager bundle.");
            }

            /* Initialising data archival configurations */
            DeviceManagementConfig config =
                    DeviceConfigurationManager.getInstance().getDeviceManagementConfig();

            boolean archivalTaskEnabled = false;
            boolean purgingTaskEnabled = false;

            if (config.getArchivalConfiguration() != null
                && config.getArchivalConfiguration().getArchivalTaskConfiguration() != null){
                archivalTaskEnabled = config.getArchivalConfiguration().getArchivalTaskConfiguration().isEnabled();
                purgingTaskEnabled = config.getArchivalConfiguration().getArchivalTaskConfiguration()
                                              .getPurgingTaskConfiguration() != null
                                      && config.getArchivalConfiguration()
                                              .getArchivalTaskConfiguration().getPurgingTaskConfiguration().isEnabled();
            }

            if (archivalTaskEnabled || purgingTaskEnabled) {
                DataSourceConfig dsConfig = config.getDeviceManagementConfigRepository().getDataSourceConfig();
                ArchivalSourceDAOFactory.init(dsConfig);
                DataSourceConfig purgingDSConfig = config.getArchivalConfiguration().getDataSourceConfig();
                ArchivalDestinationDAOFactory.init(purgingDSConfig);
            }

            ArchivalTaskManager archivalTaskManager = new ArchivalTaskManagerImpl();

            // This will start the data archival task
            if (archivalTaskEnabled) {
                archivalTaskManager.scheduleArchivalTask();
                log.info("Data archival task has been scheduled.");
            } else {
                log.warn("Data archival task has been disabled. It is recommended to enable archival task to " +
                         "prune the transactional databases tables time to time if you are using MySQL.");
            }
            
            // This will start the data deletion task.
            if (purgingTaskEnabled) {
                archivalTaskManager.scheduleDeletionTask();
                log.info("Data purging task has been scheduled for archived data.");
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

    protected void setDeviceManagementService(DeviceManagementProviderService deviceManagementService){

    }

    protected void unsetDeviceManagementService(DeviceManagementProviderService deviceManagementService){

    }

    @SuppressWarnings("unused")
    protected void deactivate(ComponentContext componentContext) {

    }

}
