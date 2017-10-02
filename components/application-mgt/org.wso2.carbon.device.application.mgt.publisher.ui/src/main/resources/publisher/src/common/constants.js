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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

'use strict';

//TODO: Replace the server address with response from auth endpoint and remove hardcoded ids etc.
export default class Constants {

    static scopes = 'perm:application:get perm:application:create perm:application:update perm:application-mgt:login' +
        ' perm:application:delete perm:platform:add perm:platform:remove perm:roles:view perm:devices:view';

    static appManagerEndpoints = {
        GET_ALL_APPS: 'https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/',
        CREATE_APP: 'https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/',
        UPLOAD_IMAGE_ARTIFACTS: 'https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/upload-image-artifacts/', //+appId
        GET_IMAGE_ARTIFACTS: "https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/image-artifacts/"
    };

    static platformManagerEndpoints = {
        CREATE_PLATFORM: 'https://localhost:8243/api/application-mgt/v1.0/platforms/1.0.0',
        GET_ENABLED_PLATFORMS: 'https://localhost:8243/api/application-mgt/v1.0/platforms/1.0.0?status=ENABLED',
        GET_PLATFORM: 'https://localhost:8243/api/application-mgt/v1.0/platforms/1.0.0/'
    };

    static userConstants = {
        LOGIN_URL:"https://localhost:9443/auth/application-mgt/v1.0/auth/login",
        LOGOUT_URL: "https://localhost:9443/auth/application-mgt/v1.0/auth/logout",
        REFRESH_TOKEN_URL: "",
        WSO2_USER: 'wso2_user',
        PARTIAL_TOKEN: 'WSO2_IOT_TOKEN'
    };

    static hostConstants = {
        baseURL : window.location.origin,
        appContext : window.location.pathname.split("/")[1]

    }

    static defaultLocale = "en";
}
