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

function onRequest(context) {
    var page = {};
    var groupId = request.getParameter("groupId");
    var userModule = require("/app/modules/user.js").userModule;
    var constants = require("/app/modules/constants.js");
    var permissions = [];
    var currentUser = session.get(constants.USER_SESSION_KEY);
    if (currentUser) {
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/list")) {
            permissions.push("LIST_DEVICES");
        } else if (userModule.isAuthorized("/permission/admin/device-mgt/user/devices/list")) {
            permissions.push("LIST_OWN_DEVICES");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/add")) {
            permissions.push("ADD_DEVICE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/edit")) {
            permissions.push("EDIT_DEVICE");
        }
        if (userModule.isAuthorized("/permission/admin/device-mgt/admin/devices/remove")) {
            permissions.push("REMOVE_DEVICE");
        }
        page.permissions = stringify(permissions);
        page.currentUser = currentUser;
    }
    return page;
}