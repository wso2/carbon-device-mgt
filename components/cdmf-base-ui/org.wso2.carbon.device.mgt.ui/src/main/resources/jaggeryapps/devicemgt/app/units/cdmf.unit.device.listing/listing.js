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

function onRequest(context){
    var page_data = {};
    var groupId = request.getParameter("groupId");
    var userModule = require("/app/modules/user.js").userModule;
    var constants = require("/app/modules/constants.js");
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
        page_data.permissions = stringify(permissions);
        page_data.currentUser = currentUser;
        var deviceCount;
        if (groupId) {
            var groupModule = require("/app/modules/group.js").groupModule;
            deviceCount = groupModule.getDevices(groupId).data.length;
            page_data.groupId = groupId;
        } else {
            var deviceModule = require("/app/modules/device.js").deviceModule;
            deviceCount = deviceModule.getOwnDevicesCount();
        }
        if (deviceCount > 0){
            page_data.deviceCount = deviceCount;
        }
    }
    return page_data;
}