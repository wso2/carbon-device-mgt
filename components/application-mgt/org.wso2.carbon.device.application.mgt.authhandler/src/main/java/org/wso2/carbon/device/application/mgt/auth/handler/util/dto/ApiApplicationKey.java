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

public class ApiApplicationKey {
    private String client_id;
    private String client_secret;

    public String getConsumerKey() {
        return this.client_id;
    }

    public void setClient_id(String consumerKey) {
        this.client_id = consumerKey;
    }

    public String getConsumerSecret() {
        return this.client_secret;
    }

    public void setClient_secret(String consumerSecret) {
        this.client_secret = consumerSecret;
    }
}
