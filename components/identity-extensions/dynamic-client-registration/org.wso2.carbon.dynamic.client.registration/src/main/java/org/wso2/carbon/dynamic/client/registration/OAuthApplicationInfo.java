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

package org.wso2.carbon.dynamic.client.registration;

import org.json.simple.JSONObject;
import org.wso2.carbon.dynamic.client.registration.util.DCRConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents an OAuth application populated with necessary data.
 */
public class OAuthApplicationInfo {

    private String clientId;
    private String clientName;
    private String callBackURL;
    private String clientSecret;
    private Map<String, Object> parameters = new HashMap<String, Object>();

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

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public void addParameter(String name, Object value) {
        parameters.put(name, value);
    }

    public Object getParameter(String name) {
        return parameters.get(name);
    }

    public String getJsonString() {
        return JSONObject.toJSONString(parameters);
    }

    public String getClientName() {
        return clientName;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void putAll(Map<String, Object> parameters) {
        this.parameters.putAll(parameters);
    }

    public void removeParameter(String key) {
        this.parameters.remove(key);
    }

    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put(DCRConstants.ClientMetadata.OAUTH_CLIENT_ID, this.getClientId());
        obj.put(DCRConstants.ClientMetadata.OAUTH_CLIENT_NAME, this.getClientName());
        obj.put(DCRConstants.ClientMetadata.OAUTH_CALLBACK_URIS, this.getCallBackURL());
        obj.put(DCRConstants.ClientMetadata.OAUTH_CLIENT_SECRET, this.getClientSecret());
        return obj.toString();
    }

}
