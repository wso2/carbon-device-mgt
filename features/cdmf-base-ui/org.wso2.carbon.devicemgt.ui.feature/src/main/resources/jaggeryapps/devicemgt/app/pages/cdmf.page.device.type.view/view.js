/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function onRequest(context) {
    /**
     * {{#itr context}}key : {{key}} value : {{value}}{{/itr}}
     */
    context.handlebars.registerHelper("itr", function (obj, options) {
        var key, buffer = '';
        for (key in obj) {
            if (obj.hasOwnProperty(key)) {
                buffer += options.fn({key: key, value: obj[key]});
            }
        }
        return buffer;
    });

    var utility = require("/app/modules/utility.js").utility;
    var deviceType = context.uriParams.deviceType;

    var configs = utility.getDeviceTypeConfig(deviceType);
    if(!configs["deviceType"]){
        throw new Error("Invalid Device Type Configurations Found!","");
    }

    return {
        "deviceTypeViewUnitName": "cdmf.unit.device.type." + deviceType + ".type-view",
        "deviceType": deviceType,
        "label" : configs["deviceType"]["label"]
    };
}

