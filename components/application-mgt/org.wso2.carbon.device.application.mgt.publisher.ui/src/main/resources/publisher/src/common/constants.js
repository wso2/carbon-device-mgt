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

export default class Constants {

    static scopes = 'perm:application:get perm:application:create perm:application:update perm:application-mgt:login' +
        ' perm:application:delete perm:platform:add perm:platform:remove perm:roles:view perm:devices:view';

    static TOKEN_ENDPOINT = '/token';
    static DYNAMIC_CLIENT_REGISTER_ENDPOINT = '/api-application-registration/register';

    static appManagerEndpoints = {
        GET_ALL_APPS: 'https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/',
        CREATE_APP: 'https://localhost:8243/api/application-mgt/v1.0/applications/1.0.0/',
        UPLOAD_IMAGES: '/api/application-mgt/v1.0/applications/1.0.0/upload-image-artifacts/', //+appId
    };

    static platformManagerEndpoints = {
        CREATE_PLATFORM: 'https://localhost:8243/api/application-mgt/v1.0/platforms/1.0.0/'
    }

    static userConstants = {
        WSO2_USER: 'wso2_user',
        PARTIAL_TOKEN: 'WSO2_IOT_TOKEN'
    }

}



