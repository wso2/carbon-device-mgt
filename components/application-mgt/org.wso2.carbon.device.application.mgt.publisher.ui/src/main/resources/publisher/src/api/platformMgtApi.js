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

import Axios from 'axios';
import AuthHandler from './authHandler';
import Constants from '../common/constants';
import Helper from './helpers/appMgtApiHelpers';

/**
 * Api definitions for Platform management.
 * */
export default class PlatformMgtApi{
    /**
     * Create a new Platform
     * @param general: Platform general information.
     * @param config: Platform configurations.
     * @param prop: Platform properties.
     * */
    static createPlatform(general, config, prop) {
        const headers = AuthHandler.createAuthenticationHeaders("multipart/form-data");
        let platform = Helper.buildPlatform(general, config, prop);

        let platformData = new FormData();
        platformData.append("platform", platform.platform);
        platformData.append("icon", platform.icon);

        return Axios.post(Constants.platformManagerEndpoints.CREATE_PLATFORM, platformData, {headers: headers});
    }

    /**
     * Get available platforms
     * */
    static getPlatforms() {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.get(Constants.platformManagerEndpoints.GET_ENABLED_PLATFORMS, {headers: headers});
    }

    /**
     * Get the user specified platform
     * @param platformId: The identifier of the platform
     * */
    static getPlatform(platformId) {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.get(Constants.platformManagerEndpoints.GET_PLATFORM + platformId, {headers: headers});
    }

    /**
     * Delete specified platform
     * @param platformId: The id of the platform which is to be deleted.
     * */
    static deletePlatform(platformId) {
        const headers = AuthHandler.createAuthenticationHeaders("application/json");
        return Axios.delete(Constants.platformManagerEndpoints.GET_PLATFORM + platformId, {headers: headers});
    }
}