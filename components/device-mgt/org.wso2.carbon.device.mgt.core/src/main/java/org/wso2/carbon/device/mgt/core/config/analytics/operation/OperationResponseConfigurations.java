/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.analytics.operation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * This class represents the information related to Operation response configuration.
 */
@XmlRootElement(name = "PublishOperationResponse")
public class OperationResponseConfigurations {

    private boolean enabled;
    private List<String> operations;

    public boolean isEnabled() {
        return enabled;
    }

    @XmlElement(name = "Enabled", required = true)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getOperations() {
        return operations;
    }

    @XmlElementWrapper(name = "Operations", required = true)
    @XmlElement(name = "Operation", required = true)
    public void setOperations(List<String> operations) {
        this.operations = operations;
    }
}
