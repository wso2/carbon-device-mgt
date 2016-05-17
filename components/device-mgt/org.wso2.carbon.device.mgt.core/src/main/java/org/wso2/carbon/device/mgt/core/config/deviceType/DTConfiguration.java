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


/**
 * This class will read the configurations related to task. This task will be responsible for adding the operations.
 */
package org.wso2.carbon.device.mgt.core.config.deviceType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DTDepyloymentConfiguration")
public class DTConfiguration {

    private String dtHostAddress;
    private String dtHostPort;

    @XmlElement(name = "DTHostAddress", required = true)
    public String getDtHostAddress() {
        return dtHostAddress;
    }

    public void setDtHostAddress(String dtHostAddress) {
        this.dtHostAddress = dtHostAddress;
    }

    @XmlElement(name = "DTHostPort", required = true)
    public String getDtHostPort() {
        return dtHostPort;
    }

    public void setDtHostPort(String dtHostPort) {
        this.dtHostPort = dtHostPort;
    }


}
