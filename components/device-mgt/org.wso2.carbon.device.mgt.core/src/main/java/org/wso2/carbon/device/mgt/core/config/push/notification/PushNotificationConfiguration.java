/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
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
*/
package org.wso2.carbon.device.mgt.core.config.push.notification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class is for Push notification related Configurations
 */
@XmlRootElement(name = "PushNotificationConfiguration")
public class PushNotificationConfiguration {

    private int schedulerBatchSize;
    private int schedulerBatchDelayMills;
    private int schedulerTaskInitialDelay;
    private boolean schedulerTaskEnabled;
    private List<String> pushNotificationProviders;

    @XmlElement(name = "SchedulerBatchSize", required = true)
    public int getSchedulerBatchSize() {
        return schedulerBatchSize;
    }

    public void setSchedulerBatchSize(int schedulerBatchSize) {
        this.schedulerBatchSize = schedulerBatchSize;
    }

    @XmlElement(name = "SchedulerBatchDelayMills", required = true)
    public int getSchedulerBatchDelayMills() {
        return schedulerBatchDelayMills;
    }

    public void setSchedulerBatchDelayMills(int schedulerBatchDelayMills) {
        this.schedulerBatchDelayMills = schedulerBatchDelayMills;
    }

    @XmlElement(name = "SchedulerTaskInitialDelay", required = true)
    public int getSchedulerTaskInitialDelay() {
        return schedulerTaskInitialDelay;
    }

    public void setSchedulerTaskInitialDelay(int schedulerTaskInitialDelay) {
        this.schedulerTaskInitialDelay = schedulerTaskInitialDelay;
    }

    @XmlElement(name = "SchedulerTaskEnabled", required = true)
    public boolean isSchedulerTaskEnabled() {
        return schedulerTaskEnabled;
    }

    public void setSchedulerTaskEnabled(boolean schedulerTaskEnabled) {
        this.schedulerTaskEnabled = schedulerTaskEnabled;
    }

    @XmlElementWrapper(name = "PushNotificationProviders", required = true)
    @XmlElement(name = "Provider", required = true)
    public List<String> getPushNotificationProviders() {
        return pushNotificationProviders;
    }

    public void setPushNotificationProviders(List<String> pushNotificationProviders) {
        this.pushNotificationProviders = pushNotificationProviders;
    }
}
