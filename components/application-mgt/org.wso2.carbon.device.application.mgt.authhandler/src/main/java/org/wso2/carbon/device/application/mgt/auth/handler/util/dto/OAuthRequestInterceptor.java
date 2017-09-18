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


import feign.RequestInterceptor;
import feign.RequestTemplate;

import static feign.Util.checkNotNull;

/**
 * This is a request interceptor to add oauth token header.
 */
public class OAuthRequestInterceptor implements RequestInterceptor {

    private final String headerValue;

    /**
     * Creates an interceptor that authenticates all requests with the specified OAUTH token
     *
     * @param token the access token to use for authentication
     */
    public OAuthRequestInterceptor(String token) {
        checkNotNull(token, "access_token");
        headerValue = "Bearer " + token;
    }
    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", headerValue);
    }
}
