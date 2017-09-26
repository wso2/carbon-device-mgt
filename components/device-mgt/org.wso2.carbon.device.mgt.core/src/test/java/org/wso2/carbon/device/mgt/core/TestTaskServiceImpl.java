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
package org.wso2.carbon.device.mgt.core;

import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

import java.util.List;
import java.util.Set;

public class TestTaskServiceImpl implements TaskService {
    @Override
    public TaskManager getTaskManager(String s) throws TaskException {
        return new TaskManager() {
            @Override
            public void initStartupTasks() throws TaskException {

            }

            @Override
            public void scheduleTask(String s) throws TaskException {

            }

            @Override
            public void rescheduleTask(String s) throws TaskException {

            }

            @Override
            public boolean deleteTask(String s) throws TaskException {
                return false;
            }

            @Override
            public void pauseTask(String s) throws TaskException {

            }

            @Override
            public void resumeTask(String s) throws TaskException {

            }

            @Override
            public void registerTask(TaskInfo taskInfo) throws TaskException {

            }

            @Override
            public TaskState getTaskState(String s) throws TaskException {
                return null;
            }

            @Override
            public TaskInfo getTask(String s) throws TaskException {
                return null;
            }

            @Override
            public List<TaskInfo> getAllTasks() throws TaskException {
                return null;
            }

            @Override
            public boolean isTaskScheduled(String s) throws TaskException {
                return false;
            }
        };
    }

    @Override
    public List<TaskManager> getAllTenantTaskManagersForType(String s) throws TaskException {
        return null;
    }

    @Override
    public void registerTaskType(String s) throws TaskException {

    }

    @Override
    public Set<String> getRegisteredTaskTypes() {
        return null;
    }

    @Override
    public void serverInitialized() {

    }

    @Override
    public boolean isServerInit() {
        return false;
    }

    @Override
    public TaskServiceConfiguration getServerConfiguration() {
        return null;
    }

    @Override
    public void runAfterRegistrationActions() throws TaskException {

    }
}
