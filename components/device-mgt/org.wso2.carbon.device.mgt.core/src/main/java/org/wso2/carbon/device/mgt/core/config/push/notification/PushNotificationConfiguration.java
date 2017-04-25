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

    private int SchedulerBatchSize;
    private int SchedulerBatchDelayMills;
    private boolean SchedulerTaskEnabled;
    private List<String> pushNotificationProviders;

    @XmlElement(name = "SchedulerBatchSize", required = true)
    public int getSchedulerBatchSize() {
        return SchedulerBatchSize;
    }

    public void setSchedulerBatchSize(int SchedulerBatchSize) {
        this.SchedulerBatchSize = SchedulerBatchSize;
    }

    @XmlElement(name = "SchedulerBatchDelayMills", required = true)
    public int getSchedulerBatchDelayMills() {
        return SchedulerBatchDelayMills;
    }

    public void setSchedulerBatchDelayMills(int SchedulerBatchDelayMills) {
        this.SchedulerBatchDelayMills = SchedulerBatchDelayMills;
    }
    @XmlElement(name = "SchedulerTaskEnabled", required = true)
    public boolean isSchedulerTaskEnabled() {
        return SchedulerTaskEnabled;
    }

    public void setSchedulerTaskEnabled(boolean schedulerTaskEnabled) {
        SchedulerTaskEnabled = schedulerTaskEnabled;
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
