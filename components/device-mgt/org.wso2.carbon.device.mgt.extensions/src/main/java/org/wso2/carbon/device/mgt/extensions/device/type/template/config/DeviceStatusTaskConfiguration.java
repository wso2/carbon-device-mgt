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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DeviceStatusTaskConfig")
public class DeviceStatusTaskConfiguration {

    private boolean enabled;
    private int frequency;
    private int idleTimeToMarkInactive;
    private int idleTimeToMarkUnreachable;

    @XmlElement(name = "RequireStatusMonitoring", required = true)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElement(name = "Frequency", required = true)
    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @XmlElement(name = "IdleTimeToMarkInactive", required = true)
    public int getIdleTimeToMarkInactive() {
        return idleTimeToMarkInactive;
    }

    public void setIdleTimeToMarkInactive(int idleTimeToMarkInactive) {
        this.idleTimeToMarkInactive = idleTimeToMarkInactive;
    }

    @XmlElement(name = "IdleTimeToMarkUnreachable", required = true)
    public int getIdleTimeToMarkUnreachable() {
        return idleTimeToMarkUnreachable;
    }

    public void setIdleTimeToMarkUnreachable(int idleTimeToMarkUnreachable) {
        this.idleTimeToMarkUnreachable = idleTimeToMarkUnreachable;
    }
}