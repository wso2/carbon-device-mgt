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

function onRequest(context){
    var userModule = require("/modules/user.js").userModule;
    var constants = require("/modules/constants.js");
    var permissions = [];
    var currentUser = session.get(constants.USER_SESSION_KEY);
    if (currentUser){
        if(userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/devices/list")){
            permissions.push("LIST_DEVICES");
        }else if(userModule.isAuthorized("/permission/admin/device-mgt/user/devices/list")){
            permissions.push("LIST_OWN_DEVICES");
        }else if(userModule.isAuthorized("/permission/admin/device-mgt/emm-admin/policies/list")){
            permissions.push("LIST_POLICIES");
        }
        context.permissions = stringify(permissions);
        context.currentUser = currentUser;
        var deviceModule = require("/modules/device.js").deviceModule;
        var deviceCount = deviceModule.getOwnDevicesCount();
        if (deviceCount > 0){
            context.deviceCount = deviceCount;
        }
    }
    return context;
}