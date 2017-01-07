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


package org.wso2.carbon.apimgt.handlers.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.List;

/**
 * This class initialize the iot-api-config.xml and hold the values, in order to be read from the relevant classes. This
 * get initialized at the start of the server when apis get loaded.
 */
@XmlRootElement(name = "ServerConfiguration")
public class IOTServerConfiguration {

    private  String hostname;
    private  String verificationEndpoint;
    private  String username;
    private  String password;
    private  String dynamicClientRegistrationEndpoint;
    private  String oauthTokenEndpoint;
    private List<ContextPath> apis;

    @XmlElement(name = "Hostname", required = true)
    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @XmlElement(name = "VerificationEndpoint", required = true)
    public String getVerificationEndpoint() {
        return verificationEndpoint;
    }

    public void setVerificationEndpoint(String verificationEndpoint) {
        this.verificationEndpoint = verificationEndpoint;
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

    @XmlElement(name = "DynamicClientRegistrationEndpoint", required = true)
    public String getDynamicClientRegistrationEndpoint() {
        return dynamicClientRegistrationEndpoint;
    }

    public void setDynamicClientRegistrationEndpoint(String dynamicClientRegistrationEndpoint) {
        this.dynamicClientRegistrationEndpoint = dynamicClientRegistrationEndpoint;
    }

    @XmlElement(name = "OauthTokenEndpoint", required = true)
    public String getOauthTokenEndpoint() {
        return oauthTokenEndpoint;
    }

    public void setOauthTokenEndpoint(String oauthTokenEndpoint) {
        this.oauthTokenEndpoint = oauthTokenEndpoint;
    }

    @XmlElementWrapper(name="APIS")
    @XmlElement(name = "ContextPath", required = true)
    public List<ContextPath> getApis() {
        return apis;
    }

    public void setApis(List<ContextPath> apis) {
        this.apis = apis;
    }

    @XmlRootElement(name = "ContextPath")
    public static class ContextPath {

        private String contextPath;

        @XmlValue()
        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }
    }
}

