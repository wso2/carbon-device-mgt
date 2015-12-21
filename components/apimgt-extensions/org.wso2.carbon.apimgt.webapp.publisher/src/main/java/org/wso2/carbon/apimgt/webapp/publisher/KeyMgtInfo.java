/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.webapp.publisher;

public class KeyMgtInfo {

    private String userId;
    private String applicationName;
    private String tokenType;
    private String callbackUrl;
    private String[] allowedDomains;
    private String validityTime;
    private String tokenScope;
    private String groupingId;
    private String jsonString;

    public KeyMgtInfo(String userId, String applicationName, String tokenType, String callbackUrl, String[]
            allowedDomains, String validityTime, String tokenScope, String groupingId, String jsonString) {
        this.userId = userId;
        this.applicationName = applicationName;
        this.tokenType = tokenType;
        this.callbackUrl = callbackUrl;
        this.allowedDomains = allowedDomains;
        this.validityTime = validityTime;
        this.tokenScope = tokenScope;
        this.groupingId = groupingId;
        this.jsonString = jsonString;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String[] getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(String[] allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public String getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(String validityTime) {
        this.validityTime = validityTime;
    }

    public String getTokenScope() {
        return tokenScope;
    }

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    public String getGroupingId() {
        return groupingId;
    }

    public void setGroupingId(String groupingId) {
        this.groupingId = groupingId;
    }

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }
}
