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

package org.wso2.carbon.apimgt.integration.client.exception;

/**
 * This holds api client exception.
 */
public class APIMClientOAuthException extends RuntimeException {

    private static final long serialVersionUID = -3976392476319079281L;
    private String responseReason;
    private int responseStatus;
    private String methodKey;

    APIMClientOAuthException(String methodKey, String reason, int status) {
        super("Exception occured while invoking " + methodKey + " status = " + status + " reason = " + reason);
        this.methodKey = methodKey;
        this.responseReason = reason;
        this.responseStatus = status;
    }

    APIMClientOAuthException(String message) {
        super(message);
    }

    public APIMClientOAuthException(String message, Exception e) {
        super(message, e);
    }

    public String getResponseReason() {
        return responseReason;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getMethodKey() {
        return methodKey;
    }

}