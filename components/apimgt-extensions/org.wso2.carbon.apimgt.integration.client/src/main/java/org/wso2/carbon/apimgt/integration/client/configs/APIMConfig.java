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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.integration.client.configs;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This holds the configuration api manager integration.
 */
@XmlRootElement(name = "APIMConfiguration")
public class APIMConfig {

    String dcrEndpoint;
    String tokenEndpoint;
    String publisherEndpoint;
    String storeEndpoint;
    String username;
    String password;

    @XmlElement(name = "DCREndpoint", required = true)
    public String getDcrEndpoint() {
        return dcrEndpoint;
    }

    public void setDcrEndpoint(String dcrEndpoint) {
        this.dcrEndpoint = dcrEndpoint;
    }

    @XmlElement(name = "TokenEndpoint", required = true)
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    @XmlElement(name = "PublisherEndpoint", required = true)
    public String getPublisherEndpoint() {
        return publisherEndpoint;
    }

    public void setPublisherEndpoint(String publisherEndpoint) {
        this.publisherEndpoint = publisherEndpoint;
    }

    @XmlElement(name = "StoreEndpoint", required = true)
    public String getStoreEndpoint() {
        return storeEndpoint;
    }

    public void setStoreEndpoint(String storeEndpoint) {
        this.storeEndpoint = storeEndpoint;
    }

    @XmlElement(name = "Username", required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlElement(name = "Password", required = true)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
