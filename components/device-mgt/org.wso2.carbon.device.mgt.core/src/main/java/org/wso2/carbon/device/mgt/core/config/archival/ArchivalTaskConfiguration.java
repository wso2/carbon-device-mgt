/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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

package org.wso2.carbon.device.mgt.core.config.archival;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ArchivalTask")
public class ArchivalTaskConfiguration {
    private boolean enabled;
    private String cronExpression;
    private String taskClazz;
    private int retentionPeriod;
    private int batchSize;
    private PurgingTaskConfiguration purgingTaskConfiguration;

    @XmlElement(name = "Enabled", required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElement(name = "CronExpression", required = true)
    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    @XmlElement(name = "TaskClass", required = true)
    public String getTaskClazz() {
        return taskClazz;
    }

    public void setTaskClazz(String taskClazz) {
        this.taskClazz = taskClazz;
    }

    @XmlElement(name = "RetentionPeriod", required = true)
    public int getRetentionPeriod() {
        return retentionPeriod;
    }

    public void setRetentionPeriod(int retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
    }

    @XmlElement(name = "PurgingTask", required = true)
    public PurgingTaskConfiguration getPurgingTaskConfiguration() {
        return purgingTaskConfiguration;
    }

    public void setPurgingTaskConfiguration(PurgingTaskConfiguration purgingTaskConfiguration) {
        this.purgingTaskConfiguration = purgingTaskConfiguration;
    }

    @XmlElement(name ="ExecutionBatchSize", required = true)
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
