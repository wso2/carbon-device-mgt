/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.device.mgt.core.archival.ArchivalException;
import org.wso2.carbon.device.mgt.core.archival.ArchivalService;
import org.wso2.carbon.device.mgt.core.archival.ArchivalServiceImpl;
import org.wso2.carbon.ntask.core.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ArchivedDataDeletionTask implements Task {

    private static Log log = LogFactory.getLog(ArchivedDataDeletionTask.class);

    private ArchivalService archivalService;

    @Override
    public void setProperties(Map<String, String> map) {

    }

    @Override
    public void init() {
        this.archivalService = new ArchivalServiceImpl();
    }

    @Override
    public void execute() {
        log.info("Executing DataDeletionTask at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        long startTime = System.nanoTime();
        try {
            archivalService.deleteArchivedRecords();
        } catch (ArchivalException e) {
            log.error("An error occurred while executing DataDeletionTask", e);
        }
        long endTime = System.nanoTime();
        long difference = (endTime - startTime) / 1000000 * 1000;
        log.info("DataDeletionTask completed. Total execution time: " + difference + " seconds");
    }
}
