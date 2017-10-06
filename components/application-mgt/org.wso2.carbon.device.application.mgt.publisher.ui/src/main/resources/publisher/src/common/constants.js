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
import Configuration from './configuration';

'use strict';

class Constants {

    constructor() {
        this.scopes = 'perm:application:get perm:application:create perm:application:update perm:application-mgt:login' +
            ' perm:application:delete perm:platform:add perm:platform:remove perm:roles:view perm:devices:view';
        this.appManagerEndpoints = {};
        this.platformManagerEndpoints = {};
        this.userConstants = {};
        this.defaultLocale = "en";

    }

    load() {
        let applicationApiContext = '/api/application-mgt/v1.0/applications/1.0.0/';
        let platformApiContext = '/api/application-mgt/v1.0/platforms/1.0.0';

        let apiBaseUrl = 'https://' + Configuration.serverConfig.hostname + ':' + Configuration.serverConfig.apiPort;
        let httpBaseUrl = 'https://' + Configuration.serverConfig.hostname + ':' + Configuration.serverConfig.httpsPort;

        this.appManagerEndpoints = {
            GET_ALL_APPS: apiBaseUrl + applicationApiContext,
            CREATE_APP: apiBaseUrl + applicationApiContext,
            UPLOAD_IMAGE_ARTIFACTS: apiBaseUrl + applicationApiContext + 'upload-image-artifacts/', //+appId
            GET_IMAGE_ARTIFACTS: apiBaseUrl + applicationApiContext + 'image-artifacts/',
            APP_RELEASE: apiBaseUrl + applicationApiContext + "release/", //+uuid
            GET_APP_RELEASE_ARTIFACTS: apiBaseUrl + applicationApiContext + "/release-artifacts/", //+AppId/version
            GET_NEXT_LIFECYCLE_STATE: apiBaseUrl + applicationApiContext //+ [uuid]/lifecycle
        };

        this.platformManagerEndpoints = {
            CREATE_PLATFORM: apiBaseUrl + platformApiContext,
            GET_ENABLED_PLATFORMS: apiBaseUrl + platformApiContext + '?status=ENABLED',
            GET_PLATFORM: apiBaseUrl + platformApiContext, //+platformId
            GET_PLATFORMS: apiBaseUrl + platformApiContext,
            UPDATE_STATUS: apiBaseUrl + platformApiContext + "update-status/", // + platformId + ?status=
            EDIT_PLATFORM: apiBaseUrl + platformApiContext //+platformId
        };

        this.userConstants = {
            LOGIN_URL: httpBaseUrl + '/auth/application-mgt/v1.0/auth/login',
            LOGOUT_URL: httpBaseUrl + '/auth/application-mgt/v1.0/auth/logout',
            REFRESH_TOKEN_URL: "",
            WSO2_USER: 'wso2_user',
            PARTIAL_TOKEN: 'WSO2_IOT_TOKEN'
        };
    }
}

export default(new Constants);
