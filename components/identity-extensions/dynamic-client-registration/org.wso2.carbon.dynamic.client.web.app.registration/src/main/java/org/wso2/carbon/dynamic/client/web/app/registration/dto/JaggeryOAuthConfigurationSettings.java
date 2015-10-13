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

package org.wso2.carbon.dynamic.client.web.app.registration.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents OAuthConfiguration data required to create OAuth service provider for Jaggery apps.
 */
@XmlRootElement(name = "OAuthSettings")
public class JaggeryOAuthConfigurationSettings {

    private String grantType;
    private boolean saasApp;
    private String callbackURL;
    private String tokenScope;
    private boolean requireDynamicClientRegistration;

    @XmlElement(name = "saasApp", required = true)
    public boolean isSaasApp() {
        return saasApp;
    }

    public void setSaasApp(boolean saasApp) {
        this.saasApp = saasApp;
    }

    @XmlElement(name = "callbackURL", required = false)
    public String getCallbackURL() {
        return callbackURL;
    }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    @XmlElement(name = "tokenScope", required = false)
    public String getTokenScope() {
        return tokenScope;
    }

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    @XmlElement(name = "grantType", required = true)
    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    @XmlElement(name = "requireDynamicClientRegistration", required = true)
    public boolean isRequireDynamicClientRegistration() {
        return requireDynamicClientRegistration;
    }

    public void setRequireDynamicClientRegistration(boolean requireDynamicClientRegistration) {
        this.requireDynamicClientRegistration = requireDynamicClientRegistration;
    }
}
