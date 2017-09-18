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

package org.wso2.carbon.device.application.mgt.auth.handler.util.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This holds api application consumer key and secret.
 */
@XmlRootElement
public class ApiApplicationKey {
    @XmlElement
    private String clientId;
    @XmlElement
    private String clientSecret;

    public String getConsumerKey() {
        return this.clientId;
    }

    public void setClientId(String consumerKey) {
        this.clientId = consumerKey;
    }

    public String getConsumerSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(String consumerSecret) {
        this.clientSecret = consumerSecret;
    }
}
