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
 *
 */

import axios from 'axios';
import Constants from './constants';


class Configuration {

    constructor() {
        this.serverConfig = {};
        this.hostConstants = {
            baseURL: window.location.origin,
            appContext: window.location.pathname.split("/")[1]
        };
    }

    loadConfiguration(callback) {
        let thisObject = this;
        axios.get(thisObject.hostConstants.baseURL + '/' + thisObject.hostConstants.appContext + "/config.json").
        then(function (response) {
            console.log('server config was read successfully! ');
            thisObject.serverConfig = response.data.config;
            Constants.load();
            callback();
        }).catch(function (error) {
            console.log('unable to load the config file!' + error);
        });
    }

}

export default (new Configuration());
