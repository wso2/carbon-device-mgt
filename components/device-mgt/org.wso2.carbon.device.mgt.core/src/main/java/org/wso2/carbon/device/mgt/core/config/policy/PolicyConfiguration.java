/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.policy;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "PolicyConfiguration")
public class PolicyConfiguration {

    private String monitoringClass;
    private boolean monitoringEnable;
    private int monitoringFrequency;
    private int maxRetries;
    private int minRetriesToMarkUnreachable;
    private int minRetriesToMarkInactive;
    private List<String> platforms;
    private String policyEvaluationPoint;

    @XmlElement(name = "MonitoringClass", required = true)
    public String getMonitoringClass() {
        return monitoringClass;
    }

    public void setMonitoringClass(String monitoringClass) {
        this.monitoringClass = monitoringClass;
    }

    @XmlElement(name = "MaxRetries", required = true)
    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @XmlElement(name = "MinRetriesToMarkUnreachable", required = true)
    public int getMinRetriesToMarkUnreachable() {
        return minRetriesToMarkUnreachable;
    }

    public void setMinRetriesToMarkUnreachable(int minRetriesToMarkUnreachable) {
        this.minRetriesToMarkUnreachable = minRetriesToMarkUnreachable;
    }

    @XmlElement(name = "MonitoringEnable", required = true)
    public boolean getMonitoringEnable() {
        return monitoringEnable;
    }

    public void setMonitoringEnable(boolean monitoringEnable) {
        this.monitoringEnable = monitoringEnable;
    }

    @XmlElement(name = "MinRetriesToMarkInactive", required = true)
    public int getMinRetriesToMarkInactive() {
        return minRetriesToMarkInactive;
    }

    public void setMinRetriesToMarkInactive(int minRetriesToMarkInactive) {
        this.minRetriesToMarkInactive = minRetriesToMarkInactive;
    }

    @XmlElement(name = "MonitoringFrequency", required = true)
    public int getMonitoringFrequency() {
        return monitoringFrequency;
    }

    public void setMonitoringFrequency(int monitoringFrequency) {
        this.monitoringFrequency = monitoringFrequency;
    }

    @XmlElementWrapper(name = "Platforms", required = true)
    @XmlElement(name = "Platform", required = true)
    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    @XmlElement(name = "PolicyEvaluationPoint", required = true)
    public String getPolicyEvaluationPointName() {
        return policyEvaluationPoint;
    }

    public void setPolicyEvaluationPointName(String policyEvaluationPointName) {
        this.policyEvaluationPoint = policyEvaluationPointName;
    }

}
