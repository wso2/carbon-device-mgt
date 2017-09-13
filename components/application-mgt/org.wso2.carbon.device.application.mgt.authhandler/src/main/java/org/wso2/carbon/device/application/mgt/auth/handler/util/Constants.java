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

package org.wso2.carbon.device.application.mgt.auth.handler.util;

public class Constants {
    public static String SCOPES = "perm:application:get perm:application:create perm:application:update " +
            "perm:application-mgt:login perm:application:delete perm:platform:add perm:platform:remove " +
            "perm:roles:view perm:devices:view perm:platform:get";

    public static String[] TAGS = {"device_management"};
    public static String USER_NAME = "userName";
    public static String APPLICATION_NAME = "applicationmgt_publisher";
    public static String TOKEN_ENDPOINT = "https://localhost:8243";
    public static String PASSWORD_GRANT_TYPE = "password";
    public static String REFRESH_GRANT_TYPE = "refresh_token";
    public static String API_APPLICATION_ENDPOINT = "https://localhost:9443/api-application-registration/";
    public static String APPLICATION_INFO = "application_info";
}
