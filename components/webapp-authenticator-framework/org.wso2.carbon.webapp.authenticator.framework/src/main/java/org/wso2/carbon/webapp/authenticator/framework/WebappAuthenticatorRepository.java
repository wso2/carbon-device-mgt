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

package org.wso2.carbon.webapp.authenticator.framework;

import org.wso2.carbon.webapp.authenticator.framework.authenticator.WebappAuthenticator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebappAuthenticatorRepository {

    private Map<String, WebappAuthenticator> authenticators;

    public WebappAuthenticatorRepository() {
        this.authenticators = new ConcurrentHashMap<>();
    }

    public void addAuthenticator(WebappAuthenticator authenticator) {
        if (authenticator == null) {
            throw new IllegalStateException("Authenticator implementation to be added to the webapp " +
                    "authenticator repository cannot be null or empty");
        }
        if (authenticator.getName() == null || authenticator.getName().isEmpty()) {
            throw new IllegalStateException("Authenticator name cannot be null or empty");
        }
        authenticators.put(authenticator.getName(), authenticator);
    }

    public WebappAuthenticator getAuthenticator(String name) {
        return authenticators.get(name);
    }

    public Map<String, WebappAuthenticator> getAuthenticators() {
        return authenticators;
    }

}
