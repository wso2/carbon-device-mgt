/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.config.email;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "EmailClientConfiguration")
public class EmailConfigurations {

    private int minNumOfThread;
    private int maxNumOfThread;
    private int keepAliveTime;
    private int threadQueueCapacity;
    private String lBHostPortPrefix;
    private String enrollmentContextPath;

    @XmlElement(name = "minimumThread", required = true)
    public int getMinNumOfThread() {
        return minNumOfThread;
    }

    public void setMinNumOfThread(int minNumOfThread) {
        this.minNumOfThread = minNumOfThread;
    }
    @XmlElement(name = "maximumThread", required = true)
    public int getMaxNumOfThread() {
        return maxNumOfThread;
    }

    public void setMaxNumOfThread(int maxNumOfThread) {
        this.maxNumOfThread = maxNumOfThread;
    }

    @XmlElement(name = "keepAliveTime", required = true)
    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }
    @XmlElement(name = "ThreadQueueCapacity", required = true)
    public int getThreadQueueCapacity() {
        return threadQueueCapacity;
    }

    public void setThreadQueueCapacity(int threadQueueCapacity) {
        this.threadQueueCapacity = threadQueueCapacity;
    }

    @XmlElement(name = "LBHostPortPrefix", required = true)
    public String getlBHostPortPrefix() {
        return lBHostPortPrefix;
    }

    public void setlBHostPortPrefix(String lBHostPortPrefix) {
        this.lBHostPortPrefix = lBHostPortPrefix;
    }

    @XmlElement(name = "enrollmentContextPath", required = true)
    public String getEnrollmentContextPath() {
        return enrollmentContextPath;
    }

    public void setEnrollmentContextPath(String enrollmentContextPath) {
        this.enrollmentContextPath = enrollmentContextPath;
    }
}
