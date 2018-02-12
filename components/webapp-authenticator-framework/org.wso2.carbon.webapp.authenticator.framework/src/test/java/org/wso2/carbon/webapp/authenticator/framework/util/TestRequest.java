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

package org.wso2.carbon.webapp.authenticator.framework.util;

import org.apache.catalina.connector.Request;

/**
 * This is a test class implementation of {@link Request}
 */
public class TestRequest extends Request {
    private String contextPath;
    private String requestURI;

    public TestRequest(String contextPath, String requestURI) {
        this.contextPath = contextPath;
        this.requestURI = requestURI;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }
}
