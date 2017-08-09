/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.config.remote.session;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the information related to Remote Session configuration.
 */
@XmlRootElement(name = "RemoteSessionConfiguration")
public class RemoteSessionConfiguration {

    private String remoteSessionServerUrl;
    private boolean isEnabled;

    @XmlElement(name = "RemoteSessionServerUrl", required = true)
    public void setRemoteSessionServerUrl(String remoteSessionServerUrl) {
        this.remoteSessionServerUrl = remoteSessionServerUrl;
    }

    public String getRemoteSessionServerUrl() {
        return remoteSessionServerUrl;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    @XmlElement(name = "isEnabled", required = true)
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

}


