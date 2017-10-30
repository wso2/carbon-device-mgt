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
package org.wso2.carbon.dynamic.client.registration.util;

/**
 * This class holds the util methods used by DCR component.
 */
public class DynamicClientRegistrationUtil {

    public static void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
    }

    public static void validateApplicationName(String applicationName) {
        if (applicationName == null || applicationName.isEmpty()) {
            throw new IllegalArgumentException("Application name cannot be null or empty");
        }
    }

    public static void validateConsumerKey(String consumerKey) {
        if (consumerKey == null || consumerKey.isEmpty()) {
            throw new IllegalArgumentException("Consumer Key cannot be null or empty");
        }
    }
}