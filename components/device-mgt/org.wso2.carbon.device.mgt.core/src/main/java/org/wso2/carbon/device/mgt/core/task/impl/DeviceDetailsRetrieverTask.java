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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.task.DeviceMgtTaskException;
import org.wso2.carbon.device.mgt.core.task.DeviceTaskManager;
import org.wso2.carbon.ntask.core.Task;

import java.util.Map;

public class DeviceDetailsRetrieverTask implements Task {

    private static Log log = LogFactory.getLog(DeviceDetailsRetrieverTask.class);
//    private DeviceTaskManager deviceTaskManager = new DeviceTaskManagerImpl();

    @Override
    public void setProperties(Map<String, String> map) {

    }

    @Override
    public void init() {

    }

    @Override
    public void execute() {

        if (log.isDebugEnabled()) {
            log.debug("Device details retrieving task started to run.");
        }

        DeviceTaskManager deviceTaskManager = new DeviceTaskManagerImpl();

        try {
            deviceTaskManager.addOperations();
        } catch (DeviceMgtTaskException e) {
            log.error("Error occurred while trying to add the operations to device to retrieve device details.", e);
        }

    }
}

