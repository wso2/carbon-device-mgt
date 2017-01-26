/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.integration.client.model;

public class OAuthApplication {

    private String jsonString;
    private String appOwner;
    private String clientName;
    private String callBackURL;
    private String isSaasApplication;
    private String clientId;
    private String clientSecret;

    public String getJsonString() {
        return jsonString;
    }

    public void setJsonString(String jsonString) {
        this.jsonString = jsonString;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getIsSaasApplication() {
        return isSaasApplication;
    }

    public void setIsSaasApplication(String isSaasApplication) {
        this.isSaasApplication = isSaasApplication;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OAuthApplication {\n");

        sb.append("  jsonString: ").append(jsonString).append("\n");
        sb.append("  appOwner: ").append(appOwner).append("\n");
        sb.append("  clientName: ").append(clientName).append("\n");
        sb.append("  callBackURL: ").append(callBackURL).append("\n");
        sb.append("  isSaasApplication: ").append(isSaasApplication).append("\n");
        sb.append("  clientId: ").append(isSaasApplication).append("\n");
        sb.append("  clientSecret: ").append(clientSecret).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

}
